package dev.thatsmybaby.survival.listener;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import dev.thatsmybaby.survival.provider.RedisProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AsyncTabCompleteListener implements Listener {

    @EventHandler
    public void onAsyncTabCompleteEvent(AsyncTabCompleteEvent ev) {
        if (!ev.isCommand()) {
            return;
        }

        String[] split = ev.getBuffer().split(" ");

        if (!Arrays.asList("/tpa", "/tpa", "/tpadeny", "/tp", "/msg", "/message", "/tell", "/w").contains(split[0])) {
            return;
        }

        String targetName = split.length > 1 ? split[1] : "";

        int lastSpaceIndex = targetName.lastIndexOf(' ');
        if (lastSpaceIndex >= 0) {
            targetName = targetName.substring(lastSpaceIndex + 1);
        }

        List<String> list = new ArrayList<>();

        String finalTargetName = targetName;
        RedisProvider.execute(jedis -> {
            for (String redisTargetName : jedis.smembers("players-online")) {
                if ((!finalTargetName.equals("") && !redisTargetName.toLowerCase().startsWith(finalTargetName.toLowerCase())) || list.contains(redisTargetName.toLowerCase())) {
                    continue;
                }

                if (finalTargetName.equals("") && list.size() == 10) {
                    break;
                }

                list.add(redisTargetName);
            }
        });

        ev.setCompletions(list);
    }
}