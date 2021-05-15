package org.yinan.config.context;

import org.yinan.config.entity.SystemConfig;
import org.yinan.config.entity.message.FileSystemDO;
import org.yinan.config.entity.message.MasterInfoDO;
import org.yinan.config.entity.message.RpcDO;
import org.yinan.config.entity.message.WorkerInfoDO;
import org.yinan.config.resolve.ConfigResolver;
import org.yinan.config.resolve.IResolver;

import java.util.List;
import java.util.Map;

/**
 * @author yinan
 * @date 2021/5/15
 */
public class ConfigContext {
    private ConfigContext() {
        IResolver<SystemConfig> configResolver = new ConfigResolver();
        SystemConfig systemConfig = configResolver.resolve();
        rpcInfos = ConfigContextUtil.convert2Rpc(systemConfig.getGrpc());
        masterInfo = ConfigContextUtil.convert2Master(systemConfig.getMapReduce().getMaster());
        fileSystems = ConfigContextUtil.convert2FileSystem(systemConfig.getMapReduce().getSharding());
        workerInfos = ConfigContextUtil.convert2WorkerInfo(systemConfig.getMapReduce().getWorkers());
    }

    private Map<String, RpcDO> rpcInfos;

    private MasterInfoDO masterInfo;

    private List<FileSystemDO> fileSystems;

    private List<WorkerInfoDO> workerInfos;

    public Map<String, RpcDO> getRpcInfos() {
        return rpcInfos;
    }

    public MasterInfoDO getMasterInfo() {
        return masterInfo;
    }

    public List<FileSystemDO> getFileSystems() {
        return fileSystems;
    }

    public List<WorkerInfoDO> getWorkerInfos() {
        return workerInfos;
    }

    public static ConfigContext getInstance() {
        return Inner.INSTANCE;
    }

    private static class Inner {
        private final static ConfigContext INSTANCE = new ConfigContext();
    }

}
