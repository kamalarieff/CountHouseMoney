package com.mudah.my.utils;

import android.content.Context;

import com.mudah.my.R;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static long getYearAgo(Date date) {
        long diff = System.currentTimeMillis() - date.getTime(); //result in millis

    // AGE = TOTAL in millisecond / Millisecond in second / second in minute / minute in hour / hour in day / day in year
        long agemillis = diff / 1000 / 60 / 60 / 24 / 365;
        return agemillis;
    }

    public static Date getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static long getDaysAgo(Date date) {
        final long diff = getStartOfDay().getTime() - date.getTime();
        if (diff < 0) {
            return 0;
        } else {
            return TimeUnit.MILLISECONDS.toDays(diff) + 1;
        }
    }

    public static String getTimeAgo(Context context, long date) {

        final long diff = System.currentTimeMillis() - date;
        //now you have a date interval representing with milliseconds.
        //you can use this diff to do something like:

        if (TimeUnit.MILLISECONDS.toSeconds(diff) < 60) {
            return context.getString(R.string.time_now);
        } else if (TimeUnit.MILLISECONDS.toMinutes(diff) < 2) {
            return context.getString(R.string.time_last_minute);
        } else if (TimeUnit.MILLISECONDS.toMinutes(diff) < 60) {
            return TimeUnit.MILLISECONDS.toMinutes(diff) + " " + context.getString(R.string.time_min);
        } else if (TimeUnit.MILLISECONDS.toHours(diff) < 2) {
            return context.getString(R.string.time_last_hour);
        } else if (TimeUnit.MILLISECONDS.toHours(diff) < 24) {
            return TimeUnit.MILLISECONDS.toHours(diff) + " " + context.getString(R.string.time_hr);
        } else if (TimeUnit.MILLISECONDS.toDays(diff) < 2) {
            return context.getString(R.string.time_yesterday);
        } else if (TimeUnit.MILLISECONDS.toDays(diff) < 7) {
            return TimeUnit.MILLISECONDS.toDays(diff) + " " + context.getString(R.string.time_day);
        } else if (TimeUnit.MILLISECONDS.toDays(diff) < 14) {
            return context.getString(R.string.time_last_wk);
        } else if (TimeUnit.MILLISECONDS.toDays(diff) < 30) {
            return TimeUnit.MILLISECONDS.toDays(diff) / 7 + " " + context.getString(R.string.time_wk);
        } else if (TimeUnit.MILLISECONDS.toDays(diff) < 60) {
            return context.getString(R.string.time_last_month);
        } else if (TimeUnit.MILLISECONDS.toDays(diff) < 365) {
            return TimeUnit.MILLISECONDS.toDays(diff) / 30 + " " + context.getString(R.string.time_month);
        } else if (TimeUnit.MILLISECONDS.toDays(diff) < 730) {
            return context.getString(R.string.time_last_year);
        } else {
            return TimeUnit.MILLISECONDS.toDays(diff) / 365 + " " + context.getString(R.string.time_year);
        }

    }
}


