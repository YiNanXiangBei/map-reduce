package org.yinan.rpc.service.impl;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.grpc.MapBackFeedEntry;
import org.yinan.grpc.ReduceBackFeedEntry;
import org.yinan.grpc.ResultInfo;
import org.yinan.rpc.service.MasterReceiver;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class MasterReceiveServiceImpl extends MasterReceiver {
    private final static Logger LOGGER = LoggerFactory.getLogger(MasterReceiveServiceImpl.class);

    private final MasterReceiver receiver;

    public MasterReceiveServiceImpl(MasterReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void mapNotify(MapBackFeedEntry request, StreamObserver<ResultInfo> responseObserver) {
        try {
            receiver.mapNotify(request);
        } catch (Exception e) {
            responseObserver.onNext(ResultInfo
                    .newBuilder()
                    .setSuccess(false)
                    .setCode(500)
                    .setMessage(e.toString())
                    .build());
            responseObserver.onCompleted();
            return;
        }
        responseObserver.onNext(ResultInfo
                .newBuilder()
                .setSuccess(true)
                .setCode(200)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void reduceNotify(ReduceBackFeedEntry request, StreamObserver<ResultInfo> responseObserver) {
        try {
            receiver.reduceNotify(request);
        } catch (Exception e) {
            responseObserver.onNext(ResultInfo
                    .newBuilder()
                    .setSuccess(false)
                    .setCode(500)
                    .setMessage(e.toString())
                    .build());
            responseObserver.onCompleted();
            return;
        }
        responseObserver.onNext(ResultInfo
                .newBuilder()
                .setSuccess(true)
                .setCode(200)
                .build());
        responseObserver.onCompleted();
    }
}
