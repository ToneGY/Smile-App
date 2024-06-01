package com.example.smile.util;

import android.annotation.SuppressLint;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@SuppressLint("SimpleDateFormat")
public class TimeUtil {
    public static String getYear(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy年");
        return dtf.format(LocalDateTime.now());
    }
    public static String getDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM月dd日");
        return dtf.format(LocalDateTime.now());
    }

    public static String getTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        return dtf.format(LocalDateTime.now());
    }

    public static String getCompliteTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy MM-dd HH:mm");
        return dtf.format(LocalDateTime.now());
    }

    public static long getTimeStamp(String completeTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MM-dd HH:mm");
        Date date = new Date();
        try {
            date = simpleDateFormat.parse(completeTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        return date.getTime();
    }

    public static boolean greaterCompare(String l, String r){
        return l.compareTo(r) > 0;
    }

    public static String timeAdd(String l,int day, int hour, int min){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM-dd HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(l, formatter);
        return formatter.format(localDateTime.plusDays(day).plusHours(hour).plusMinutes(min));
    }

    public static String getDateAndTime(String completeTime){
        if(completeTime == null) return "";
        String[] s = completeTime.split(" ");
        return s[1]+" "+s[2];
    }

    public static String normalFormat2MyFormat(String normalFormat){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime localDateTime = LocalDateTime.parse(normalFormat, formatter);

        SimpleDateFormat f2 = new SimpleDateFormat("MM-dd HH:mm");
        return f2.format(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
    }


    public static List<String> findEveryDay(String beginTime, String endTime) {
        //创建一个放所有日期的集合
        List<String> dates = new ArrayList();

        //创建时间解析对象规定解析格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM-dd HH:mm");
        SimpleDateFormat cur_sdf = new SimpleDateFormat("yyyy MM-dd HH:mm");

        //将传入的时间解析成Date类型,相当于格式化
        Date dBegin = null;
        try {
            dBegin = sdf.parse(beginTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //将格式化后的第一天添加进集合
        dates.add(cur_sdf.format(dBegin));

        //使用本地的时区和区域获取日历
        Calendar calBegin = Calendar.getInstance();

        //传入起始时间将此日历设置为起始日历
        calBegin.setTime(dBegin);

        Calendar dEnd = Calendar.getInstance();
        try {
            dEnd.setTime(Objects.requireNonNull(sdf.parse(endTime)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.e("timeutil",dEnd.getTime()+" "+ calBegin.getTime());
        //判断结束日期前一天是否在起始日历的日期之后
        while (dEnd.after(calBegin)) {
            Log.e("timeutil","1");
            //根据日历的规则:月份中的每一天，为起始日历加一天
            calBegin.add(Calendar.DAY_OF_MONTH, 1);

            //得到的每一天就添加进集合
            dates.add(cur_sdf.format(calBegin.getTime()));
            //如果当前的起始日历超过结束日期后,就结束循环
        }
        return dates;
    }
}
