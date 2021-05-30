package org.yinan.rpc.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.grpc.HeartBeatInfo;
import org.yinan.grpc.MapRemoteFileEntry;
import org.yinan.grpc.ReduceRemoteEntry;
import org.yinan.grpc.ResultInfo;
import org.yinan.grpc.WorkerReceiveServiceGrpc;

/**
 * @author yinan
 * @date 2021/5/12
 * 主节点发送消息给worker节点
 */
public class MasterNotifyService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MasterNotifyService.class);

    private final WorkerReceiveServiceGrpc.WorkerReceiveServiceBlockingStub blockingStub;

    private final ManagedChannel channel;

    public MasterNotifyService(String ip, Integer port) {
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forAddress(ip, port)
                .usePlaintext();
        channel = channelBuilder.build();
        this.blockingStub = WorkerReceiveServiceGrpc.newBlockingStub(channel);
    }

    public ResultInfo notifyMap(MapRemoteFileEntry request) {
        try {
            return blockingStub.mapReceiveSender(request);
        } catch (Exception e) {
            LOGGER.error("notify map worker error: {}", e.toString());
            return ResultInfo.newBuilder()
                    .setSuccess(false)
                    .setCode(500)
                    .setMessage("can not connect map worker")
                    .build();
        } finally {
            if (channel != null) {
                channel.shutdown();
            }
        }
    }

    public ResultInfo notifyReduce(ReduceRemoteEntry request) {
        try {
            return blockingStub.reduceReceiveSender(request);
        } catch (Exception e) {
            LOGGER.error("notify reduce worker error: {}", e.toString());
            return ResultInfo.newBuilder()
                    .setSuccess(false)
                    .setCode(500)
                    .setMessage("can not connect reduce worker")
                    .build();
        }
    }

    public ResultInfo heartBeat(HeartBeatInfo heartBeatInfo) {
        try {
            return blockingStub.heartBeat(heartBeatInfo);
        } catch (Exception e) {
            LOGGER.error("heartbeat with worker error: {}", e.toString());
            return ResultInfo.newBuilder()
                    .setSuccess(false)
                    .setCode(500)
                    .setMessage("can not connect worker")
                    .build();
        }
    }


}
