package org.yinan.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.common.entity.FileExecInfo;
import org.yinan.common.entity.WorkerInfo;
import org.yinan.config.context.ConfigContext;
import org.yinan.config.entity.message.FileSystemDO;
import org.yinan.config.entity.message.WorkerInfoDO;
import org.yinan.grpc.HeartBeatInfo;
import org.yinan.grpc.MapBackFeedEntry;
import org.yinan.grpc.MapRemoteFileEntry;
import org.yinan.grpc.ReduceBackFeedEntry;
import org.yinan.grpc.ResultInfo;
import org.yinan.io.FileStreamUtil;
import org.yinan.io.ShellUtils;
import org.yinan.rpc.service.MasterNotifyService;
import org.yinan.rpc.service.MasterReceiver;
import org.yinan.rpc.service.callback.ICallBack;
import org.yinan.util.ConvertUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author yinan
 * @date 2021/5/16
 * 应用启动类
 */
public class ServiceStart {

    private final Executor executor;

    private final ScheduledExecutorService scheduledExecutorService;

    private final static String FILE_FLAG = "FILE_FLAG";

    private final static String MAP_IP = "map_ip";

    private final static String MAP_PORT = "map_port";

    private final static String MAP_FILE_LOCATION = "map_file_location";

    private final static String SPEND_TIME = "spend_time";

