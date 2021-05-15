package org.yinan.config.resolve;

import org.junit.Test;
import org.yinan.config.entity.SystemConfig;

import static org.junit.Assert.*;

/**
 * @author yinan
 * @date 2021/5/13
 */
public class ConfigResolverTest {

    ConfigResolver configResolver = new ConfigResolver();

    @Test
    public void resolve() {
        SystemConfig config = configResolver.resolve();
        assertNotNull(config);
        assertNotNull(config.getGrpc());
        assertNotNull(config.getMapReduce());
        assertEquals("master-to-worker", config.getGrpc().getServices().get(0).getName());
        assertEquals(9999, config.getGrpc().getServices().get(0).getPort());
        assertEquals("127.0.0.1:8080", config.getMapReduce().getMaster().getIp());
        assertEquals(3, config.getMapReduce().getWorkers().size());
        assertEquals("127.0.0.1:9090", config.getMapReduce().getWorkers().get(0).getIp());
        assertEquals("1", config.getMapReduce().getWorkers().get(0).getRegion());
        assertEquals("1", config.getMapReduce().getWorkers().get(0).getRoom());
        assertEquals("127.0.0.1:8989", config.getMapReduce().getSharding().getInfos().get(0).getIp());
        assertEquals("/home/yinan/1.txt", config.getMapReduce().getSharding().getInfos().get(0).getLocation());
    }

    @Test
    public void testResolve() {
    }
}