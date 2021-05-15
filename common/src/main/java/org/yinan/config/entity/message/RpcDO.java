package org.yinan.config.entity.message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author yinan
 * @date 2021/5/15
 */
@Getter
@Setter
@ToString
public class RpcDO {
    private String name;

    private Integer port;
}
