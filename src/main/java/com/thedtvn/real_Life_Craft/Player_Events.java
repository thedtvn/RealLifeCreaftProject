package com.thedtvn.real_Life_Craft;

import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Player_Events implements Listener {

    public static Real_Life_Craft root_plugin;

    public static NamedTextColor[] barColos = {
            NamedTextColor.GREEN,
            NamedTextColor.YELLOW,
            NamedTextColor.RED,
            NamedTextColor.DARK_RED,
            NamedTextColor.DARK_GRAY
    };

    public int day = 24_000;

    public HashMap<Player, Scheduler.Task> player_task = new HashMap<>();

    public Player_Events(Real_Life_Craft main_plugin) {
        root_plugin = main_plugin;
    }

    public void savePlayerSleep(Player player, int last_sleep) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(root_plugin.last_sleep, PersistentDataType.INTEGER, last_sleep);
    }

    private int getSleep(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Integer last_sleep = pdc.get(root_plugin.last_sleep, PersistentDataType.INTEGER);
        if (last_sleep == null) {
            last_sleep = 0;
        }
        return last_sleep;
    }

    public Component createPowerBar(int power) {
        int bar_string_length = 24;
        int max_power = day * 5;
        int bar_power = power < max_power / bar_string_length ? 0 : (int) Math.ceil((double) power / max_power * bar_string_length);
        ComponentBuilder<TextComponent, TextComponent.Builder> bar_str = Component.text("Energy: ", NamedTextColor.WHITE).toBuilder();
        for (int i = 0; i < bar_string_length; i++) {
            if (i > bar_power || bar_power == 0) {
                NamedTextColor color = barColos[Math.min((i * 5) / bar_string_length, 4)];
                Component color_component = Component.text("█", color);
                bar_str.append(color_component);
            } else {
                Component color_component = Component.text("░", NamedTextColor.GRAY);
                bar_str.append(color_component);
            }
        }
        double percent_power = 100.0 - (power * 100.0 / max_power);
        Component color_component = Component.text(" " + String.format("%.1f", percent_power) + "%", NamedTextColor.WHITE);
        bar_str.append(color_component);
        return bar_str.build();
    }

    public int getLastSleep(Player player) {
        int sleep_out;
        int last_sleep = player.getStatistic(Statistic.TIME_SINCE_REST);
        int last_sleep_save = getSleep(player);
        if (last_sleep != 0 && last_sleep > last_sleep_save) {
            sleep_out = last_sleep;
            savePlayerSleep(player, sleep_out);
        } else if (last_sleep_save == 0) {
            sleep_out = last_sleep;
            player.setStatistic(Statistic.TIME_SINCE_REST, sleep_out);
        } else {
            sleep_out = last_sleep_save;
        }
        int last_death = player.getStatistic(Statistic.TIME_SINCE_DEATH);
        return Math.min(sleep_out, last_death);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int player_sleep = getSleep(player);
        player.setStatistic(Statistic.TIME_SINCE_REST, player_sleep);
        Runnable fn = () -> {
            int lowest = getLastSleep(player);
            int effect_time = 20 * 4;
            Component bar = createPowerBar(lowest);
            player.sendActionBar(bar);
            if (lowest > day) {
                int effect_level = lowest - day * 51;
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, effect_time, effect_level));
            }
            if (lowest > day * 2) {
                int effect_level = lowest - (day * 2) * 51;
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, effect_time, effect_level));
            }
            if (lowest > day * 3) {
                int effect_level = lowest - (day * 3) * 51;
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, effect_time, effect_level));
            }
            if (lowest > day * 4) {
                int effect_level = lowest - (day * 4) * 51;
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, effect_time, effect_level));
            }
            if (lowest >= day * 5) {
                player.damage(100);
            }
        };
        long tick = Scheduler.secToTick(1);
        Scheduler.Task task = Scheduler.runTimer(fn, 0, tick);
        player_task.put(player, task);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Scheduler.Task task = player_task.get(player);
        if (task != null) {
            task.cancel();
            player_task.remove(player);
        }
    }

    @EventHandler
    public void onPlayerRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) return;
        int new_power = (int) (getLastSleep(player) + (event.getAmount() * 20) * 10);
        player.setStatistic(Statistic.TIME_SINCE_REST, new_power);
        savePlayerSleep(player, new_power);
    }

    @EventHandler
    public void onPlayerComsumeTaiLoc(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item.getType() != Material.POTION) return;
        if (!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return;
        List<Component> lore = meta.lore();
        if (lore == null) return;
        if (lore.size() < 2) return;
        if (!lore.getFirst().equals(Component.text("Sting"))) return;
        int level = item.getEnchantmentLevel(Enchantment.EFFICIENCY);
        int lowest = getLastSleep(player);
        int add_power = day / 4 * level;
        int new_power = lowest - add_power;
        if (new_power < 0) {
            double panalty = new_power * -1;
            player.damage((panalty / day) * 10);
            new_power = 0;
        }

        savePlayerSleep(player, new_power);
        player.setStatistic(Statistic.TIME_SINCE_REST, new_power);

        // Remove the bad effects first ìf not to low
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.BLINDNESS);

        // Add the good effects
        int shot_time = 20 * 5;
        int long_time = 20 * 10;
        int power_speed = 1;
        if (level > 1) {
            shot_time += level * 2;
            long_time += level * 5;
            power_speed = level * 2;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, shot_time, 255));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, long_time, power_speed));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, long_time, level));
    }

    @EventHandler
    public void onPlayerSleep(PlayerDeepSleepEvent event) {
        Player player = event.getPlayer();
        savePlayerSleep(player, 0);
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.NAUSEA);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
    }

}
