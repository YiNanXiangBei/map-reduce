package org.yinan.rpc.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.config.context.ConfigContext;
import org.yinan.grpc.HeartBeatInfo;
import org.yinan.grpc.MapRemoteFileEntry;
import org.yinan.grpc.ReduceRemoteEntry;
import org.yinan.grpc.ResultInfo;
import org.yinan.grpc.WorkerReceiveServiceGrpc;
import org.yinan.rpc.service.callback.ICallBack;
import org.yinan.rpc.service.impl.WorkerReceiveServiceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class WorkerReceiver extends WorkerReceiveServiceGrpc.WorkerReceiveServiceImplBase{

    private final static Logger LOGGER = LoggerFactory.getLogger(MasterReceiver.class);

    private static volatile boolean hashInit = false;

    public final static String MAP_RECEIVER_SENDER = "map_receiver_sender";

    public final static String REDUCE_RECEIVER_SENDER = "reduce_receiver_sender";

    public final static String HEART_BEAT = "heart_beat";

    private final Map<String, ICallBack<?>> callBackMap = new HashMap<>();

    private Server server;

    public WorkerReceiver() {
        if (!hashInit) {
            try {
                hashInit = true;
                this.server = ServerBuilder.forPort(ConfigContext.getInstance()
                        .getRpcInfos()
                        .get("worker").getPort())
                        .addService(new WorkerReceiveServiceImpl(this))
                        .build();

            } catch (Exception e) {
                LOGGER.error("create grpc error: {}", e.toString());
            }
        }
    }
    public WorkerReceiver register(String name, ICallBack<?> callBack) {
        callBackMap.put(name, callBack);
        return this;
    }

    public void finished() {
        if (this.server != null) {
            try {
                this.server.start();
                this.server.awaitTermination();
            } catch (Exception e) {
                LOGGER.error("init server error: {}", e.toString());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void mapReceiveSender(MapRemoteFileEntry request) {
        LOGGER.info("receive map remote info: {}", request.toString());
        ((ICallBack<MapRemoteFileEntry>) callBackMap.get(MAP_RECEIVER_SENDER)).call(request);
    }

    @SuppressWarnings("unchecked")
    public void reduceReceiveSender(ReduceRemoteEntry request) {
        LOGGER.info("receive reduce remote info: {}", request.toString());
        ((ICallBack<ReduceRemoteEntry>) callBackMap.get(REDUCE_RECEIVER_SENDER)).call(request);

    }

    @SuppressWarnings("unchecked")
    public void heartBeat(HeartBeatInfo request) {
        LOGGER.info("receive heart beat info: {}", request.toString());
        ((ICallBack<HeartBeatInfo>) callBackMap.get(HEART_BEAT)).call(request);
    }

}
