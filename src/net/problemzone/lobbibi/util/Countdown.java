package net.problemzone.lobbibi.util;

import net.problemzone.lobbibi.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Countdown {

    private static final Set<Integer> EXACT_CHAT_CALLS = new HashSet<>(Arrays.asList(60, 30, 20, 10, 5, 3, 2, 1));
    private static final int XP_BAR_TICK_SPEED = 2;

    private static BukkitTask levelCountdown;
    private static BukkitTask xpBarCountdown;
    private static BukkitTask chatCountdown;


    public static void createLevelCountdown(int seconds, @Nullable Language title) {

        cancelLevelCountdown();

        AtomicInteger level = new AtomicInteger(seconds);

        levelCountdown = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.setLevel(level.get());

                    if (level.get() <= 0) {
                        if (!this.isCancelled()) this.cancel();
                        return;
                    }

                    if (level.get() <= 3) {
                        if(title != null) player.sendTitle(title.getText(), ChatColor.GREEN + "" + player.getLevel(), 0, 20, 0);
                    }
                });
                level.getAndDecrement();
            }
        }.runTaskTimer(Main.getJavaPlugin(), 0, 20);
    }

    public static void createXpBarCountdown(int seconds) {

        cancelXpBarCountdown();

        final float division = XP_BAR_TICK_SPEED / (seconds * 20F);
        AtomicReference<Float> value = new AtomicReference<>((float) 1);

        xpBarCountdown = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (value.get() <= division) {
                        if (!this.isCancelled()) this.cancel();
                        player.setExp(0);
                        return;
                    }
                    player.setExp(value.get());
                });
                value.set(value.get() - division);
            }
        }.runTaskTimer(Main.getJavaPlugin(), 0, XP_BAR_TICK_SPEED);
    }

    public static void createChatCountdown(int seconds, Language text) {

        cancelChatCountdown();

        AtomicInteger remaining = new AtomicInteger(seconds);
        chatCountdown = new BukkitRunnable() {
            @Override
            public void run() {
                if (remaining.get() <= 0) {
                    Bukkit.getOnlinePlayers().forEach(Sounds.CLICK_TIMER_END::playSoundForPlayer);
                    if (!this.isCancelled()) this.cancel();
                    return;
                }
                if (EXACT_CHAT_CALLS.contains(remaining.get())) {
                    Bukkit.broadcastMessage(String.format(text.getFormattedText(), remaining.get()));
                    Bukkit.getOnlinePlayers().forEach(Sounds.CLICK_TIMER::playSoundForPlayer);
                }
                remaining.getAndDecrement();
            }
        }.runTaskTimer(Main.getJavaPlugin(), 0, 20);
    }

    public static void cancelLevelCountdown() {
        if (levelCountdown == null) return;
        if (!levelCountdown.isCancelled()) levelCountdown.cancel();
        Bukkit.getOnlinePlayers().forEach(player -> player.setLevel(0));
    }

    public static void cancelXpBarCountdown() {
        if (xpBarCountdown == null) return;
        if (!xpBarCountdown.isCancelled()) xpBarCountdown.cancel();
        Bukkit.getOnlinePlayers().forEach(player -> player.setExp(0));
    }

    public static void cancelChatCountdown() {
        if (chatCountdown == null) return;
        if (!chatCountdown.isCancelled()) chatCountdown.cancel();
    }

}
