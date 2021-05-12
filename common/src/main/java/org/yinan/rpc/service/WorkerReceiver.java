package org.yinan.rpc.service;

import io.grpc.stub.StreamObserver;
import org.yinan.grpc.HeartBeatInfo;
import org.yinan.grpc.MapRemoteFileEntry;
import org.yinan.grpc.ReduceRemoteEntry;
import org.yinan.grpc.ResultInfo;
import org.yinan.grpc.WorkerReceiveServiceGrpc;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class WorkerReceiver extends WorkerReceiveServiceGrpc.WorkerReceiveServiceImplBase{

    public void mapReceiveSender(MapRemoteFileEntry request) {
        //TODO 业务处理
        System.out.println(request);
    }



    public void reduceReceiveSender(ReduceRemoteEntry request) {
        //TODO 业务处理
        System.out.println(request);

    }


    public void heartBeat(HeartBeatInfo request) {
        //TODO 业务处理
        System.out.println(request);

    }

}
