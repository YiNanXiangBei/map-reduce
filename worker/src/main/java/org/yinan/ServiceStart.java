package org.yinan;

import org.yinan.map.MapReceiverCallBack;
import org.yinan.reduce.ReduceReceiverCallBack;
import org.yinan.rpc.service.WorkerReceiver;

/**
 * @author yinan
 * @date 2021/5/23
 */
public class ServiceStart {
    public static void main(String[] args) {
        WorkerReceiver workerReceiver = new WorkerReceiver();
        workerReceiver
                .register(WorkerReceiver.HEART_BEAT, new HeartBeatCallBack())
                .register(WorkerReceiver.MAP_RECEIVER_SENDER, new MapReceiverCallBack())
                .register(WorkerReceiver.REDUCE_RECEIVER_SENDER, new ReduceReceiverCallBack())
                .finished();

    }


}
