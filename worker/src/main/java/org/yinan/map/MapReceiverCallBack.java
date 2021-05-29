package org.yinan.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.ProcessContext;
import org.yinan.grpc.DealFile;
import org.yinan.grpc.MapBackFeedEntry;
import org.yinan.grpc.MapRemoteFileEntry;
import org.yinan.io.FileStreamUtil;
import org.yinan.io.ShellUtils;
import org.yinan.rpc.service.WorkerNotifyService;
import org.yinan.rpc.service.callback.ICallBack;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author yinan
 * @date 2021/5/23
 */
public class MapReceiverCallBack implements ICallBack<MapRemoteFileEntry> {

    private final static Logger LOGGER = LoggerFactory.getLogger(MapReceiverCallBack.class);

    private final AtomicInteger count = new AtomicInteger();

    private final static String MAP_RECEIVE_FILE = "MAP_RECEIVE_FILE_";

    private final static String MAP_GENERATE_FILE = "MAP_GENERATE_FILE_";

    /**
     * 到这里说明请求没有问题，有问题的是结果
     * @param mapRemoteFileEntry
     */
    @Override
    public void call(MapRemoteFileEntry mapRemoteFileEntry) {
        //去指定ip获取文件，保存到本地
        LOGGER.info("node map receive message from master ...");
        String fileSystemIp = mapRemoteFileEntry.getRemoteIp();
        int fileSystemPort = mapRemoteFileEntry.getRemotePort();
        String fileName = mapRemoteFileEntry.getFileLocation();
        String username = mapRemoteFileEntry.getUsername();
        String password = mapRemoteFileEntry.getPassword();
        String fileType = mapRemoteFileEntry.getFileLocation().split("\\.")[1];
        String localFile = MAP_RECEIVE_FILE + count + "." + fileType;
        String mapGeneFile = MAP_GENERATE_FILE + count.getAndIncrement() + ".json";
        ShellUtils.scpDownload(fileSystemIp, fileSystemPort, username, password,
                fileName, localFile);
        //调用用户实现的接口IMap
        IMap map = ProcessContext.getMap();
        long currentTime = System.currentTimeMillis();
        String ip = System.getProperty("user.host");
        LOGGER.info("================== get local ip: {} ==================", ip);
        try {
            Map<String, Object> results = map.map(FileStreamUtil.readJsonFile(localFile), mapGeneFile);
            long spendTime = System.currentTimeMillis() - currentTime;
            List<String> keys = new ArrayList<>(results.keySet());
            //执行结果返回给master
            new WorkerNotifyService()
                    .mapNotify(
                            MapBackFeedEntry
                                    .newBuilder()
                                    .setIp(ip)
                                    .setFileSystemLocation(fileSystemIp + "::" + fileName)
                                    .setSuccess(true)
                                    .setSpendTime((int) spendTime)
                                    .addDeaFiles(DealFile.newBuilder()
                                            .addAllKeys(keys)
                                            .setFileName(System.getProperty("user.dir") + "/" + mapGeneFile)
                                            .build())
                                    .build()
                    );
        } catch (IOException e) {
            LOGGER.error("can not read file :{}, error: {}", localFile, e.toString());
            new WorkerNotifyService()
                    .mapNotify(
                            MapBackFeedEntry
                                    .newBuilder()
                                    .setIp(ip)
                                    .setSuccess(false)
                                    .build());
        }
    }
}
