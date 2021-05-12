package org.yinan.rpc.service;

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

    private MasterReceiveServiceGrpc.MasterReceiveServiceBlockingStub blockingStub;

    public WorkerNotifyService(MasterReceiveServiceGrpc
                                       .MasterReceiveServiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
    }

    public ResultInfo mapNotify(MapBackFeedEntry request) {
        return blockingStub.mapNotify(request);
    }

    public ResultInfo reduceNotify(ReduceBackFeedEntry request) {
        return blockingStub.reduceNotify(request);
    }

}
