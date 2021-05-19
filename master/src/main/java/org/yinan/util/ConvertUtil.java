package org.yinan.util;

import org.yinan.common.entity.WorkerInfo;
import org.yinan.config.entity.message.WorkerInfoDO;

/**
 * @author yinan
 * @date 2021/5/16
 */
public class ConvertUtil {

    public static WorkerInfo convert(WorkerInfoDO workerInfoDO) {
        WorkerInfo workerInfo = new WorkerInfo();
        workerInfo.setPort(workerInfoDO.getPort());
        workerInfo.setIp(workerInfoDO.getIp());
        workerInfo.setRegion(workerInfoDO.getRegion());
        workerInfo.setRoom(workerInfoDO.getRoom());
        workerInfo.setUsername(workerInfoDO.getUsername());
        workerInfo.setPassword(workerInfoDO.getPassword());
        return workerInfo;
    }

}
