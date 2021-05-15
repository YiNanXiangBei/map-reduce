package org.yinan.config.context;

import org.yinan.config.entity.GrpcConfig;
import org.yinan.config.entity.MapReduceConfig;
import org.yinan.config.entity.message.FileSystemDO;
import org.yinan.config.entity.message.MasterInfoDO;
import org.yinan.config.entity.message.RpcDO;
import org.yinan.config.entity.message.WorkerInfoDO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yinan
 * @date 2021/5/15
 */
public class ConfigContextUtil {
    public static List<FileSystemDO> convert2FileSystem(MapReduceConfig.Sharding sharding) {
        List<MapReduceConfig.ShardingInfo> shardingInfos = sharding.getInfos();
        List<FileSystemDO> fileSystemDOS = new ArrayList<>();
        if (shardingInfos == null || shardingInfos.isEmpty()) {
            return fileSystemDOS;
        }
        for (MapReduceConfig.ShardingInfo shardingInfo : shardingInfos) {
            FileSystemDO fileSystem = new FileSystemDO();
            fileSystem.setLocation(shardingInfo.getLocation());
            fileSystem.setPassword(shardingInfo.getPassword());
            fileSystem.setUsername(shardingInfo.getUsername());
            String ip = shardingInfo.getIp();
            String[] ipInfo = ip.split(":");
            fileSystem.setIp(ipInfo[0]);
            fileSystem.setPort(Integer.parseInt(ipInfo[1]));
            fileSystemDOS.add(fileSystem);
        }

        return fileSystemDOS;
    }

    public static MasterInfoDO convert2Master(MapReduceConfig.MasterInfo masterInfo) {
        String ip = masterInfo.getIp();
        String[] ipInfo = ip.split(":");
        MasterInfoDO masterInfoDO = new MasterInfoDO();
        masterInfoDO.setIp(ipInfo[0]);
        masterInfoDO.setPort(Integer.parseInt(ipInfo[1]));
        return masterInfoDO;
    }

    public static Map<String, RpcDO> convert2Rpc(GrpcConfig grpc) {
        List<GrpcConfig.ServiceInfo> serviceInfos = grpc.getServices();
        Map<String, RpcDO> rpcDOS = new HashMap<>();
        for (GrpcConfig.ServiceInfo serviceInfo : serviceInfos) {
            RpcDO rpcDO = new RpcDO();
            rpcDO.setName(serviceInfo.getName());
            rpcDO.setPort(serviceInfo.getPort());
            rpcDOS.put(serviceInfo.getName(), rpcDO);
        }

        return rpcDOS;
    }

    public static List<WorkerInfoDO> convert2WorkerInfo(List<MapReduceConfig.WorkerInfo> workerInfos) {
        List<WorkerInfoDO> workerInfoDOS = new ArrayList<>();
        if (workerInfos == null || workerInfos.isEmpty()) {
            return workerInfoDOS;
        }
        for (MapReduceConfig.WorkerInfo workerInfo : workerInfos) {
            WorkerInfoDO workerInfoDO = new WorkerInfoDO();
            workerInfoDO.setRegion(workerInfo.getRegion());
            workerInfoDO.setRoom(workerInfo.getRoom());
            String ip = workerInfo.getIp();
            String[] ipInfo = ip.split(":");
            workerInfoDO.setIp(ipInfo[0]);
            workerInfoDO.setPort(Integer.parseInt(ipInfo[1]));
            workerInfoDOS.add(workerInfoDO);
        }

        return workerInfoDOS;
    }
}
