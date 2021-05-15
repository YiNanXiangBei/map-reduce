package org.yinan.io;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author yinan
 * @date 2021/5/15
 */
public class SCPUtilsTest {

    @Test
    public void scp() {
        assertTrue(SCPUtils.scp("192.168.1.102", 22, "yinan",
                "Q1w2e3r4t5", "/home/yinan/1.txt", "./1.txt"));
    }
}