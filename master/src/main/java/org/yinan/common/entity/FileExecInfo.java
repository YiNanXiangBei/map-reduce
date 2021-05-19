package org.yinan.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author yinan
 * @date 2021/5/16
 * 文件执行结果记录类
 */
@Getter
@Setter
public class FileExecInfo {
    /**
     * 执行该文件的机器id，也就是ip
     */
    private String runId;

    /**
     * 文件名称，全名称
     */
    private String fileName;

    /**
     * 执行结果
     * success : 成功
     * fail：失败
     */
    private String status;

    /**
     * 花费时间 单位 毫秒
     */
    private Integer execTime;

    /**
     * map节点执行成功之后文件保存点
     */
    private Map<String, String> savePoints;
}
