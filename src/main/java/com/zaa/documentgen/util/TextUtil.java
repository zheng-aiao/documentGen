package com.zaa.documentgen.util;

import java.io.*;
import java.util.Iterator;
import java.util.Objects;

/**
 * TODO Add class comment here<p/>
 *@version 1.0.0
 *@since 1.0.0
 *@author zhengaiao
 *@history<br/>
 * ver         date      author   desc
 * 1.0.0    2022/4/11      zhengaiao  created<br/>
 *<p/>
 */
public class TextUtil {

    public static final String EMPTY = "";

    public static void write(String file, String content) {
        File wFile = new File(file);
        try (
                //打开文件
                //创建FileWriter
                FileWriter writer = new FileWriter(wFile);
                //使用BufferedWriter加速
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
        ) {
            //写入
            bufferedWriter.write(content);
            //刷新缓冲区
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();// TODO: handle exception
        }
    }

    public static String reader(String file) {
        File myFile = new File(file);
        try (
                //创建FileReader
                FileReader fileReader = new FileReader(myFile);
                //使用BufferedReader加速
                BufferedReader bufferedReader = new BufferedReader(fileReader);
        ) {
            /** 打开文件，逐行读取文本 **/
            String lineString = null;
            StringBuilder stringBuilder = new StringBuilder();
            while ((lineString = bufferedReader.readLine()) != null) {
                stringBuilder.append(lineString);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();// TODO: handle exception
        }
        return null;

    }


    /**
     * 在指定字符串前面添加指定个数的空格
     *
     * @param str
     * @param x
     *
     */
    public static String addBlank(String str, int x) {
        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < x; i++) {
            stringBuilder.append(" ");
        }
        stringBuilder.append(str);
        return stringBuilder.toString();
    }

    /**
     * 连接字符串
     * @param params
     *
     */
    public static String concat(String... params) {
        StringBuilder stringBuilder= new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            stringBuilder.append(params[i]);
        }
        return stringBuilder.toString();
    }


    public static String defaultIfBlank(String type,String defaultVal){
        if(type == null || type.equals("")){
            return defaultVal;
        }else{
            return type;
        }
    }

    public static boolean isBlank(String ref){
        return ref == null || ref.trim().equals("") || ref.trim().equals("null");
    }

    public static String substringBeforeLast(String str, String separator) {
        if (!isEmpty(str) && !isEmpty(separator)) {
            int pos = str.lastIndexOf(separator);
            return pos == -1 ? str : str.substring(0, pos);
        } else {
            return str;
        }
    }
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    public static String join(Iterable<?> iterable, String separator) {
        return iterable == null ? null : join(iterable.iterator(), separator);
    }
    public static String join(Iterator<?> iterator, String separator) {
        if (iterator == null) {
            return null;
        } else if (!iterator.hasNext()) {
            return "";
        } else {
            Object first = iterator.next();
            if (!iterator.hasNext()) {
                return Objects.toString(first, "");
            } else {
                StringBuilder buf = new StringBuilder(256);
                if (first != null) {
                    buf.append(first);
                }

                while(iterator.hasNext()) {
                    if (separator != null) {
                        buf.append(separator);
                    }

                    Object obj = iterator.next();
                    if (obj != null) {
                        buf.append(obj);
                    }
                }

                return buf.toString();
            }
        }
    }


}
