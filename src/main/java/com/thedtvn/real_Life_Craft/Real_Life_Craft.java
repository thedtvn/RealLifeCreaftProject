package com.thedtvn.real_Life_Craft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class Real_Life_Craft extends JavaPlugin {

    NamespacedKey last_sleep = new NamespacedKey(this, "sleep");

    public static Real_Life_Craft getInstance() {
        return getPlugin(Real_Life_Craft.class);
    }

    public ShapedRecipe createRecipe(String color_string, NamedTextColor color, Material dye) {
        NamespacedKey tailoc = new NamespacedKey(this, "tailoc_"+color.toString());
        ItemStack tailoc_it = new ItemStack(Material.POTION);
        tailoc_it.setAmount(1);
        ItemMeta meta = tailoc_it.getItemMeta();
        meta.displayName(Component.text("Sting "+color_string, color));
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(Component.text("Sting"));
        lore.add(Component.text("Nước tăng lực"));
        meta.lore(lore);
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.EFFICIENCY, 255, true);
        tailoc_it.setItemMeta(meta);

        ShapedRecipe tailoc_bottoe = new ShapedRecipe(tailoc, tailoc_it);
        tailoc_bottoe.shape("sss","sBs","sds");
        tailoc_bottoe.setIngredient('d', dye);
        tailoc_bottoe.setIngredient('s', Material.SUGAR);
        tailoc_bottoe.setIngredient('B', Material.POTION);
        return tailoc_bottoe;
    }

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Player_Events(this), this);

        getServer().addRecipe(createRecipe("Đỏ", NamedTextColor.RED, Material.RED_DYE));
        getServer().addRecipe(createRecipe("Vàng", NamedTextColor.YELLOW, Material.YELLOW_DYE));
        getServer().addRecipe(createRecipe("Xanh", NamedTextColor.BLUE, Material.BLUE_DYE));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
