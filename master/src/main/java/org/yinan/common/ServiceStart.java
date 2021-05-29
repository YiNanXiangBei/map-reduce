package org.yinan.common;

import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.common.entity.DetailFileInfo;
import org.yinan.common.entity.FileExecInfo;
import org.yinan.common.entity.WorkerInfo;
import org.yinan.config.context.ConfigContext;
import org.yinan.config.entity.message.FileSystemDO;
import org.yinan.config.entity.message.WorkerInfoDO;
import org.yinan.grpc.DealFile;
import org.yinan.grpc.HeartBeatInfo;
import org.yinan.grpc.MapBackFeedEntry;
import org.yinan.grpc.MapRemoteFileEntry;
import org.yinan.grpc.ReduceBackFeedEntry;
import org.yinan.grpc.ReduceRemoteEntry;
import org.yinan.grpc.ResultInfo;
import org.yinan.io.FileStreamUtil;
import org.yinan.io.ShellUtils;
import org.yinan.rpc.service.MasterNotifyService;
import org.yinan.rpc.service.MasterReceiver;
import org.yinan.rpc.service.callback.ICallBack;
import org.yinan.util.ConvertUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author yinan
 * @date 2021/5/16
 * 应用启动类
 */
public class ServiceStart {

    private final Executor executor;

    /**
     * map节点心跳检测任务开启
     */
    private ScheduledFuture<?> mapScheduleFuture;

    /**
     * reduce节点心跳检测任务开启
     */
    private ScheduledFuture<?> reduceScheduleFuture;

    /**
     * syncsave 线程
     */
    private final ScheduledFuture<?> syncSaveScheduleFuture;

    private final ScheduledExecutorService scheduledExecutorService;

    private final static String FILE_FLAG = "FILE_FLAG";

    private ConfigManager configManager;

