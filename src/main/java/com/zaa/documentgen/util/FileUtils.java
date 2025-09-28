package com.zaa.documentgen.util;

import com.zaa.documentgen.exception.BaseException;
import com.zaa.documentgen.resp.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class FileUtils {

    public static File createFile(String path, String name) {
        File file = null;
        try
        {
            file = new File(path, name);
            boolean exists = file.exists();
            if (exists)
            {
                file.delete();
            } else
            {
                if (!file.getParentFile().exists())
                {
                    //创建上级目录
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
        } catch (IOException e)
        {
            log.info("create file fail, path:{}, e:{}", path+name, e);
            throw new BaseException(ResultCodeEnum.RESULT_FAIL_CREATE_FILE);
        }
        return file;
    }

    public static File createFileIfNotExists(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            return file;
        }

        // 创建父目录（如果不存在）
        File parentDir = file.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }

        // 创建文件
        if (file.createNewFile()) {
            return file;
        } else {
            throw new IOException("文件创建失败: " + filePath);
        }
    }
}
