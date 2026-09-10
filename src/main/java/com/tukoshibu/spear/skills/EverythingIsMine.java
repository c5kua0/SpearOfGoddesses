package com.tukoshibu.spear.skills;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.tukoshibu.spear.SpearPlugin;
import com.tukoshibu.spear.managers.SkillManager;

public class EverythingIsMine {

    public static void activate(Player player, SpearPlugin plugin, SkillManager skillManager) {
        int radius = plugin.getConfig().getInt("everything-is-mine.radius", 8);
        int enemySlownessLevel = plugin.getConfig().getInt("everything-is-mine.slowness-level", 3) - 1;
        int enemyWeaknessLevel = plugin.getConfig().getInt("everything-is-mine.weakness-level", 2) - 1;
        int enemyEffectDuration = plugin.getConfig().getInt("everything-is-mine.enemy-effect-duration", 8);
        int strengthLevel = plugin.getConfig().getInt("everything-is-mine.strength-level", 2) - 1;
        int resistanceLevel = plugin.getConfig().getInt("everything-is-mine.resistance-level", 1) - 1;
        int ownerEffectDuration = plugin.getConfig().getInt("everything-is-mine.owner-effect-duration", 8);
        long cooldown = plugin.getConfig().getLong("everything-is-mine.cooldown", 60);

        Location centerLocation = player.getLocation();

        // Affect all entities in radius
        for (LivingEntity entity : centerLocation.getNearbyLivingEntities(radius, radius, radius)) {
            // Don't affect the player themselves
            if (entity.equals(player)) {
                continue;
            }

            // Apply debuffs to enemies
            entity.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOWNESS,
                    enemyEffectDuration * 20,
                    enemySlownessLevel,
                    false,
                    false
            ));

            entity.addPotionEffect(new PotionEffect(
                    PotionEffectType.WEAKNESS,
                    enemyEffectDuration * 20,
                    enemyWeaknessLevel,
                    false,
                    false
            ));
        }

        // Apply buffs to owner
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                ownerEffectDuration * 20,
                strengthLevel,
                false,
                false
        ));

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                ownerEffectDuration * 20,
                resistanceLevel,
                false,
                false
        ));

        // Particle effect
        spawnUltimateParticles(centerLocation, radius);

        // Sound effect
        player.getWorld().playSound(centerLocation, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.0f);

        // Broadcast message
        Bukkit.broadcastMessage("§6§l[GREED] TUKOSHIBU has unleashed EVERYTHING IS MINE!");

        // Cooldown
        skillManager.setUltimateCooldown(player.getUniqueId(), cooldown);

        player.sendMessage("§6Everything is Mine §ractivated!");
    }

    private static void spawnUltimateParticles(Location location, int radius) {
        // Large gold particle explosion
        location.getWorld().spawnParticle(
                Particle.REDSTONE,
                location,
                100,
                1.5,
                1.5,
                1.5,
                new Particle.DustOptions(Color.YELLOW, 1.5f)
        );

        // Ring of particles
        int particleCount = 64;
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            Location particleLocation = location.clone().add(x, 1, z);

            location.getWorld().spawnParticle(
                    Particle.REDSTONE,
                    particleLocation,
                    5,
                    0.2,
                    0.2,
                    0.2,
                    new Particle.DustOptions(Color.YELLOW, 1.2f)
            );
        }
    }
}
