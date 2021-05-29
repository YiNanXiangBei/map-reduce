package org.yinan.io;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author yinan
 * @date 2021/5/16
 * 文件生成类
 */
public class FileStreamUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileStreamUtil.class);
    public static <T> boolean save(T t, String fileName) {
        try {
            String jsonString = JSONObject.toJSONString(t);
            File file = new File(fileName);
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file, false), StandardCharsets.UTF_8));
            writer.write(jsonString);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LOGGER.error("can not save file {}, error message is {}", fileName, e.toString());
            return false;
        }

        return true;

    }

    public static <T> T loadObject(Class<T> tClass, String fileName) {
        try {
            String jsonString = readJsonFile(fileName);
            return JSON.parseObject(jsonString, tClass);
        } catch (IOException e) {
            LOGGER.error("load json to object error : {}", e.toString());
        }
        return null;
    }

    public static <T> List<T> loadArray(Class<T> tClass, String fileName) {
        try {
            String jsonString = readJsonFile(fileName);
            return JSON.parseArray(jsonString, tClass);
        } catch (IOException e) {
            LOGGER.error("load json to list error : {}", e.toString());
        }
        return null;
    }

    public static <T> T load(TypeReference<T> typeReference, String fileName, T t) {
        try {
            String jsonString  = readJsonFile(fileName);
            if ("".equals(jsonString)) {
                return t;
            }
            return JSON.parseObject(jsonString, typeReference);
        } catch (Exception e) {
            LOGGER.error("load json to map error: {}", e.toString());
        }
        return null;
    }


    public static String readJsonFile(String fileName) throws IOException {
        String jsonStr;
        File jsonFile = new File(fileName);
        FileReader fileReader = new FileReader(jsonFile);
        Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);
        int ch = 0;
        StringBuilder sb = new StringBuilder();
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        fileReader.close();
        reader.close();
        jsonStr = sb.toString();
        return jsonStr;
    }

    public static boolean isExist(String fileName) throws IOException {
        File file = new File(fileName);
        boolean exist = file.exists();
        if (!exist) {
            //不存在则创建
            boolean create = file.createNewFile();
            if (!create) {
                throw new RuntimeException("can not create file mark !");
            }
        }
        return exist;
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        return file.delete();
    }
}
