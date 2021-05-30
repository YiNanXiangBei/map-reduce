package org.yinan.statistics.reduce;

import org.yinan.reduce.IReduce;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        Map<String, Object> results = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        for(Map.Entry<String, Object> entry : content.entrySet()) {
            if (keys.contains(entry.getKey())) {
                results.put(entry.getKey(), entry.getValue());
            }
        }
        return results;
    }
}
