package org.yinan.rpc.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.config.context.ConfigContext;
import org.yinan.grpc.MapBackFeedEntry;
import org.yinan.grpc.MasterReceiveServiceGrpc;
import org.yinan.grpc.ReduceBackFeedEntry;
import org.yinan.grpc.ResultInfo;

/**
 * @author yinan
 * @date 2021/5/12
 * worker节点发送消息给主节点
 */
public class WorkerNotifyService {

    private final static Logger LOGGER = LoggerFactory.getLogger(WorkerNotifyService.class);

    private final MasterReceiveServiceGrpc.MasterReceiveServiceBlockingStub blockingStub;

    public WorkerNotifyService() {
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forAddress(ConfigContext.getInstance().getMasterInfo().getIp(),
                        ConfigContext.getInstance().getMasterInfo().getPort())
                .usePlaintext();
        ManagedChannel channel = channelBuilder.build();
        this.blockingStub = MasterReceiveServiceGrpc.newBlockingStub(channel);

    }

    public ResultInfo mapNotify(MapBackFeedEntry request) {
        try {
            return blockingStub.mapNotify(request);
        } catch (Exception e) {
            LOGGER.error("worker map notify master error: {}", e.toString());
            return ResultInfo.newBuilder()
                    .setSuccess(false)
                    .setCode(500)
                    .setMessage("can not connect master")
                    .build();
        }
    }

    public ResultInfo reduceNotify(ReduceBackFeedEntry request) {
        try {
            return blockingStub.reduceNotify(request);
        } catch (Exception e) {
            LOGGER.error("worker reduce notify master error: {}", e.toString());
            return ResultInfo.newBuilder()
                    .setSuccess(false)
                    .setCode(500)
                    .setMessage("can not connect master")
                    .build();
        }
    }

}
