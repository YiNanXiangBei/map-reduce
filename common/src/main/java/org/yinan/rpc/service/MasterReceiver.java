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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final Executor executor = Executors.newFixedThreadPool(3, new ThreadFactory() {
        private int count = 0;
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("master receiver thread " + count ++);
            return thread;
        }
    });

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
        executor.execute(() -> ((ICallBack<MapBackFeedEntry>) callBackMap.get(MAP_NOTIFY)).call(request));
    }

    @SuppressWarnings("unchecked")
    public void reduceNotify(ReduceBackFeedEntry request) {
        LOGGER.info("receive reduce worker info: {}", request.toString());
        executor.execute(() -> ((ICallBack<ReduceBackFeedEntry>) callBackMap.get(REDUCE_NOTIFY)).call(request));
    }

    public synchronized void stop() {
        if (this.server != null) {
            this.server.shutdown();
        }
    }
}
