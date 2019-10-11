package com.mobilegenomics.f5n;

import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

public class TimeFormat {

    public static String millisToShortDHMS(long duration) {
        String res = "";
        long hours = TimeUnit.MILLISECONDS.toHours(duration)
                - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
        if (seconds == 0) {
            res = String.format("%02dms", duration);
        } else if (minutes == 0) {
            res = String.format("%02ds", seconds);
        } else if (hours == 0) {
            res = String.format("%02dm:%02ds", minutes, seconds);
        } else {
            res = String.format("%02dh:%02dm:%02ds", hours, minutes, seconds);
        }
        return res;
    }

    public static String millisToDateTime(long date) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(date);
    }

}
