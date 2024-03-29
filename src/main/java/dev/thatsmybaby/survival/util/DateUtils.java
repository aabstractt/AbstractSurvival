package dev.thatsmybaby.survival.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils {
    private static Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", 2);

    public static String removeTimePattern(String input) {
        return timePattern.matcher(input).replaceFirst("").trim();
    }

    public static long handleParseTime(String input) {
        if (Character.isLetter(input.charAt(0)))
            return -1L;
        long result = 0L;
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) {
                number.append(c);
            } else {
                String str;
                if (Character.isLetter(c) && !(str = number.toString()).isEmpty()) {
                    result += handleConvert(Integer.parseInt(str), c);
                    number = new StringBuilder();
                }
            }
        }
        return result;
    }

    private static long handleConvert(int value, char charType) {
        switch (charType) {
            case 'y':
                return value * TimeUnit.DAYS.toMillis(365L);
            case 'M':
                return value * TimeUnit.DAYS.toMillis(30L);
            case 'w':
                return value * TimeUnit.DAYS.toMillis(7L);
            case 'd':
                return value * TimeUnit.DAYS.toMillis(1L);
            case 'h':
                return value * TimeUnit.HOURS.toMillis(1L);
            case 'm':
                return value * TimeUnit.MINUTES.toMillis(1L);
            case 's':
                return value * TimeUnit.SECONDS.toMillis(1L);
        }
        return -1L;
    }

    public static String formatTimeMillis(long millis) {
        long seconds = millis / 1000L;
        if (seconds <= 0L)
            return "0 seconds";
        long minutes = seconds / 60L;
        seconds %= 60L;
        long hours = minutes / 60L;
        minutes %= 60L;
        long day = hours / 24L;
        hours %= 24L;
        long years = day / 365L;
        day %= 365L;
        StringBuilder time = new StringBuilder();
        if (years != 0L)
            time.append(years).append((years == 1L) ? " year " : " years ");
        if (day != 0L)
            time.append(day).append((day == 1L) ? " day " : " days ");
        if (hours != 0L)
            time.append(hours).append((hours == 1L) ? " hour " : " hours ");
        if (minutes != 0L)
            time.append(minutes).append((minutes == 1L) ? " minute " : " minutes ");
        if (seconds != 0L)
            time.append(seconds).append((seconds == 1L) ? " second " : " seconds ");
        return time.toString().trim();
    }

    public static long parseTime(String time) {
        long totalTime = 0L;
        boolean found = false;
        Matcher matcher = Pattern.compile("\\d+\\D+").matcher(time);
        while (matcher.find()) {
            String s = matcher.group();
            Long value = Long.valueOf(Long.parseLong(s.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[0]));
            String type = s.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[1];
            switch (type) {
                case "s":
                    totalTime += value.longValue();
                    found = true;
                case "m":
                    totalTime += value.longValue() * 60L;
                    found = true;
                case "h":
                    totalTime += value.longValue() * 60L * 60L;
                    found = true;
                case "d":
                    totalTime += value.longValue() * 60L * 60L * 24L;
                    found = true;
                case "w":
                    totalTime += value.longValue() * 60L * 60L * 24L * 7L;
                    found = true;
                case "mo":
                    totalTime += value.longValue() * 60L * 60L * 24L * 30L;
                    found = true;
                case "y":
                    totalTime += value.longValue() * 60L * 60L * 24L * 365L;
                    found = true;
            }
        }
        return found ? (totalTime * 1000L + 1000L) : -1L;
    }

    public static String getDate(long value) {
        return (new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a")).format(new Date(value));
    }

    public static long parseDateDiff(String time, boolean future) {
        Matcher m = timePattern.matcher(time);
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        boolean found = false;
        while (m.find()) {
            if (m.group() == null || m.group().isEmpty())
                continue;
            for (int c = 0; c < m.groupCount(); ) {
                if (m.group(c) == null || m.group(c).isEmpty()) {
                    c++;
                    continue;
                }
                found = true;
            }
            if (!found)
                continue;
            if (m.group(1) != null && !m.group(1).isEmpty())
                years = Integer.parseInt(m.group(1));
            if (m.group(2) != null && !m.group(2).isEmpty())
                months = Integer.parseInt(m.group(2));
            if (m.group(3) != null && !m.group(3).isEmpty())
                weeks = Integer.parseInt(m.group(3));
            if (m.group(4) != null && !m.group(4).isEmpty())
                days = Integer.parseInt(m.group(4));
            if (m.group(5) != null && !m.group(5).isEmpty())
                hours = Integer.parseInt(m.group(5));
            if (m.group(6) != null && !m.group(6).isEmpty())
                minutes = Integer.parseInt(m.group(6));
            if (m.group(7) == null || m.group(7).isEmpty())
                break;
            seconds = Integer.parseInt(m.group(7));
        }
        if (!found)
            return -5L;
        GregorianCalendar var13 = new GregorianCalendar();
        if (years > 0)
            var13.add(1, years * (future ? 1 : -1));
        if (months > 0)
            var13.add(2, months * (future ? 1 : -1));
        if (weeks > 0)
            var13.add(3, weeks * (future ? 1 : -1));
        if (days > 0)
            var13.add(5, days * (future ? 1 : -1));
        if (hours > 0)
            var13.add(11, hours * (future ? 1 : -1));
        if (minutes > 0)
            var13.add(12, minutes * (future ? 1 : -1));
        if (seconds > 0)
            var13.add(13, seconds * (future ? 1 : -1));
        GregorianCalendar max = new GregorianCalendar();
        max.add(1, 10);
        return var13.after(max) ? max.getTimeInMillis() : var13.getTimeInMillis();
    }

    static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
        int diff = 0;
        long savedDate = fromDate.getTimeInMillis();
        while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate))) {
            savedDate = fromDate.getTimeInMillis();
            fromDate.add(type, future ? 1 : -1);
            diff++;
        }
        fromDate.setTimeInMillis(savedDate);
        return --diff;
    }

    public static String formatDateDiff(long date) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(date);
        GregorianCalendar now = new GregorianCalendar();
        return formatDateDiff(now, c);
    }

    public static String formatDateDiff(Calendar fromDate, Calendar toDate) {
        boolean future = false;
        if (toDate.equals(fromDate))
            return "now";
        if (toDate.after(fromDate))
            future = true;
        StringBuilder sb = new StringBuilder();
        int[] types = { 1, 2, 5, 11, 12, 13 };
        String[] names = {
                "year", "years", "month", "months", "day", "days", "hour", "hours", "minute", "minutes",
                "second", "seconds" };
        int accuracy = 0;
        for (int i = 0; i < types.length && accuracy <= 2; i++) {
            int diff = dateDiff(types[i], fromDate, toDate, future);
            if (diff > 0) {
                accuracy++;
                sb.append(" ").append(diff).append(" ").append(names[i * 2 + ((diff > 1) ? 1 : 0)]);
            }
        }
        return (sb.length() == 0) ? "now" : sb.toString().trim();
    }

    public static String formatSimplifiedDateDiff(long date) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(date);
        GregorianCalendar now = new GregorianCalendar();
        return formatSimplifiedDateDiff(now, c);
    }

    public static String formatSimplifiedDateDiff(Calendar fromDate, Calendar toDate) {
        boolean future = false;
        if (toDate.equals(fromDate))
            return "now";
        if (toDate.after(fromDate))
            future = true;
        StringBuilder sb = new StringBuilder();
        int[] types = { 1, 2, 5, 11, 12, 13 };
        String[] names = {
                "y", "y", "m", "m", "d", "d", "h", "h", "m", "m",
                "s", "s" };
        int accuracy = 0;
        for (int i = 0; i < types.length && accuracy <= 2; i++) {
            int diff = dateDiff(types[i], fromDate, toDate, future);
            if (diff > 0) {
                accuracy++;
                sb.append(" ").append(diff).append("").append(names[i * 2 + ((diff > 1) ? 1 : 0)]);
            }
        }
        return (sb.length() == 0) ? "now" : sb.toString().trim();
    }

    public static String readableTime(long time) {
        int second = 1000;
        int minute = 60 * second;
        int hour = 60 * minute;
        int day = 24 * hour;
        long ms = time;
        StringBuilder text = new StringBuilder("");
        if (time > day) {
            text.append(time / day).append((time / day > 1L) ? "days " : "day ");
            ms = time % day;
        }
        if (ms > hour) {
            text.append(ms / hour).append((ms / hour > 1L) ? "hours " : "hour ");
            ms %= hour;
        }
        if (ms > minute) {
            text.append(ms / minute).append((ms / minute > 1L) ? "minutes " : "minute ");
            ms %= minute;
        }
        if (ms > second)
            text.append(ms / second).append((ms / second > 1L) ? "seconds " : "second ");
        return text.toString();
    }

    public static String readableTime(BigDecimal time) {
        String text = "";
        if (time.doubleValue() <= 60.0D) {
            time = time.add(BigDecimal.valueOf(0.1D));
            return text + " " + time + "s";
        }
        if (time.doubleValue() <= 3600.0D) {
            int minutes = time.intValue() / 60;
            int seconds = time.intValue() % 60;
            DecimalFormat formatter = new DecimalFormat("00");
            return text + " " + formatter.format(minutes) + ":" + formatter.format(seconds) + "m";
        }
        return null;
    }
}
