package com.umang.picloc.utility;

import android.content.Context;

import com.umang.picloc.R;

import java.util.Date;

/**
 * Created by umang on 29/12/15.
 */
public class Utility {

    private static final long ONE_MINUTE = 60;
    private static final long ONE_HOUR = 3600;
    private static final long ONE_DAY = 86400;
    private static final long ONE_WEEK = 604800;
    private static final long ONE_YEAR = 31540000;


    public static String getPrettyTime(Context con, Long time) {
        String print;
        Date currentDate = new Date();
        Date thenDate = new Date(time * 1000);

        long diff = (currentDate.getTime() - thenDate.getTime()) / 1000; // in  seconds

        if (diff < ONE_MINUTE) {
            print = diff + con.getString(R.string.short_second);
        } else if (diff < ONE_HOUR) {
            print = ((int) (diff / ONE_MINUTE)) + con.getString(R.string.short_minute);
        } else if (diff < ONE_DAY) {
            print = ((int) (diff / ONE_HOUR)) + con.getString(R.string.short_hour);
        } else if (diff < ONE_WEEK) {
            print = ((int) (diff / ONE_DAY)) + con.getString(R.string.short_day);
        } else if (diff < ONE_YEAR) {
            print = ((int) (diff / ONE_WEEK)) + con.getString(R.string.short_week);
        } else {
            print = ((int) (diff / ONE_YEAR)) + con.getString(R.string.short_year);
        }
        return print;
    }
}
