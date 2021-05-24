package org.yinan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.grpc.HeartBeatInfo;
import org.yinan.rpc.service.callback.ICallBack;

/**
 * @author yinan
 * @date 2021/5/23
 */
public class HeartBeatCallBack implements ICallBack<HeartBeatInfo> {

    private final static Logger LOGGER = LoggerFactory.getLogger(HeartBeatCallBack.class);

    @Override
    public void call(HeartBeatInfo heartBeatInfo) {
        LOGGER.info("get heart beat info : {}", heartBeatInfo.toString());
    }
}
