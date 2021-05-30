package org.yinan.statistics.map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yinan.io.FileStreamUtil;
import org.yinan.map.IMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yinan
 * @date 2021/5/24
 */
public class MapDemo implements IMap {

    private final static Logger LOGGER = LoggerFactory.getLogger(MapDemo.class);

    /**
     * 统计词频，返回每个单词个数
     * @param message 这里使用string原因是，考虑到既然已经使用了mapreduce，那么到这里的文件肯定可以读到内存了
     * @param fileName 处理之后的文件保存位置，全路径文件名
     * @return key value结构结果
     */
    @Override
    public Map<String, Object> map(String message, String fileName) {
        String[] values = message.split(" ");
        LOGGER.info("get original data, size is: {}", values.length);
        Map<String, Object> results = new HashMap<>();
        for (String str : values) {
            if (StringUtils.isNotBlank(str)) {
                String lowCase = StringUtils.chomp(str).toLowerCase();
                int count = (int) results.getOrDefault(lowCase, 0) + 1;
                results.put(lowCase, count);
            }
        }
        LOGGER.info("save result in {}", fileName);
        FileStreamUtil.save(results, fileName);
        return results;
    }
}
