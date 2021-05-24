package org.yinan.rpc.service;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.yinan.grpc.MapBackFeedEntry;
import org.yinan.grpc.MasterReceiveServiceGrpc;
import org.yinan.grpc.ReduceBackFeedEntry;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class WorkerNotifyServiceTest {

    private MasterReceiveServiceGrpc.MasterReceiveServiceBlockingStub blockingStub;

    private WorkerNotifyService notifyService;

    @Before
    public void setUp() {
        notifyService = new WorkerNotifyService();
    }

    @Test
    public void mapNotify() {

    }

    @Test
    public void reduceNotify() {
        ReduceBackFeedEntry backFeedEntry = ReduceBackFeedEntry.newBuilder()
                .setFinished(true)
                .setFileLocation("1.txt")
                .setMessage("success")
                .build();

        System.out.println(notifyService.reduceNotify(backFeedEntry));
    }
}