package org.yinan.map;

import java.io.InputStream;
import java.util.Map;

/**
 * @author yinan
 * @date 2021/5/23
 * 接口，用户需要自己实现
 */
public interface IMap {

    /**
     * 输入文件流，返回map
     * @param message 这里使用string原因是，考虑到既然已经使用了mapreduce，那么到这里的文件肯定可以读到内存了
     * @return
     */
    Map<String, Object> map(String message);

}
