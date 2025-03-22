package com.thedtvn.real_Life_Craft;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Real_Life_Craft extends JavaPlugin {

    NamespacedKey last_sleep = new NamespacedKey(this, "sleep");

    public static Real_Life_Craft getInstance() {
        return getPlugin(Real_Life_Craft.class);
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Player_Events(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
