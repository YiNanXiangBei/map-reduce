package org.yinan.config.resolve;

import de.beosign.snakeyamlanno.constructor.AnnotationAwareConstructor;
import org.yaml.snakeyaml.Yaml;
import org.yinan.config.entity.SystemConfig;

import java.io.InputStream;

/**
 * @author yinan
 * @date 2021/5/13
 */
public class ConfigResolver implements IResolver<SystemConfig> {

    private String fileName;

    public ConfigResolver(String fileName) {
        this.fileName = fileName;
    }

    public ConfigResolver() {
    }

    @Override
    public SystemConfig resolve() {
        if (fileName == null || "".equals(fileName.trim())) {
            fileName = "application.yml";
        }
        return resolve(fileName);
    }

    @Override
    public SystemConfig resolve(String fileName) {
        Yaml yaml = new Yaml(new AnnotationAwareConstructor(SystemConfig.class));
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(fileName);
//        Map<String, Object> object = yaml.load(inputStream);
        return yaml.loadAs(inputStream, SystemConfig.class);
    }
}
