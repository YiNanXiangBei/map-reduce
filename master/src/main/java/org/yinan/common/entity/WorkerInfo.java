package org.yinan.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author yinan
 * @date 2021/5/16
 * 执行任务的worker信息
 */
@Getter
@Setter
public class WorkerInfo {
    /**
     * 机器所在区域
     */
    private String region;

    /**
     * 机器所在机房
     */
    private String room;

    /**
     * 机器ip
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 执行的文件名称
     */
    private String fileName;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;


}
