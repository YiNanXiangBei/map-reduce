package org.yinan.rpc.service.rpcImpl;

import io.grpc.stub.StreamObserver;
import org.yinan.grpc.MapBackFeedEntry;
import org.yinan.grpc.MasterReceiveServiceGrpc;
import org.yinan.grpc.ReduceBackFeedEntry;
import org.yinan.grpc.ResultInfo;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class MasterReceiveService extends MasterReceiveServiceGrpc.MasterReceiveServiceImplBase {
    @Override
    public void mapNotify(MapBackFeedEntry request, StreamObserver<ResultInfo> responseObserver) {
        super.mapNotify(request, responseObserver);
    }

    @Override
    public void reduceNotify(ReduceBackFeedEntry request, StreamObserver<ResultInfo> responseObserver) {
        super.reduceNotify(request, responseObserver);
    }
}
