package org.yinan.rpc.service;


import org.junit.Before;
import org.junit.Test;
import org.yinan.config.context.ConfigContext;
import org.yinan.config.entity.message.WorkerInfoDO;
import org.yinan.grpc.HeartBeatInfo;
import org.yinan.grpc.MapRemoteFileEntry;
import org.yinan.grpc.ReduceRemoteEntry;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class MasterNotifyServiceTest {

    private List<MasterNotifyService> notifyServices;

    @Before
    public void setUp() {
        List<WorkerInfoDO> workers = ConfigContext.getInstance().getWorkerInfos();
        notifyServices = new ArrayList<>();
        for (WorkerInfoDO workerInfo : workers) {
            MasterNotifyService notifyService = new MasterNotifyService(workerInfo.getIp(), workerInfo.getPort());
            notifyServices.add(notifyService);
        }
    }

    @Test
    public void notifyMap() {
        for (MasterNotifyService notifyService : notifyServices) {
            MapRemoteFileEntry request = MapRemoteFileEntry
                    .newBuilder()
                    .setFileLocation("/user/test")
                    .setFileName("test.txt")
                    .setRemoteIp("127.0.0.1:8080")
                    .setRemotePort(31001)
                    .build();
            System.out.println(notifyService.notifyMap(request));
        }
        System.out.println("success");
    }

    public @Test
    void notifyReduce() {
        for (MasterNotifyService notifyService : notifyServices) {
            ReduceRemoteEntry reduceRemoteEntry = ReduceRemoteEntry
                    .newBuilder()
                    .addMapDealInfo(MapRemoteFileEntry.newBuilder().setRemoteIp("127.0.0.1").build())
                    .build();
            System.out.println(notifyService.notifyReduce(reduceRemoteEntry));
        }
    }

    @Test
    public void heartBeat() throws InterruptedException {
        for (MasterNotifyService notifyService : notifyServices) {
            for (int i = 0; i < 3; i++) {
                Thread.sleep(3000);
                HeartBeatInfo heartBeatInfo = HeartBeatInfo
                        .newBuilder()
                        .setLastRenewal(1620830341)
                        .setCurrentRenewal(1620830359)
                        .build();
                System.out.println(notifyService.heartBeat(heartBeatInfo));
            }
        }
    }


}