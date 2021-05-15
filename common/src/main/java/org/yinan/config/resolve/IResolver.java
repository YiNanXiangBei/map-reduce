package org.yinan.config.resolve;

/**
 * @author yinan
 * @date 2021/5/13
 */
public interface IResolver<T> {

    /**
     * 配置文件解析
     * @return
     */
    T resolve();

    /**
     * 配置文件解析
     * @param fileName 文件名
     * @return
     */
    T resolve(String fileName);
}