    private final static String NONE = "NONE";

    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceStart.class);

    /**
     * 使用telnet命令计算得到的存活的worker节点信息
     */
    private final Map<String, WorkerInfoDO> ALIVE_WORKER = new ConcurrentHashMap<>();

    /**
     * 记录map处理文件结果
     */
    private final List<MapRemoteFileEntry> mapRemoteFileEntries = new CopyOnWriteArrayList<>();
    /**
     * 心跳检测信息记录 -- 持久化
     * 如果出现节点三次没有响应，那么直接将该节点丢弃
     */
    private final Map<String, Long> WORKER_HEART_BEATS_INFOS = new ConcurrentHashMap<>();

    public ServiceStart() {
        executor = new ThreadPoolExecutor(7, 15,
                3000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100), new ThreadFactory() {
            private int count = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("notify worker thread " + count++);
                return thread;
            }
        });
        scheduledExecutorService = Executors.newScheduledThreadPool(3, new ThreadFactory() {
            private int count = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("schedule thread " + count ++);
                return thread;
            }
        });
        syncSaveScheduleFuture = scheduledExecutorService.scheduleAtFixedRate(this::saveSync, 100,
                2000, TimeUnit.MILLISECONDS);
    }

    public void init() {
        try {
            this.configManager = new ConfigManager();
            if (!isOld()) {
                //开启新的任务需要预处理的一些事
                onlyNewTaskInit();
            } else {
                onlyProcessingTaskInit();
            }
            //公共需要处理的任务
            //初始化监听，map或者reduce节点如果完成了任务，那么必须要向master节点返回结果，如果返回失败会一直重试
            initServerListener();
        } catch (Exception e) {
            LOGGER.error("can not start master : {}", e.toString());
        } finally {
            configManager.addSceneSnapshot(Constant.FINISHED_TASK, true);
            //销毁之前创建的文件
            if (!FileStreamUtil.deleteFile(FILE_FLAG)) {
                LOGGER.error("can not clear file: {}", FILE_FLAG);
            } else {
                LOGGER.info("all process has finished !");
            }
        }
    }

    /**
     * 判断项目状态，决定是启动一个新的任务，还是继续旧的任务
     *
     * @return 是否是新任务
     */
    private boolean isOld() throws IOException {
        return FileStreamUtil.isExist(FILE_FLAG);
    }

    /**
     * 新任务初始化，给存活的worker节点分配角色，要么map要么reduce
     */
    private void onlyNewTaskInit() {
        List<WorkerInfoDO> workInfos = ConfigContext.getInstance().getWorkerInfos();
        //telnet检测
        workInfos.forEach(workerInfo -> {
            //注意这里端口写死为22
            boolean success = ShellUtils.telnet(workerInfo.getIp(), 22,
                    5000);
            if (!success) {
                WorkerInfo info = ConvertUtil.convert(workerInfo);
                configManager.addFailedWorker(info);
            } else {
                ALIVE_WORKER.put(workerInfo.getIp(),
                        workerInfo);
            }
        });
        //向所有节点推送jar包，并启动jar包
        for (Map.Entry<String, WorkerInfoDO> entry : ALIVE_WORKER.entrySet()) {
            WorkerInfoDO workerInfoDO = entry.getValue();
            boolean success = false;
            success = ShellUtils.scpUpload(workerInfoDO.getIp(),
                    22,
                    workerInfoDO.getUsername(),
                    workerInfoDO.getPassword(),
                    "/home/yinan/kill.sh",
                    "kill.sh",
                    "sh kill.sh"
                    );
            if (!success) {
                continue;
            }
            //注意这里端口写死为22
            success = ShellUtils.scpUpload(workerInfoDO.getIp(),
                    22,
                    workerInfoDO.getUsername(),
                    workerInfoDO.getPassword(),
                    "/home/yinan/map-reduce.jar",
                    "map-reduce.jar",
                    "java -jar -Duser.host=" + "\"" + workerInfoDO.getIp()+ "\" /home/yinan/map-reduce.jar");
            if (success) {
                configManager.addAliveWorker(entry.getKey(), ConvertUtil.convert(entry.getValue()));
            }
        }
        //获取一批节点处理map任务,这里我们暂定map和reduce任务比例为2：1，如果机器数量小于
        //2:1，那么先执行map任务，再执行reduce任务
        int count = configManager.aliveWorkerSize();
        if (count < 3) {
            //先执行map，再执行reduce
            configManager.addAllMapWorkers(configManager.getAllAliveWorkers());
        } else {
            //按照比例设定执行map和reduce节点比例
            int mapCount = 2 * count / 3;
            Map<String, WorkerInfo> aliveWorkers = configManager.getAllAliveWorkers();
            for (Map.Entry<String, WorkerInfo> entry : aliveWorkers.entrySet()) {
                if (mapCount > 0) {
                    configManager.addMapWorker(entry.getKey(), entry.getValue());
                    mapCount--;
                } else {
                    configManager.addReduceWorker(entry.getKey(), entry.getValue());
                }
            }

        }
        //初始化文件映射
        initFileOwner();
        configManager.addSceneSnapshot(Constant.STARTED_TASK, true);
        //通知map节点执行map任务，map节点执行成功会调用reduce节点任务
        notifyMapNode();
    }

    /**
     * 只有还在处理的任务需要初始化的
     */
    private void onlyProcessingTaskInit() {

        //判断任务是否分配，如果任务还没有分配，执行初始化逻辑
        //任务已经分配，判断节点是否完成执行，若未完成，调用notify执行逻辑
        if (configManager.getSceneSnapshot(Constant.STARTED_TASK) == null) {
            onlyNewTaskInit();
        } else if (configManager.getSceneSnapshot(Constant.FINISHED_TASK) == null) {
            //从文件中读取数据并塞到内存
            load();
            notifyMapNode();
        }
    }


    /**
     * 初始化服务监听，最后初始化
     */
    private void initServerListener() {
        MasterReceiver masterReceiver = new MasterReceiver();
        masterReceiver.register(MasterReceiver.MAP_NOTIFY, new ICallBack<MapBackFeedEntry>() {
            @Override
            public void call(MapBackFeedEntry mapBackFeedEntry) {
                String ip = mapBackFeedEntry.getIp();
                WorkerInfoDO workerInfoDO = ALIVE_WORKER.get(ip);
                //结构：ip::port
                String ipPort = ip + "::" + workerInfoDO.getPort();
                if (mapBackFeedEntry.getSuccess()) {
                    //表示某个map节点处理成功，将结果保存到map中，同时将处理成功的文件清除
                    //该location表示的是文件系统中的location
                    String fileLocation = mapBackFeedEntry.getFileSystemLocation();
                    List<DealFile> dealFileList = mapBackFeedEntry.getDeaFilesList();
                    FileExecInfo execInfo = new FileExecInfo();
                    execInfo.setRunId(ipPort);
                    execInfo.setStatus("success");
                    execInfo.setFileName(fileLocation);
                    execInfo.setExecTime(mapBackFeedEntry.getSpendTime());
                    handleDetailFile(execInfo, mapBackFeedEntry.getDeaFilesList());
                    configManager.addSuccess(ip + "::" + fileLocation, execInfo);
                    //统计key值信息，不过多考虑，仅仅使用逗号分割key
                    List<String> keys = dealFileList
                            .stream()
                            .map(DealFile::getKeysList)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
                    configManager.addAllKeys(keys);
                    dealFileList.forEach(file -> {
                        //记录哪台机器处理之后的文件信息有哪些
                        mapRemoteFileEntries.add(MapRemoteFileEntry
                                .newBuilder()
                                .setRemoteIp(ip)
                                .setRemotePort(22)
                                .setUsername(workerInfoDO.getUsername())
                                .setPassword(workerInfoDO.getPassword())
                                .setFileName(file.getFileName())
                                .build()
                        );
                    });
                    configManager.remove(fileLocation);
                    configManager.removeFileOwner(ipPort);

                } else {
                    checkMapRetryTimes(ipPort);
                }

            }
        }).register(MasterReceiver.REDUCE_NOTIFY, new ICallBack<ReduceBackFeedEntry>() {

            @Override
            public void call(ReduceBackFeedEntry reduceBackFeedEntry) {
                String ip = reduceBackFeedEntry.getIp();
                //输出reduce结果
                if (reduceBackFeedEntry.getFinished()) {
                    System.out.println("reduce node :" + ip + " has finished, " +
                            "file has been saved in remote server: " + reduceBackFeedEntry.getFileLocation());
                    //清除该reduce节点任务
                    configManager.removeReduceKey(ip);
                    //标记该节点已完成任务
                    configManager.addFinishedReduceTask(ip);

                    if (configManager.reduceKeyIsEmpty()) {
                        masterReceiver.stop();
                        syncSaveScheduleFuture.cancel(false);
                    }
                } else {
                    reduceFailDeal(ip);
                }
            }
        }).finished();

    }



    private void initFileOwner() {
        List<FileSystemDO> files = ConfigContext.getInstance().getFileSystems();
        for (FileSystemDO file : files) {
            //哪个文件由哪个worker负责
            configManager.addWorkerDoingFile(file.getIp() + "::" + file.getLocation(), NONE);
        }
    }


    /**
     * 暂时单开线程做
     */
    private void notifyMapNode() {
        //map节点循环检测，判断是否存在未完成任务
        executor.execute(new NotifyMap());
        //心跳检测，每3秒钟检测一次
        mapScheduleFuture = scheduledExecutorService.scheduleAtFixedRate(new MapHeartBeatCheck(), 2000,
                5000, TimeUnit.MILLISECONDS);
    }

    private void notifyReduceNode() {
        //通知reduce节点执行任务
        executor.execute(new NotifyReduce());
        reduceScheduleFuture = scheduledExecutorService.scheduleAtFixedRate(new ReduceHeartBeatCheck(), 2000,
                5000, TimeUnit.MILLISECONDS);
    }

    /**
     * map节点心跳检测
     * @param ipPort
     */
    private void checkMapRetryTimes(String ipPort) {
        int retryTimes = configManager.retryTimes(ipPort);
        if (retryTimes > 3) {
            //将当前节点从存活节点剔除
            String fileInfo = configManager.getFileDoingOwner(ipPort);
            //移除该节点执行文件信息
            configManager.removeFileOwner(ipPort);
            //将其正在执行的文件重置为未执行
            configManager.addWorkerDoingFile(fileInfo, NONE);
            //从存活节点中移除
            configManager.removeMapWorker(ipPort);
        } else {
            configManager.addRetryTimes(ipPort, retryTimes + 1);
        }
    }

    /**
     * reduce节点心跳检测
     */
    private void checkReduceRetryTimes(String ip, Integer port) {
        String ipPort = ip + "::" + port;
        int retryTimes = configManager.retryTimes(ipPort);
        if (retryTimes > 3) {
            //将当前节点从存活节点剔除
            reduceFailDeal(ip);
        } else {
            configManager.addRetryTimes(ipPort, retryTimes + 1);
        }
    }

    /**
     * map节点心跳检测类
     */
    class MapHeartBeatCheck implements Runnable {

        @Override
        public void run() {
            if (configManager.workerDoingFileIsNotEmpty()) {
                //map节点心跳检测
                long currentTime = System.currentTimeMillis();
                configManager.getAllMapWorkers().values().forEach(info -> {
                    String ipPort = info.getIp() + "::" + info.getPort();
                    ResultInfo resultInfo = new MasterNotifyService(info.getIp(), info.getPort())
                            .heartBeat(HeartBeatInfo.newBuilder()
                                    .setCurrentRenewal(currentTime)
                                    .setLastRenewal(lastHeartBeatRenew(ipPort))
                                    .build());
                    //记录最新的心跳时间
                    addHeartBestInfo(ipPort, currentTime);
                    if (!resultInfo.getSuccess()) {
                        //worker节点需要比较master节点上一次发送的时间是否和其接收的时间匹配，如果匹配那么返回true
                        //返回false表示不匹配或者master节点根本连不上，需要记录重试次数，如果超过对应重试次数，那么直接下线
                        checkMapRetryTimes(ipPort);

                    }
                });
            } else {
                //停止定时任务
                mapScheduleFuture.cancel(false);
            }
        }
    }

    /**
     * reduce节点心跳检测
     */
    class ReduceHeartBeatCheck implements Runnable {

        @Override
        public void run() {
            if (!configManager.reduceKeyIsEmpty()) {
                //不为空发送心跳检测，表面reduce任务开始执行
                //map节点心跳检测
                long currentTime = System.currentTimeMillis();
                configManager.getAllReduceWorkers().values().forEach(info -> {
                    String ipPort = info.getIp() + "::" + info.getPort();
                    ResultInfo resultInfo = new MasterNotifyService(info.getIp(), info.getPort())
                            .heartBeat(HeartBeatInfo.newBuilder()
                                    .setCurrentRenewal(currentTime)
                                    .setLastRenewal(lastHeartBeatRenew(ipPort))
                                    .build());
                    //记录最新的心跳时间
                    addHeartBestInfo(ipPort, currentTime);
                    if (!resultInfo.getSuccess()) {
                        //worker节点需要比较master节点上一次发送的时间是否和其接收的时间匹配，如果匹配那么返回true
                        //返回false表示不匹配或者master节点根本连不上，需要记录重试次数，如果超过对应重试次数，那么直接下线
                        checkReduceRetryTimes(info.getIp(), info.getPort());

                    }
                });
            } else {
                //停止定时任务
                reduceScheduleFuture.cancel(false);
            }
        }
    }

    /**
     * map节点分配任务类，需要启动一个线程执行
     */
    class NotifyMap implements Runnable {

        @Override
        public void run() {
            //不为空说明文件没有处理完成
            while (configManager.workerDoingFileIsNotEmpty()) {
                Queue<WorkerInfo> queue = new LinkedList<>(configManager.getAllAliveWorkerList());
                Map<String, String> ownerDoingFiles = configManager.allOwnerDoingFiles();
                Map<String, List<FileSystemDO>> fileSystems = ConfigContext.getInstance().getMapFileSystems();
                for (Map.Entry<String, String> entry : ownerDoingFiles.entrySet()) {
                    if (NONE.equals(entry.getValue())) {
                        //说明该文件还没有被处理
                        WorkerInfo worker = queue.poll();
                        //如果没有节点满足要求，自旋
                        while (worker == null ||
                                configManager.containsFileOwner(worker.getIp() + "::" + worker.getPort())) {
                            queue.offer(worker);
                            worker = queue.poll();
                        }
                        //机器没有正在处理文件
                        FileSystemDO file = fileSystems.get(entry.getKey()).get(0);
                        MapRemoteFileEntry remoteFileEntry = MapRemoteFileEntry
                                .newBuilder()
                                .setFileLocation(file.getLocation())
                                .setRemoteIp(file.getIp())
                                .setRemotePort(file.getPort())
                                .setUsername(file.getUsername())
                                .setPassword(file.getPassword())
                                .build();

                        new MasterNotifyService(worker.getIp(), worker.getPort())
                                .notifyMap(remoteFileEntry);
                        String workerInfo = worker.getIp() + "::" + worker.getPort();
                        String fileInfo = entry.getKey();
                        //哪个文件被哪个worker处理
                        configManager.addWorkerDoingFile(fileInfo, workerInfo);
                        //哪个worker处理哪个文件
                        configManager.addFileOwner(workerInfo, fileInfo);
                        //处理结束放到队列中
                        queue.offer(worker);
                    }

                }
            }
            //说明所有map任务已经执行完成了，可以进行reduce节点的任务处理
            notifyReduceNode();
        }
    }

    /**
     * reduce节点处理任务类
     */
    class NotifyReduce implements Runnable {

        @Override
        public void run() {
            Map<String, WorkerInfo> reduceWorkers = configManager.getAllReduceWorkers();
            if (reduceWorkers.isEmpty()) {
                //说明节点数量过少
                reduceWorkers = configManager.getAllMapWorkers();
            }
            if (reduceWorkers.isEmpty()) {
                throw new RuntimeException("reduce workers is empty!");
            }
            List<String> allKeys = new ArrayList<>(configManager.getAllKeys());
            //分配任务
            int offset = allKeys.size() / reduceWorkers.size();
            int startIndex = 0;
            int remainder = allKeys.size() % reduceWorkers.size();
            //向所有reduce节点发送请求告知分别去这些节点上获取已经分类的数据
            for (Map.Entry<String, WorkerInfo> entry : reduceWorkers.entrySet()) {
                String ip = entry.getKey();
                WorkerInfo workerInfo = entry.getValue();
                List<String> subList;
                if (remainder > 0) {
                    subList = allKeys.subList(startIndex, offset + startIndex + 1);
                    remainder --;
                    startIndex += offset + 1;
                } else {
                    subList = allKeys.subList(startIndex, offset + startIndex);
                    startIndex += offset;
                }
                if (configManager.ipHasDoing(ip)) {
                    continue;
                }
                configManager.addReduceKey(ip, subList);

                ReduceRemoteEntry reduceRemoteEntry = ReduceRemoteEntry.newBuilder()
                        .addAllMapDealInfo(mapRemoteFileEntries)
                        .addAllKeys(subList)
                        .build();
                new MasterNotifyService(workerInfo.getIp(), workerInfo.getPort())
                        .notifyReduce(reduceRemoteEntry);
            }

        }
    }

    private void reduceFailDeal(String oldIp) {
        //换节点重试
        List<String> aliveReduces = configManager.getAllFinishedReduceTaskMachine();
        while (aliveReduces.isEmpty()) {
            try {
                TimeUnit.MILLISECONDS.sleep(3000);
            } catch (InterruptedException e) {
                //异常无所谓
            }
        }
        String newIp = aliveReduces.get(0);
        aliveReduces.remove(newIp);
        List<String> keys = configManager.getReduceKeys(oldIp);
        configManager.addReduceKey(newIp, keys);
        WorkerInfo workerInfo = configManager.getReduceWorkerInfo(newIp);
        ReduceRemoteEntry reduceRemoteEntry = ReduceRemoteEntry.newBuilder()
                .addAllMapDealInfo(mapRemoteFileEntries)
                .build();
        new MasterNotifyService(workerInfo.getIp(), workerInfo.getPort())
                .notifyReduce(reduceRemoteEntry);
        configManager.removeReduceKey(oldIp);
    }


    /**
     * 异步持久化数据
     */
    private void saveSync() {
       boolean result = FileStreamUtil.save(configManager.getAllFileDoingOwner(), Constant.FILE_DOING_OWNER);
       assert result;
       result = FileStreamUtil.save(configManager.getAllSuccess(), Constant.SUCCESS_FILE);
       assert result;
       result = FileStreamUtil.save(configManager.allOwnerDoingFiles(), Constant.OWNER_DOING_FILE);
       assert result;
       result = FileStreamUtil.save(configManager.getAllMapWorkers(), Constant.MAP_WORKER_INFO);
       assert result;
       result = FileStreamUtil.save(configManager.getAllReduceWorkers(), Constant.REDUCE_WORKER_INFO);
       assert result;
       result = FileStreamUtil.save(configManager.getAllAliveWorkers(), Constant.ALIVE_WORKER);
       assert result;
       result = FileStreamUtil.save(configManager.getAllMapKeys(), Constant.ALL_KEYS);
       assert result;
       result = FileStreamUtil.save(configManager.getAllFinishedReduceMachines(), Constant.FINISHED_REDUCE_TASK);
       assert result;
       result = FileStreamUtil.save(configManager.getAllReduceKeys(), Constant.REDUCE_KEY);
       assert result;
       result = FileStreamUtil.save(configManager.getAllSceneSnapshot(), Constant.SCENE_SNAPSHOT);
       assert result;
    }

    /**
     * 加载数据到内存
     */
    private void load() {
        configManager.addAllSuccess(FileStreamUtil.load(new TypeReference<Map<String, FileExecInfo>>() {},
                Constant.SUCCESS_FILE, new ConcurrentHashMap<>()));
        configManager.addAllFileOwner(FileStreamUtil.load(new TypeReference<Map<String, String>>() {},
                Constant.FILE_DOING_OWNER, new ConcurrentHashMap<>()));
        configManager.addAllWorkerDoingFiles(FileStreamUtil.load(new TypeReference<Map<String, String>>() {},
                Constant.OWNER_DOING_FILE, new ConcurrentHashMap<>()));
        configManager.addAllMapWorkers(FileStreamUtil.load(new TypeReference<Map<String, WorkerInfo>>() {},
                Constant.MAP_WORKER_INFO, new ConcurrentHashMap<>()));
        configManager.addAllReduceWorkers(FileStreamUtil.load(new TypeReference<Map<String, WorkerInfo>>() {},
                Constant.REDUCE_WORKER_INFO, new ConcurrentHashMap<>()));
        configManager.addAllAliveWorkers(FileStreamUtil.load(new TypeReference<Map<String, WorkerInfo>>() {},
                Constant.ALIVE_WORKER, new ConcurrentHashMap<>()));
        configManager.addAllMapKeys(FileStreamUtil.load(new TypeReference<Map<String, Set<String>>>() {},
                Constant.ALL_KEYS, new ConcurrentHashMap<>()));
        configManager.addAllFinishedReduceTasks(FileStreamUtil.load(new TypeReference<Map<String, List<String>>>() {},
                Constant.FINISHED_REDUCE_TASK, new ConcurrentHashMap<>()));
        configManager.addAllReduceKeys(FileStreamUtil.load(new TypeReference<Map<String, List<String>>>() {},
                Constant.REDUCE_KEY, new ConcurrentHashMap<>()));
        configManager.addAllSceneSnapshot(FileStreamUtil.load(new TypeReference<Map<String, Boolean>>() {},
                Constant.SCENE_SNAPSHOT, new ConcurrentHashMap<>()));
    }


    private void addHeartBestInfo(String ipInfo, Long lastRenew) {
        WORKER_HEART_BEATS_INFOS.put(ipInfo, lastRenew);
    }

    private Long lastHeartBeatRenew(String ipInfo) {
        return WORKER_HEART_BEATS_INFOS.getOrDefault(ipInfo, 0L);
    }

    private void handleDetailFile(FileExecInfo execInfo, List<DealFile> dealFiles) {
        List<DetailFileInfo> fileInfos = new ArrayList<>();
        dealFiles.forEach(dealFile -> {
            DetailFileInfo fileInfo = new DetailFileInfo();
            fileInfo.setFileName(dealFile.getFileName());
            fileInfo.setKeys(dealFile.getKeysList());
            fileInfos.add(fileInfo);
        });
        execInfo.setDealFiles(fileInfos);
    }

}
