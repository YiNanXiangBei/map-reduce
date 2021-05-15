package org.yinan.rpc.service;


import io.grpc.Server;
import org.junit.Test;
import org.yinan.grpc.MapBackFeedEntry;
import org.yinan.grpc.ReduceBackFeedEntry;
import org.yinan.rpc.service.callback.ICallBack;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class MasterReceiveServiceImplTest {

    private Server server;

//    @Before
//    public void setUp() throws Exception {
//        this.server = ServerBuilder.forPort(9999)
//                .addService(new MasterReceiveServiceImpl())
//                .build().start();
//        this.server.awaitTermination();
//    }

    @Test
    public void mapNotify() {

    }

    @Test
    public void reduceNotify() {

    }

    @Test
    public void register() {
        MasterReceiver masterReceiver = new MasterReceiver();
        masterReceiver.register(MasterReceiver.MAP_NOTIFY, (ICallBack<MapBackFeedEntry>) System.out::println)
                .register(MasterReceiver.REDUCE_NOTIFY, (ICallBack<ReduceBackFeedEntry>) System.out::println)
                .finished();
    }
}