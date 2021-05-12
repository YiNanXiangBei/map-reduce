package org.yinan.rpc.service;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.Before;
import org.junit.Test;
import org.yinan.grpc.HeartBeatInfo;
import org.yinan.grpc.MapRemoteFileEntry;
import org.yinan.grpc.ReduceRemoteEntry;
import org.yinan.grpc.WorkerReceiveServiceGrpc;
import org.yinan.grpc.WorkerReceiveServiceGrpc.WorkerReceiveServiceBlockingStub;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class MasterNotifyServiceTest {

    private WorkerReceiveServiceBlockingStub blockingStub;

    private MasterNotifyService notifyService;

    @Before
    public void before() {
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forAddress("127.0.0.1", 8888)
                .usePlaintext();
        ManagedChannel channel = channelBuilder.build();
        blockingStub = WorkerReceiveServiceGrpc.newBlockingStub(channel);
        notifyService = new MasterNotifyService(blockingStub);
    }

    @Test
    public void notifyMap() {
        MapRemoteFileEntry request = MapRemoteFileEntry
                .newBuilder()
                .setFileLocation("/user/test")
                .setFileName("test.txt")
                .setRemoteIp("127.0.0.1:8080")
                .setRemotePort(31001)
                .build();
        System.out.println(notifyService.notifyMap(request));
    }

    public @Test
    void notifyReduce() {
        ReduceRemoteEntry reduceRemoteEntry = ReduceRemoteEntry
                .newBuilder()
                .putResources("key1", "test1.txt")
                .putResources("key2", "test1.txt")
                .putResources("key3", "test1.txt")
                .build();
        System.out.println(notifyService.notifyReduce(reduceRemoteEntry));
    }

    @Test
    public void heartBeat() throws InterruptedException {
        while (true) {
            Thread.sleep(3000);
            HeartBeatInfo heartBeatInfo = HeartBeatInfo
                    .newBuilder()
                    .setLastRenewal("1620830341")
                    .setCurrentRenewal("1620830359")
                    .build();
            System.out.println(notifyService.heartBeat(heartBeatInfo));
        }
    }


}