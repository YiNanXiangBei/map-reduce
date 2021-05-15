package org.yinan.config.entity;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author yinan
 * @date 2021/5/13
 */
@Setter
@Getter
public class GrpcConfig {

    private List<ServiceInfo> services;

    @Getter
    @Setter
    public static class ServiceInfo {
        private String name;

        private int port;
    }
}
