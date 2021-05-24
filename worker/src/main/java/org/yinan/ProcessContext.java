package org.yinan;

import org.yinan.map.IMap;
import org.yinan.reduce.IReduce;

/**
 * @author yinan
 * @date 2021/5/23
 */
public class ProcessContext {
    private static IMap map;

    private static IReduce reduce;

    public static IMap getMap() {
        return map;
    }

    public static IReduce getReduce() {
        return reduce;
    }

    public static void registerMap(IMap iMap) {
        map = iMap;
    }

    public static void registerReduce(IReduce iReduce) {
        reduce = iReduce;
    }
}
