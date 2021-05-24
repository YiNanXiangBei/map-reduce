package org.yinan.common;

import org.yinan.common.entity.FileExecInfo;
import org.yinan.common.entity.WorkerInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yinan
 * @date 2021/5/16
 */
public class ConfigManager {
    /**
     * map成功执行结果保存处 - 持久化，文件名 SUCCESS_FILE.json
     * key ip::fileLocation
     */
    private final Map<String, FileExecInfo> SUCCESS_FILE = new ConcurrentHashMap<>();

    /**
     *
     * 哪个worker处理哪个文件，这里文件处理成功就会删除，文件名，FILE_DOING_OWNER.json
     * key ip::port value file location
     */
    private final Map<String, String> FILE_DOING_OWNER= new ConcurrentHashMap<>();

    /**
     * 哪个文件被哪个worker处理，文件名 OWNER_DOING_FILE.json
     * key file location , value ip::port
     */
    private final Map<String, String> OWNER_DOING_FILE = new ConcurrentHashMap<>();

    /**
     * 负责处理map工作的节点信息集合 -- 持久化，文件名 MAP_WORKER_INFO.json
     * key ip 其中port为yml配置文件中的端口
     */
    private final Map<String, WorkerInfo> MAP_WORKER_INFO = new ConcurrentHashMap<>();

    /**
     * 负责处理reduce工作的节点信息 -- 持久化，文件名 REDUCE_WORKER_INFO.json
     * key ip
     */
    private final Map<String, WorkerInfo> REDUCE_WORKER_INFO = new ConcurrentHashMap<>();

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
     * 所有成功运行jar包的节点，包含map和reduce节点，文件名 ALIVE_WORKER
     * key ip , value worker info
     */
    private final Map<String, WorkerInfo> ALIVE_WORKER = new ConcurrentHashMap<>();

    /**
     * 记录所有的key信息，key是由用户代码定义的，文件名 ALL_KEYS.json
     */
    private final Set<String> ALL_KEYS = new HashSet<>();

    /**
     * 处理完成reduce任务的机器，文件名 FINISHED_REDUCE_TASK.json
     */
    private final List<String> FINISHED_REDUCE_TASK = new ArrayList<>();

    /**
     * 记录什么reduce处理哪些key，文件名 REDUCE_KEY.json
     */
    private final Map<String, List<String>> REDUCE_KEY = new ConcurrentHashMap<>();

    /**
     * 现场状态快照，帮助崩溃恢复，文件名 SCENE_SNAPSHOT.json
     */
    private final Map<String, Boolean> SCENE_SNAPSHOT = new ConcurrentHashMap<>();


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


    public void addAllReduceWorkers(Map<String, WorkerInfo> workerInfos) {
        REDUCE_WORKER_INFO.putAll(workerInfos);
    }

    public void addReduceWorker(String key, WorkerInfo workerInfo) {
        REDUCE_WORKER_INFO.put(key, workerInfo);
    }

    public Map<String, WorkerInfo> getAllReduceWorkers() {
        return REDUCE_WORKER_INFO;
    }

    public WorkerInfo getReduceWorkerInfo(String key) {
        return REDUCE_WORKER_INFO.get(key);
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

    public void addAllFileOwner(Map<String, String> maps) {
        FILE_DOING_OWNER.putAll(maps);
    }

    public void removeFileOwner(String key) {
        FILE_DOING_OWNER.remove(key);
    }

    public Map<String, String> getAllFileDoingOwner() {
        return FILE_DOING_OWNER;
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

    public void addAllWorkerDoingFiles(Map<String, String> maps) {
        OWNER_DOING_FILE.putAll(maps);
    }

    public void remove(String key) {
        OWNER_DOING_FILE.remove(key);
    }

    public boolean workerDoingFileIsNotEmpty() {
        return !OWNER_DOING_FILE.isEmpty();
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

    public void addAllAliveWorkers(Map<String, WorkerInfo> maps) {
        ALIVE_WORKER.putAll(maps);
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

    public void addAllKeys(List<String> keys) {
        ALL_KEYS.addAll(keys);
    }

    public Set<String> getAllKeys() {
        return ALL_KEYS;
    }

    public void addReduceKey(String key, List<String> keys) {
        REDUCE_KEY.put(key, keys);
    }

    public void addAllReduceKeys(Map<String, List<String>> maps) {
        REDUCE_KEY.putAll(maps);
    }

    public Map<String, List<String>> getAllReduceKeys() {
        return REDUCE_KEY;
    }

    public List<String> getReduceKeys(String key) {
        return REDUCE_KEY.get(key);
    }

    public void removeReduceKey(String key) {
        REDUCE_KEY.remove(key);
    }

    public boolean reduceKeyIsEmpty() {
        return REDUCE_KEY.isEmpty();
    }

    public boolean ipHasDoing(String ip) {
        return REDUCE_KEY.containsKey(ip);
    }

    public void addFinishedReduceTask(String machine) {
        FINISHED_REDUCE_TASK.add(machine);
    }

    public void addAllFinishedReduceTasks(List<String> machines) {
        FINISHED_REDUCE_TASK.addAll(machines);
    }

    public List<String> getAllFinishedReduceTaskMachine() {
        return FINISHED_REDUCE_TASK;
    }

    public synchronized boolean removeFinishedReduceMachine(String key) {
        return FINISHED_REDUCE_TASK.remove(key);
    }

    public void addSceneSnapshot(String key, Boolean status) {
        SCENE_SNAPSHOT.put(key, status);
    }

    public void addAllSceneSnapshot(Map<String, Boolean> snapshots) {
        SCENE_SNAPSHOT.putAll(snapshots);
    }

    public Map<String, Boolean> getAllSceneSnapshot() {
        return SCENE_SNAPSHOT;
    }

    public Boolean getSceneSnapshot(String key) {
        return SCENE_SNAPSHOT.get(key);
    }
}
