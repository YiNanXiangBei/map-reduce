package org.yinan.reduce;

import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.ProcessContext;
import org.yinan.grpc.MapRemoteFileEntry;
import org.yinan.grpc.ReduceBackFeedEntry;
import org.yinan.grpc.ReduceRemoteEntry;
import org.yinan.io.FileStreamUtil;
import org.yinan.io.ShellUtils;
import org.yinan.rpc.service.WorkerNotifyService;
import org.yinan.rpc.service.callback.ICallBack;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yinan
 * @date 2021/5/23
 */
public class ReduceReceiverCallBack implements ICallBack<ReduceRemoteEntry> {

    private final AtomicInteger atomicInteger = new AtomicInteger();

    private final String REDUCE_FILE = "_REDUCE_FILE_";

    private final String REDUCE_RESULT = "REDUCE_RESULT.json";

    private final static Logger LOGGER = LoggerFactory.getLogger(ReduceReceiverCallBack.class);

    @Override
    public void call(ReduceRemoteEntry reduceRemoteEntry) {
        LOGGER.info("node reduce receive message from master ...");
        List<MapRemoteFileEntry> allMaps = reduceRemoteEntry.getMapDealInfoList();
        Map<String, Object> results = new HashMap<>();
        String currentIp = System.getProperty("user.host");
        LOGGER.info("================== get local ip: {} ==================", currentIp);
        allMaps.forEach(mapNode -> {
            String mapIp = mapNode.getRemoteIp();
            Integer mapPort = mapNode.getRemotePort();
            String fileName = mapNode.getFileName();
            String userName = mapNode.getUsername();
            String password = mapNode.getPassword();
            String fileType = "." + fileName.split("\\.")[1];
            String localFile = mapIp + REDUCE_FILE + atomicInteger.incrementAndGet()
                    + fileType;
            if (!mapIp.equals(currentIp)) {
                LOGGER.info("download from remote map ip : {}, port: {}, remote file name: {}, local file name: {}",
                        mapIp, mapPort, fileName, localFile);
                ShellUtils.scpDownload(mapIp, mapPort, userName, password,
                        fileName, localFile);
            } else {
                LOGGER.info("download from local map ip : {}, port: {}, remote file name: {}, local file name: {}",
                        mapIp, mapPort, fileName, localFile);
                if (!ShellUtils.cp(fileName, localFile)) {
                    //复制文件失败
                    new WorkerNotifyService()
                            .reduceNotify(ReduceBackFeedEntry.newBuilder()
                                    .setIp(currentIp)
                                    .setFinished(false)
                                    .setMessage("load file error, result may be not complete!")
                                    .build());
                    return;
                }
            }
            try {
                Map<String, Object> mapContent = FileStreamUtil.load(new TypeReference<Map<String, Object>>() {},
                        localFile, new HashMap<>());
                IReduce reduce = ProcessContext.getReduce();
                results.putAll(reduce.reduce(mapContent, reduceRemoteEntry.getKeysList()));
            } catch (Exception e) {
                LOGGER.error("load local file error: {}", e.toString());
                new WorkerNotifyService()
                        .reduceNotify(ReduceBackFeedEntry.newBuilder()
                                .setIp(currentIp)
                                .setFinished(false)
                                .setMessage("load file error, result may be not complete!")
                                .build());
            }
        });
        //保存文件，通知master
        boolean success = FileStreamUtil.save(results, REDUCE_RESULT);
        new WorkerNotifyService()
                .reduceNotify(ReduceBackFeedEntry.newBuilder()
                        .setIp(currentIp)
                        .setFinished(success)
                        .setFileLocation(System.getProperty("user.dir") + "/" + REDUCE_RESULT)
                        .build());
    }
}
