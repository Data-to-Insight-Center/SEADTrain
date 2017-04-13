package edu.indiana.sead.client.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by charmadu on 3/24/16.
 */
public class DateTimeUtil {

    //time scales
    private static final String SEC = "second";
    private static final String MIN = "minute";
    private static final String HOUR = "hour";
    private static final String DAY = "day";
    //private static final String WEEK = "week";
    private static final String MONTH = "month";
    private static final String YEAR = "year";

    //seconds per time period
    private static final long SEC_N = 1;
    private static final long MIN_N = 60;
    private static final long HOUR_N = 60*60;
    private static final long DAY_N = 60*60*24;
    //private static final long WEEK_N = 60*60*24*7;
    private static final long MONTH_N = 60*60*24*30;
    private static final long YEAR_N = 60*60*24*30*12;

    //seconds-string time Maps
    private static Map<Long, String> secondsToString = new HashMap<Long, String>();
    private static Map<String, Long> stringToSeconds = new HashMap<String, Long>();

    private static final double MARGIN = 1.5;

    private static SimpleDateFormat XKeySecondFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat XKeyMinuteFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static SimpleDateFormat XKeyHourFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static SimpleDateFormat XKeyDayFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat XKeyMonthFormat = new SimpleDateFormat("yyyy-MM");
    private static SimpleDateFormat XKeyYearFormat = new SimpleDateFormat("yyyy");

    private static int timezoneOffset;

    static {
        secondsToString.put(SEC_N, SEC);
        secondsToString.put(MIN_N, MIN);
        secondsToString.put(HOUR_N, HOUR);
        secondsToString.put(DAY_N, DAY);
        //secondsToString.put(WEEK_N, WEEK);
        secondsToString.put(MONTH_N, MONTH);
        secondsToString.put(YEAR_N, YEAR);

        stringToSeconds.put(SEC, SEC_N);
        stringToSeconds.put(MIN, MIN_N);
        stringToSeconds.put(HOUR, HOUR_N);
        stringToSeconds.put(DAY, DAY_N);
        //stringToSeconds.put(WEEK, WEEK_N);
        stringToSeconds.put(MONTH, MONTH_N);
        stringToSeconds.put(YEAR, YEAR_N);

        timezoneOffset = new Date().getTimezoneOffset()*60;
    }


    public static String getTimeScale(long duration){

        if(duration < MIN_N*MARGIN) {
            return SEC;
        } else if(MIN_N*MARGIN <= duration && duration < HOUR_N*MARGIN) {
            return MIN;
        } else if(HOUR_N*MARGIN <= duration && duration < DAY_N*MARGIN) {
            return HOUR;
        } else if(DAY_N*MARGIN <= duration && duration < MONTH_N*MARGIN) { // *** either WEEK_N
            return DAY;
        //} else if(WEEK_N*MARGIN <= duration && duration < MONTH_N*MARGIN) {
            //return WEEK;
        } else if(MONTH_N*MARGIN <= duration && duration < YEAR_N*MARGIN) {
            return MONTH;
        } else {
            return YEAR;
        }
    }

    public static long getScaledTime(long seconds, String scale) {
        long multiple = stringToSeconds.get(scale);
        if(scale.equals(DAY) || scale.equals(HOUR) || scale.equals(MIN) || scale.equals(SEC)) {
            return (long) Math.floor((seconds - timezoneOffset) * 1.0 / multiple) * multiple;
        } else {
            Calendar date = Calendar.getInstance();
            date.setTime(new Date(seconds*1000));
            date.set(Calendar.DAY_OF_MONTH, 1);
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            if(scale.equals(YEAR)) {
                date.set(Calendar.MONTH, 0);
            }
            return date.getTimeInMillis()/1000;
        }
    }

    public static String getDateTime(long scaledSeconds, String scale) {
        scaledSeconds += timezoneOffset;
        if(scale.equals(SEC)) {
            return XKeySecondFormat.format(new Date(scaledSeconds*1000));
        } else if(scale.equals(MIN)) {
            return XKeyMinuteFormat.format(new Date(scaledSeconds*1000));
        } else if(scale.equals(HOUR)) {
            return XKeyHourFormat.format(new Date(scaledSeconds*1000));
        } else if(scale.equals(DAY)) {
            return XKeyDayFormat.format(new Date(scaledSeconds*1000));
        //} else if(scale.equals(WEEK)) {
            //return XKeyDayFormat.format(new Date(scaledSeconds*1000));
        } else if(scale.equals(MONTH)) {
            return XKeyMonthFormat.format(new Date(scaledSeconds*1000));
        } else {
            return XKeyYearFormat.format(new Date(scaledSeconds*1000));
        }

    }
}
