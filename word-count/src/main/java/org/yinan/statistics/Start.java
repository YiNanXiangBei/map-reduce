package org.yinan.statistics;

import org.yinan.ProcessContext;
import org.yinan.ServiceStart;
import org.yinan.statistics.map.MapDemo;
import org.yinan.statistics.reduce.ReduceDemo;

/**
 * @author yinan
 * @date 2021/5/24
 */
public class Start {
    public static void main(String[] args) {
        ProcessContext.registerMap(new MapDemo());
        ProcessContext.registerReduce(new ReduceDemo());
        new ServiceStart().init();
    }
}