    private ConfigManager configManager;

    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceStart.class);

    /**
     * 使用telnet命令计算得到的存活的worker节点信息
     */
    private final Map<String, WorkerInfoDO> ALIVE_WORKER = new ConcurrentHashMap<>();

    /**
     * 心跳检测信息记录 -- 持久化
     * 如果出现节点三次没有响应，那么直接将该节点丢弃
     */
    private final Map<String, Long> WORKER_HEART_BEATS_INFOS = new ConcurrentHashMap<>();

    public ServiceStart() {
        executor = new ThreadPoolExecutor(7, 15,
                3000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100));
        scheduledExecutorService = Executors.newScheduledThreadPool(3);
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
            //销毁之前创建的文件
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
                ALIVE_WORKER.put(workerInfo.getIp() + "::" + workerInfo.getPort(),
                        workerInfo);
            }
        });
        //向所有节点推送jar包，并启动jar包
        for (Map.Entry<String, WorkerInfoDO> entry : ALIVE_WORKER.entrySet()) {
            WorkerInfoDO workerInfoDO = entry.getValue();
            //注意这里端口写死为22
            boolean success = ShellUtils.scpUpload(workerInfoDO.getIp(),
                    22,
                    workerInfoDO.getUsername(),
                    workerInfoDO.getPassword(),
                    "/home/map-reduce.jar",
                    "map-reduce.jar",
                    "java -jar /home/map-reduce.jar");
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
                    mapCount --;
                } else {
                    configManager.addReduceWorker(entry.getValue());
                }
            }

        }
        //初始化文件映射
        initFileOwner();
        //通知map节点执行map任务
        notifyMapNode();
        //通知reduce节点执行reduce任务


    }

    /**
     * 只有还在处理的任务需要初始化的
     */
    private void onlyProcessingTaskInit() {
        //从文件中读取数据并塞到内存
    }


    /**
     * 初始化服务监听，最后初始化
     */
    private void initServerListener() {
        MasterReceiver masterReceiver = new MasterReceiver();
        masterReceiver.register(MasterReceiver.MAP_NOTIFY, new ICallBack<MapBackFeedEntry>() {
            @Override
            public void call(MapBackFeedEntry mapBackFeedEntry) {
                //结构：ip::port
                String ip = mapBackFeedEntry.getSavePointsMap().get(MAP_IP) + "::"
                        + mapBackFeedEntry.getSavePointsMap().get(MAP_PORT);
                if (mapBackFeedEntry.getSuccess()) {
                    //表示某个map节点处理成功，将结果保存到map中，同时将处理成功的文件清除
                    String fileLocation = mapBackFeedEntry.getSavePointsMap().get(MAP_FILE_LOCATION);
                    configManager.remove(fileLocation);
                    configManager.removeFileOwner(ip);
                    int spendTime = Integer.parseInt(mapBackFeedEntry.getSavePointsMap().get(SPEND_TIME));
                    FileExecInfo execInfo = new FileExecInfo();
                    execInfo.setRunId(ip);
                    execInfo.setStatus("success");
                    execInfo.setFileName(fileLocation);
                    execInfo.setSavePoints(mapBackFeedEntry.getSavePointsMap());
                    execInfo.setExecTime(spendTime);
                    configManager.addSuccess(ip + "::" + fileLocation, execInfo);
                } else {
                    checkRetryTimes(ip);
                }



            }
        }).register(MasterReceiver.REDUCE_NOTIFY, new ICallBack<ReduceBackFeedEntry>() {

            @Override
            public void call(ReduceBackFeedEntry reduceBackFeedEntry) {

            }
        }).finished();

    }

    private void initFileOwner() {
        List<FileSystemDO> files = ConfigContext.getInstance().getFileSystems();
        files.forEach(file -> {
            //哪个文件由哪个worker负责
            configManager.addWorkerDoingFile(file.getIp() + "::" + file.getLocation(), null);
        });
    }


    /**
     * 暂时单开线程做
     */
    private void notifyMapNode() {
        executor.execute(new NotifyMap());
        //心跳检测，每3秒钟检测一次
        scheduledExecutorService.scheduleAtFixedRate(new HeartBeatCheck(), 2000,
                3000, TimeUnit.MILLISECONDS);
    }

    private void notifyReduceNode() {

    }

    private void checkRetryTimes(String ipInfo) {
        int retryTimes = configManager.retryTimes(ipInfo);
        if (retryTimes > 3) {
            //将当前节点从存活节点剔除
            String fileInfo = configManager.getFileDoingOwner(ipInfo);
            //移除该节点执行文件信息
            configManager.removeFileOwner(ipInfo);
            //将其正在执行的文件重置为未执行
            configManager.addWorkerDoingFile(fileInfo, null);
            //从存活节点中移除
            configManager.removeMapWorker(ipInfo);
        } else {
            configManager.addRetryTimes(ipInfo, retryTimes + 1);
        }
    }

    /**
     * 心跳检测类
     */
    class HeartBeatCheck implements Runnable {

        @Override
        public void run() {
            //map节点心跳检测
            long currentTime = System.currentTimeMillis();
            configManager.getAllMapWorkers().values().forEach(info -> {
                String ipInfo = info.getIp() + "::" + info.getPort();
                ResultInfo resultInfo = new MasterNotifyService(info.getIp(), info.getPort())
                        .heartBeat(HeartBeatInfo.newBuilder()
                                .setCurrentRenewal(currentTime)
                                .setLastRenewal(lastHeartBeatRenew(ipInfo))
                                .build());
                //记录最新的心跳时间
                addHeartBestInfo(ipInfo, currentTime);
                if (!resultInfo.getSuccess()) {
                    //worker节点需要比较master节点上一次发送的时间是否和其接收的时间匹配，如果匹配那么返回true
                    //返回false表示不匹配或者master节点根本连不上，需要记录重试次数，如果超过对应重试次数，那么直接下线
                    checkRetryTimes(ipInfo);

                }
            });
        }
    }

    /**
     * map节点分配任务类，需要启动一个线程执行
     */
    class NotifyMap implements Runnable {

        @Override
        public void run() {
            //不为空说明文件没有处理完成
            while (!configManager.workerDoingFileIsEmpty()) {
                Queue<WorkerInfo> queue = new LinkedList<>(configManager.getAllAliveWorkerList());
                Map<String, String> ownerDoingFiles = configManager.allOwnerDoingFiles();
                Map<String, List<FileSystemDO>> fileSystems = ConfigContext.getInstance().getMapFileSystems();
                for (Map.Entry<String, String> entry : ownerDoingFiles.entrySet()) {
                    if (entry.getValue() == null) {
                        //说明该文件还没有被处理
                        WorkerInfo worker = queue.poll();
                        if (worker != null &&
                                !configManager.containsFileOwner(worker.getIp() + "::" + worker.getPort())) {
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

                        }
                        queue.offer(worker);
                    }

                }
            }
        }
    }

    /**
     * reduce节点处理任务类
     */
    class NotifyReduce implements Runnable {

        @Override
        public void run() {

        }
    }

    private void addHeartBestInfo(String ipInfo,  Long lastRenew) {
        WORKER_HEART_BEATS_INFOS.put(ipInfo, lastRenew);
    }

    private Long lastHeartBeatRenew(String ipInfo) {
        return WORKER_HEART_BEATS_INFOS.getOrDefault(ipInfo, 0L);
    }



}
