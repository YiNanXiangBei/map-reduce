package org.yinan.rpc.service.rpcImpl;

import io.grpc.stub.StreamObserver;
import org.yinan.grpc.HeartBeatInfo;
import org.yinan.grpc.MapRemoteFileEntry;
import org.yinan.grpc.ReduceRemoteEntry;
import org.yinan.grpc.ResultInfo;
import org.yinan.grpc.WorkerReceiveServiceGrpc;
import org.yinan.rpc.service.WorkerReceiver;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class WorkerReceiveService extends WorkerReceiver {
    @Override
    public void mapReceiveSender(MapRemoteFileEntry request, StreamObserver<ResultInfo> responseObserver) {
        try {
            super.mapReceiveSender(request);
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
        super.reduceReceiveSender(request);
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
            super.heartBeat(request);
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
