package org.yinan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.map.MapReceiverCallBack;
import org.yinan.reduce.ReduceReceiverCallBack;
import org.yinan.rpc.service.WorkerReceiver;

/**
 * @author yinan
 * @date 2021/5/23
 */
public class ServiceStart {
    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceStart.class);
    public void init() {
        LOGGER.info("init ...");
        WorkerReceiver workerReceiver = new WorkerReceiver();
        workerReceiver
                .register(WorkerReceiver.HEART_BEAT, new HeartBeatCallBack())
                .register(WorkerReceiver.MAP_RECEIVER_SENDER, new MapReceiverCallBack())
                .register(WorkerReceiver.REDUCE_RECEIVER_SENDER, new ReduceReceiverCallBack())
                .finished();
    }


}
