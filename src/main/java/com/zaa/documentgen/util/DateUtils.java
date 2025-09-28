package com.zaa.documentgen.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
public class DateUtils {

    /**
     * 获取当前时间
     * @param fmt 时间格式：例如 yyyy-MM-dd HH:mm:ss
     *
     */
    public static String now(String fmt) {
        SimpleDateFormat dateFormat = null;
        if (fmt == null || fmt.equals("")) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }else{
            dateFormat = new SimpleDateFormat(fmt);
        }
        Date date = new Date();
        String format = dateFormat.format(date);
        return format;
    }

    public static String convertDateToString(long time, String format) {
        SimpleDateFormat dateFormat = null;
        if (format == null || format.equals("")) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }else{
            dateFormat = new SimpleDateFormat(format);
        }

        return dateFormat.format(time);
    }

    public static Date convertStringToDate(String dateString, String format) {
        Date date = null;
        SimpleDateFormat dateFormat = null;
        if (format == null || format.equals("")) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
        return date;
    }

    public static void main(String[] args) {
        String dateString = "2023-08-25";
        String format = "yyyy-MM-dd";

        Date date = convertStringToDate(dateString, format);
        System.out.println(date);
    }
}
