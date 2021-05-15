package org.yinan.config.entity.message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author yinan
 * @date 2021/5/15
 */
@Setter
@Getter
@ToString
public class WorkerInfoDO {
    private String ip;

    private Integer port;

    private String region;

    private String room;
}