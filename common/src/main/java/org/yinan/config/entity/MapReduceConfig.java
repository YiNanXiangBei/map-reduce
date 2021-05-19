package org.yinan.config.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author yinan
 * @date 2021/5/13
 */
@Getter
@Setter
public class MapReduceConfig {

    private MasterInfo master;

    private List<WorkerInfo> workers;

    private Sharding sharding;


    @Getter
    @Setter
    public static class MasterInfo {
        private String ip;
    }


    @Getter
    @Setter
    public static class WorkerInfo {
        private String ip;

        private String region;

        private String room;

        private String username;

        private String password;
    }

    @Getter
    @Setter
    public static class Sharding {
        private List<ShardingInfo> infos;
    }

    @Getter
    @Setter
    public static class ShardingInfo {
        private String ip;

        private String location;

        private String username;

        private String password;
    }

}
