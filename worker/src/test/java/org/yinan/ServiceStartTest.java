package org.yinan;

import org.junit.Test;
import org.yinan.map.MapDemo;
import org.yinan.reduce.ReduceDemo;


/**
 * @author yinan
 * @date 2021/5/24
 */
public class ServiceStartTest {

    @Test
    public void init() {
        ProcessContext.registerMap(new MapDemo());
        ProcessContext.registerReduce(new ReduceDemo());
        new ServiceStart().init();
    }
}