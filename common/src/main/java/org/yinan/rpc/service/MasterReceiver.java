package org.yinan.rpc.service;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.config.context.ConfigContext;
import org.yinan.grpc.MapBackFeedEntry;
import org.yinan.grpc.MasterReceiveServiceGrpc.MasterReceiveServiceImplBase;
import org.yinan.grpc.ReduceBackFeedEntry;
import org.yinan.rpc.service.callback.ICallBack;
import org.yinan.rpc.service.impl.MasterReceiveServiceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class MasterReceiver extends MasterReceiveServiceImplBase {

    private static volatile boolean hashInit = false;

    private final static Logger LOGGER = LoggerFactory.getLogger(MasterReceiver.class);

    private final Map<String, ICallBack<?>> callBackMap = new HashMap<>();

    public final static String MAP_NOTIFY = "map_notify";

    public final static String REDUCE_NOTIFY = "reduce_notify";

    private Server server;

    public MasterReceiver() {
        if (!hashInit) {
            try {
                hashInit = true;
                this.server = ServerBuilder.forPort(ConfigContext.getInstance()
                        .getRpcInfos()
                        .get("master").getPort())
                        .addService(new MasterReceiveServiceImpl(this))
                        .build();
            } catch (Exception e) {
                LOGGER.error("create grpc error: {}", e.toString());
            }

        }
    }

    public MasterReceiver register(String name, ICallBack<?> callBack) {
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
    public void mapNotify(MapBackFeedEntry request) {
        LOGGER.info("receive map worker info: {}", request.toString());
        ((ICallBack<MapBackFeedEntry>) callBackMap.get(MAP_NOTIFY)).call(request);
    }

    @SuppressWarnings("unchecked")
    public void reduceNotify(ReduceBackFeedEntry request) {
        LOGGER.info("receive reduce worker info: {}", request.toString());
        ((ICallBack<ReduceBackFeedEntry>) callBackMap.get(REDUCE_NOTIFY)).call(request);
    }
}
