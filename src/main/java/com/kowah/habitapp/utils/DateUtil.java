package com.kowah.habitapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    final static String defaultPattern = "yyyy/MM/dd hh:mm:ss";

    /**
     * 格式化日期，返回时间戳
     */
    public static long getTimestamp(String dateStr, String pattern) throws ParseException {
        SimpleDateFormat sf = new SimpleDateFormat(pattern, Locale.CHINA);
        return sf.parse(dateStr).getTime();
    }

    /**
     * 格式化日期，返回字符串内容
     */
    public static String formatDate(long timestamp) {
        Date d = new Date(timestamp);
        SimpleDateFormat sf = new SimpleDateFormat(defaultPattern, Locale.CHINA);
        return sf.format(d);
    }

    /**
     * 格式化日期，返回字符串内容
     */
    public static String formatDate(long timestamp, String pattern) {
        Date d = new Date(timestamp);
        SimpleDateFormat sf = new SimpleDateFormat(pattern, Locale.CHINA);
        return sf.format(d);
    }

    /**
     * 获得当前时间戳往前i月份的开始时间戳
     *
     * @param timestamp 时间戳
     * @param i         往前几个月
     * @return
     */
    public static long getMonthBeginTimestamp(long timestamp, int i) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(timestamp));

        c.set(Calendar.MONTH, c.get(Calendar.MONTH) - i);
        //设置为1号,当前日期既为本月第一天
        c.set(Calendar.DAY_OF_MONTH, 1);
        //将小时至0
        c.set(Calendar.HOUR_OF_DAY, 0);
        //将分钟至0
        c.set(Calendar.MINUTE, 0);
        //将秒至0
        c.set(Calendar.SECOND, 0);
        //将毫秒至0
        c.set(Calendar.MILLISECOND, 0);
        // 获取本月第一天的时间戳
        return c.getTimeInMillis();
    }

    /**
     * 获得当前时间戳往后i月份的结束时间戳
     *
     * @param timestamp 月份时间戳
     * @param i         往后几个月
     * @return
     */
    public static long getMonthEndTimestamp(long timestamp, int i) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(timestamp));

        c.set(Calendar.MONTH, c.get(Calendar.MONTH) + i);
        //设置为当月最后一天
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        //将小时至23
        c.set(Calendar.HOUR_OF_DAY, 23);
        //将分钟至59
        c.set(Calendar.MINUTE, 59);
        //将秒至59
        c.set(Calendar.SECOND, 59);
        //将毫秒至999
        c.set(Calendar.MILLISECOND, 999);
        // 获取本月最后一天的时间戳
        return c.getTimeInMillis();
    }

    /**
     * 获得当前时间戳往前i周的周一开始时间戳
     * @param timestamp 时间戳
     * @param i         往前几周
     * @return
     */
    public static long getMondayBeginTimestamp(long timestamp, int i) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(timestamp));

        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK) + Calendar.DAY_OF_WEEK * i;
        c.add(Calendar.DATE, Calendar.MONDAY - dayOfWeek);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /**
     * 获得当前时间戳往前i天的开始时间戳
     *
     * @param timestamp 时间戳
     * @param i         往前几天
     * @return
     */
    public static long getDayBeginTimestamp(long timestamp, int i) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(timestamp));

        c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) - i);
        //将小时至0
        c.set(Calendar.HOUR_OF_DAY, 0);
        //将分钟至0
        c.set(Calendar.MINUTE, 0);
        //将秒至0
        c.set(Calendar.SECOND, 0);
        //将毫秒至0
        c.set(Calendar.MILLISECOND, 0);
        // 获取当天零点的时间戳
        return c.getTimeInMillis();
    }

    /**
     * 判断是否为周一
     * @param timestamp
     * @return
     */
    public static boolean isMonday(long timestamp){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(timestamp));
        return c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
    }

    /**
     * 判断是否为1号
     * @param timestamp
     * @return
     */
    public static boolean isFirstDayOfMonth(long timestamp){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(timestamp));
        return c.get(Calendar.DAY_OF_MONTH) == 1;
    }

    public static void main(String[] args) {
        System.out.println(getDayBeginTimestamp(System.currentTimeMillis(),1));
        System.out.println(getMondayBeginTimestamp(System.currentTimeMillis(),1));
        System.out.println(getMonthBeginTimestamp(System.currentTimeMillis(),1));
        System.out.println(getMonthEndTimestamp(System.currentTimeMillis(),-1));
        System.out.println(isMonday(1561910400000L));
        System.out.println(isFirstDayOfMonth(1561910400000L));
    }

}
