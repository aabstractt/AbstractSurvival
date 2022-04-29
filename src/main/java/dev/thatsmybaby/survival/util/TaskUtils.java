package dev.thatsmybaby.survival.util;

import dev.thatsmybaby.survival.Survival;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskUtils {

    public TaskUtils() {
    }

    public static void run(Runnable runnable) {
        Bukkit.getServer().getScheduler().runTask(Survival.getInstance(), runnable);
    }

    public static void runTimer(Runnable runnable, long delay, long timer) {
        Bukkit.getServer().getScheduler().runTaskTimer(Survival.getInstance(), runnable, delay, timer);
    }

    public static void runTimer(BukkitRunnable runnable, long delay, long timer) {
        runnable.runTaskTimer(Survival.getInstance(), delay, timer);
    }

    public static void runLater(Runnable runnable, long delay) {
        Bukkit.getServer().getScheduler().runTaskLater(Survival.getInstance(), runnable, delay);
    }

    public static void runAsyncLater(Runnable runnable, long delay) {
        Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(Survival.getInstance(), runnable, delay);
    }

    public static void runSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread())
            runnable.run();
        else
            Bukkit.getServer().getScheduler().runTask(Survival.getInstance(), runnable);
    }

    public static void runAsync(Runnable runnable) {
        if (Bukkit.isPrimaryThread())
            Bukkit.getScheduler().runTaskAsynchronously(Survival.getInstance(), runnable);
        else
            runnable.run();
    }
}
