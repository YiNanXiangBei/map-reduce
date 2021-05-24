package org.yinan.statistics.map;

import org.yinan.map.IMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yinan
 * @date 2021/5/24
 */
public class MapDemo implements IMap {

    /**
     * 统计词频，返回每个单词个数
     * @param message 这里使用string原因是，考虑到既然已经使用了mapreduce，那么到这里的文件肯定可以读到内存了
     * @return
     */
    @Override
    public Map<String, Object> map(String message) {
        Map<String, Object> results = new HashMap<>();
        results.put("test", 2);
        results.put("winson", 66);
        results.put("is", 7);
        return results;
    }
}
