package org.yinan.common;

import com.sun.org.apache.regexp.internal.RE;
import org.yinan.common.entity.FileExecInfo;
import org.yinan.common.entity.WorkerInfo;
import org.yinan.config.entity.message.WorkerInfoDO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yinan
 * @date 2021/5/16
 */
public class ConfigManager {
    /**
     * map成功执行结果保存处 - 持久化
     * key ip::fileLocation
     */
    private final Map<String, FileExecInfo> SUCCESS_FILE = new ConcurrentHashMap<>();

    /**
     * 哪个worker处理哪个文件，这里文件处理成功就会删除
     * key ip::port value file location
     */
    private final Map<String, String> FILE_DOING_OWNER= new ConcurrentHashMap<>();

    /**
     * 哪个文件被哪个worker处理
     * key file location , value ip::port
     */
    private final Map<String, String> OWNER_DOING_FILE = new ConcurrentHashMap<>();

    /**
     * 负责处理map工作的节点信息集合 -- 持久化
     * key ip::port 其中port为yml配置文件中的端口
     */
    private final Map<String, WorkerInfo> MAP_WORKER_INFO = new ConcurrentHashMap<>();

    /**
     * 负责处理reduce工作的节点信息 -- 持久化
     */
    private final List<WorkerInfo> REDUCE_WORKER_INFO = new ArrayList<>();

    /**
     * 心跳检测信息记录 -- 持久化
     * 如果出现节点三次没有响应，那么直接将该节点丢弃
     */
    private final Map<String, Integer> RETRY_TIMES = new ConcurrentHashMap<>();

    /**
     * 无法telnet的节点信息
     */
    private final List<WorkerInfo> FAILED_WORKER_INFO = new ArrayList<>();

    /**
     * 所有成功运行jar包的节点，包含map和reduce节点
     */
    private final Map<String, WorkerInfo> ALIVE_WORKER = new ConcurrentHashMap<>();


    public void addAllSuccess(Map<String, FileExecInfo> workerInfoMap) {
        SUCCESS_FILE.putAll(workerInfoMap);
    }

    public void addSuccess(String key, FileExecInfo execInfo) {
        SUCCESS_FILE.put(key, execInfo);
    }

    public Map<String, FileExecInfo> getAllSuccess() {
        return SUCCESS_FILE;
    }

    public FileExecInfo getSuccess(String key) {
        return SUCCESS_FILE.get(key);
    }


    public void addAllMapWorkers(Map<String, WorkerInfo> workerInfoMap) {
        MAP_WORKER_INFO.putAll(workerInfoMap);
    }

    public void addMapWorker(String key, WorkerInfo workerInfo) {
        MAP_WORKER_INFO.put(key, workerInfo);
    }

    public void removeMapWorker(String key) {
        MAP_WORKER_INFO.remove(key);
    }

    public Map<String, WorkerInfo> getAllMapWorkers() {
        return MAP_WORKER_INFO;
    }


    public void addAllReduceWorkers(List<WorkerInfo> workerInfos) {
        REDUCE_WORKER_INFO.addAll(workerInfos);
    }

    public void addReduceWorker(WorkerInfo workerInfo) {
        REDUCE_WORKER_INFO.add(workerInfo);
    }

    public List<WorkerInfo> getAllReduceWorkers() {
        return REDUCE_WORKER_INFO;
    }

    public void addAllFailedWorkers(List<WorkerInfo> workerInfos) {
        FAILED_WORKER_INFO.addAll(workerInfos);
    }

    public void addFailedWorker(WorkerInfo workerInfo) {
        FAILED_WORKER_INFO.add(workerInfo);
    }

    public List<WorkerInfo> getAllFailedWorkers() {
        return FAILED_WORKER_INFO;
    }

    public void addFileOwner(String key, String value) {
        FILE_DOING_OWNER.put(key, value);
    }

    public void removeFileOwner(String key) {
        FILE_DOING_OWNER.remove(key);
    }

    /**
     * 判断某个机器是否正在执行文件
     * @param key 机器信息
     * @return
     */
    public boolean containsFileOwner(String key) {
        return FILE_DOING_OWNER.containsKey(key);
    }

    public void addWorkerDoingFile(String key, String value) {
        OWNER_DOING_FILE.put(key, value);
    }

    public void remove(String key) {
        OWNER_DOING_FILE.remove(key);
    }

    public boolean workerDoingFileIsEmpty() {
        return OWNER_DOING_FILE.isEmpty();
    }

    public String getFileDoingOwner(String key) {
        return OWNER_DOING_FILE.get(key);
    }

    public Map<String, String> allOwnerDoingFiles() {
        return OWNER_DOING_FILE;
    }

    public void addAliveWorker(String key, WorkerInfo workerInfo) {
        ALIVE_WORKER.put(key, workerInfo);
    }

    public void removeNotAlive(String key) {
        ALIVE_WORKER.remove(key);
    }

    public List<WorkerInfo> getAllAliveWorkerList() {
        return new ArrayList<>(ALIVE_WORKER.values());
    }

    public Map<String, WorkerInfo> getAllAliveWorkers() {
        return ALIVE_WORKER;
    }

    public int aliveWorkerSize() {
        return ALIVE_WORKER.size();
    }

    public Integer retryTimes(String ipInfo) {
        return RETRY_TIMES.getOrDefault(ipInfo, 0);
    }

    public void addRetryTimes(String ipInfo, Integer newTimes) {
        RETRY_TIMES.put(ipInfo, newTimes);
    }



}
