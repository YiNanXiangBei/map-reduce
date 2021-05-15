package org.yinan.rpc.service.impl;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.grpc.HeartBeatInfo;
import org.yinan.grpc.MapRemoteFileEntry;
import org.yinan.grpc.ReduceRemoteEntry;
import org.yinan.grpc.ResultInfo;
import org.yinan.rpc.service.WorkerReceiver;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class WorkerReceiveServiceImpl extends WorkerReceiver {
    private final static Logger LOGGER = LoggerFactory.getLogger(WorkerReceiveServiceImpl.class);

    private final WorkerReceiver receiver;

    public WorkerReceiveServiceImpl(WorkerReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void mapReceiveSender(MapRemoteFileEntry request, StreamObserver<ResultInfo> responseObserver) {
        try {
            receiver.mapReceiveSender(request);
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
    public void reduceReceiveSender(ReduceRemoteEntry request, StreamObserver<ResultInfo> responseObserver) {
        try {
            receiver.reduceReceiveSender(request);
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
    public void heartBeat(HeartBeatInfo request, StreamObserver<ResultInfo> responseObserver) {
        try {
            receiver.heartBeat(request);
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
