package org.yinan.reduce;

import java.util.List;
import java.util.Map;

/**
 * @author yinan
 * @date 2021/5/23
 * 执行reduce逻辑的接口，用户需要自己实现
 */
public interface IReduce {

    /**
     * reduce任务处理
     * @param content 待处理任务
     * @return
     */
    Map<String, Object> reduce(String content, List<String> keys);

}
