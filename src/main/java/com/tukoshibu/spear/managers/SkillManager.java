package com.tukoshibu.spear.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.tukoshibu.spear.SpearPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillManager {

    private final SpearPlugin plugin;
    private final Map<UUID, Long> skillCooldowns = new HashMap<>();
    private final Map<UUID, Boolean> greedActive = new HashMap<>();
    private final Map<UUID, String> activeSkillSlot = new HashMap<>();
    private final Map<UUID, Long> ultimateCooldowns = new HashMap<>();

    public SkillManager(SpearPlugin plugin) {
        this.plugin = plugin;
    }

    // Check if player is the owner
    public boolean isOwner(Player player) {
        return player.getName().equalsIgnoreCase(plugin.getOwner());
    }

    // Cooldown management
    public boolean hasCooldown(UUID playerUUID, String skillName) {
        Long cooldownTime = skillCooldowns.get(playerUUID + "_" + skillName);
        if (cooldownTime == null) {
            return false;
        }
        return System.currentTimeMillis() < cooldownTime;
    }

    public void setCooldown(UUID playerUUID, String skillName, long durationSeconds) {
        skillCooldowns.put(playerUUID + "_" + skillName, System.currentTimeMillis() + (durationSeconds * 1000));
    }

    public long getRemainingCooldown(UUID playerUUID, String skillName) {
        Long cooldownTime = skillCooldowns.get(playerUUID + "_" + skillName);
        if (cooldownTime == null) {
            return 0;
        }
        long remaining = (cooldownTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    // Ultimate cooldown management
    public boolean hasUltimateCooldown(UUID playerUUID) {
        Long cooldownTime = ultimateCooldowns.get(playerUUID);
        if (cooldownTime == null) {
            return false;
        }
        return System.currentTimeMillis() < cooldownTime;
    }

    public void setUltimateCooldown(UUID playerUUID, long durationSeconds) {
        ultimateCooldowns.put(playerUUID, System.currentTimeMillis() + (durationSeconds * 1000));
    }

    public long getRemainingUltimateCooldown(UUID playerUUID) {
        Long cooldownTime = ultimateCooldowns.get(playerUUID);
        if (cooldownTime == null) {
            return 0;
        }
        long remaining = (cooldownTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    // Greed skill state management
    public boolean isGreedActive(UUID playerUUID) {
        return greedActive.getOrDefault(playerUUID, false);
    }

    public void setGreedActive(UUID playerUUID, boolean active) {
        greedActive.put(playerUUID, active);
    }

    // Active skill slot tracking
    public void setActiveSkillSlot(UUID playerUUID, String slot) {
        activeSkillSlot.put(playerUUID, slot);
    }

    public String getActiveSkillSlot(UUID playerUUID) {
        return activeSkillSlot.getOrDefault(playerUUID, "");
    }

    public void clearActiveSkillSlot(UUID playerUUID) {
        activeSkillSlot.remove(playerUUID);
    }

    public SpearPlugin getPlugin() {
        return plugin;
    }
}
