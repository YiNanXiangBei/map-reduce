package org.yinan.config.entity;

import de.beosign.snakeyamlanno.property.YamlProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yinan
 * @date 2021/5/13
 */
@Getter
@Setter
public class SystemConfig {
    private GrpcConfig grpc;

    @YamlProperty(key = "map-reduce")
    public MapReduceConfig mapReduce;
}
