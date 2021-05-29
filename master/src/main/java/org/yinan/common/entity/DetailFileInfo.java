package org.yinan.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author yinan
 * @date 2021/5/27
 */
@Setter
@Getter
public class DetailFileInfo {

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件中存在的keys集合
     */
    private List<String> keys;

}
