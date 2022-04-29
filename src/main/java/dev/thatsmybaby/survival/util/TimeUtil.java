package dev.thatsmybaby.survival.util;

public class TimeUtil {

    public static String format(int seconds) {
        int t = seconds % 3600;
        int m = t / 60;
        int s = t % 60;

        String time = m < 10 ? ("0" + m + ":") : (m + ":");
        time = s < 10 ? (time + "0" + s) : (time + s);
        return time;
    }
}
