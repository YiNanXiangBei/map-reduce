package org.yinan.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author yinan
 * @date 2021/5/16
 * reduce结果集合
 */
@Getter
@Setter
public class ReduceResultInfo {

    /**
     * 消息key
     */
    private String messageKey;

    /**
     * 结果集
     */
    private List<Object> objects;

}
