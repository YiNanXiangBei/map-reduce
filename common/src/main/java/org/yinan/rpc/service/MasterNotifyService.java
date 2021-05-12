package org.yinan.rpc.service;

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

    private final WorkerReceiveServiceGrpc.WorkerReceiveServiceBlockingStub blockingStub;

    public MasterNotifyService(WorkerReceiveServiceGrpc
                                       .WorkerReceiveServiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public ResultInfo notifyMap(MapRemoteFileEntry request) {
        return blockingStub.mapReceiveSender(request);
    }

    public ResultInfo notifyReduce(ReduceRemoteEntry request) {
        return blockingStub.reduceReceiveSender(request);
    }

    public ResultInfo heartBeat(HeartBeatInfo heartBeatInfo) {
        return blockingStub.heartBeat(heartBeatInfo);
    }

}
