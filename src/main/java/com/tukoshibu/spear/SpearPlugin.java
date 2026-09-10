package com.tukoshibu.spear;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;
import com.tukoshibu.spear.listeners.CombatListener;
import com.tukoshibu.spear.listeners.HotbarListener;
import com.tukoshibu.spear.managers.SkillManager;

import java.util.ArrayList;
import java.util.List;

public class SpearPlugin extends JavaPlugin {

    private NamespacedKey spearKey;
    private String owner;
    private String spearName;
    private SkillManager skillManager;

    @Override
    public void onEnable() {
        // Load configuration
        saveDefaultConfig();
        owner = getConfig().getString("owner", "TUKOSHIBU");
        spearName = ChatColor.translateAlternateColorCodes('&', getConfig().getString("spear-name", "&6&lSpear of Goddesses"));

        // Initialize persistent data key
        spearKey = new NamespacedKey(this, "spear_of_goddesses");

        // Initialize skill manager
        skillManager = new SkillManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new HotbarListener(this, skillManager), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this, skillManager), this);

        getLogger().info("§6Spear of Goddesses plugin enabled!");
        getLogger().info("§6Owner: " + owner);
    }

    @Override
    public void onDisable() {
        getLogger().info("§6Spear of Goddesses plugin disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("spear")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command!");
                return true;
            }

            Player player = (Player) sender;

            if (!player.getName().equalsIgnoreCase(owner)) {
                player.sendMessage("§cYou are not authorized to use this command!");
                return true;
            }

            ItemStack spear = createSpear();
            player.getInventory().addItem(spear);
            player.sendMessage("§6§lSpear of Goddesses §rhas been added to your inventory!");

            return true;
        }
        return false;
    }

    public ItemStack createSpear() {
        ItemStack spear = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = spear.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(spearName);
            meta.setUnbreakable(true);

            List<String> lore = new ArrayList<>();
            lore.add("§7§oThe weapon of the Sin Archbishop of Greed");
            lore.add("§7§oOwner: " + owner);
            meta.setLore(lore);

            spear.setItemMeta(meta);
        }

        // Mark this item as the Spear of Goddesses using PersistentDataContainer
        ItemMeta itemMeta = spear.getItemMeta();
        if (itemMeta != null) {
            itemMeta.getPersistentDataContainer().set(spearKey, PersistentDataType.BYTE, (byte) 1);
            spear.setItemMeta(itemMeta);
        }

        return spear;
    }

    public boolean isSpear(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }

        return item.getItemMeta().getPersistentDataContainer().has(spearKey, PersistentDataType.BYTE);
    }

    public String getOwner() {
        return owner;
    }

    public String getSpearName() {
        return spearName;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public NamespacedKey getSpearKey() {
        return spearKey;
    }
}
