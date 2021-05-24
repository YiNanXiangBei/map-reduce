package org.yinan.statistics.reduce;

import org.yinan.reduce.IReduce;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yinan
 * @date 2021/5/24
 */
public class ReduceDemo implements IReduce {
    /**
     * 处理指定keys的单词个数
     * @param content
     * @param keys
     * @return
     */
    @Override
    public Map<String, Object> reduce(Map<String, Object> content, List<String> keys) {
        Map<String, Object> results = new HashMap<>();
        for(Map.Entry<String, Object> entry : content.entrySet()) {
            if (keys.contains(entry.getKey())) {
                results.put(entry.getKey(), entry.getValue());
            }
        }
        return results;
    }
}
