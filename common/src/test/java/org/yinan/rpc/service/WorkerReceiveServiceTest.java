package org.yinan.rpc.service;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.Before;
import org.junit.Test;
import org.yinan.rpc.service.rpcImpl.WorkerReceiveService;

import java.io.IOException;

/**
 * @author yinan
 * @date 2021/5/12
 */
public class WorkerReceiveServiceTest  {

    private Server server;

    @Before
    public void before() throws IOException, InterruptedException {
        this.server = ServerBuilder.forPort(8888)
                .addService(new WorkerReceiveService())
                .build().start();
        this.server.awaitTermination();
    }

    @Test
    public void testMapReceiveSender() {

    }

    @Test
    public void testReduceReceiveSender() {
    }

    @Test
    public void testHeartBeat() {
    }

}