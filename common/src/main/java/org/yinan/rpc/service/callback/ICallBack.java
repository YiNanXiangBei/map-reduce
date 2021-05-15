package org.yinan.rpc.service.callback;

/**
 * @author yinan
 * @date 2021/5/15
 */
public interface ICallBack<T> {
    void call(T t);
}

