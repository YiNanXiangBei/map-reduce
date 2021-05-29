package org.yinan.rpc.service;


import io.grpc.Server;
import org.junit.Before;
import org.junit.Test;
import org.yinan.grpc.HeartBeatInfo;
import org.yinan.grpc.MapRemoteFileEntry;
import org.yinan.grpc.ReduceRemoteEntry;
import org.yinan.rpc.service.callback.ICallBack;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class WorkerReceiveServiceImplTest {

    private Server server;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void mapReceiveSender() {
        new WorkerReceiver();
    }

    @Test
    public void reduceReceiveSender() {
        WorkerReceiver workerReceiver = new WorkerReceiver();
        workerReceiver.register(WorkerReceiver.HEART_BEAT, new ICallBack<HeartBeatInfo>() {
            @Override
            public void call(HeartBeatInfo heartBeatInfo) {
                System.out.println(heartBeatInfo);
            }
        }).register(WorkerReceiver.MAP_RECEIVER_SENDER, new ICallBack<MapRemoteFileEntry>() {
            @Override
            public void call(MapRemoteFileEntry mapRemoteFileEntry) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(mapRemoteFileEntry);
            }
        }).register(WorkerReceiver.REDUCE_RECEIVER_SENDER, new ICallBack<ReduceRemoteEntry>() {
            @Override
            public void call(ReduceRemoteEntry reduceRemoteEntry) {
                System.out.println(reduceRemoteEntry);
            }
        }).finished();
    }

    @Test
    public void heartBeat() {

    }

}