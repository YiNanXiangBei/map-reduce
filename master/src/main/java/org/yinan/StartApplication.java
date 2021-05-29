package org.yinan;

import de.beosign.snakeyamlanno.constructor.AnnotationAwareConstructor;
import org.yaml.snakeyaml.Yaml;
import org.yinan.common.ServiceStart;
import org.yinan.config.entity.SystemConfig;

import java.io.InputStream;

/**
 * @author yinan
 * @date 2021/5/16
 * 应用启动类
 */
public class StartApplication {
    public static void main(String[] args) {
        new ServiceStart().init();
    }
}
