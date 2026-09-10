package com.tukoshibu.spear.skills;

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
import com.tukoshibu.spear.listeners.CombatListener;

public class GreedyClaim {

    public static void activate(Player player, SpearPlugin plugin, SkillManager skillManager) {
        int radius = plugin.getConfig().getInt("greedy-claim.radius", 8);
        int slownessLevel = plugin.getConfig().getInt("greedy-claim.slowness-level", 3) - 1;
        int weaknessLevel = plugin.getConfig().getInt("greedy-claim.weakness-level", 3) - 1;
        int effectDuration = plugin.getConfig().getInt("greedy-claim.effect-duration", 8);
        long cooldown = plugin.getConfig().getLong("greedy-claim.cooldown", 20);

        Location playerLocation = player.getLocation();

        // Find nearest valid entity within radius
        LivingEntity target = null;
        double closestDistance = radius;

        for (LivingEntity entity : playerLocation.getNearbyLivingEntities(radius, radius, radius)) {
            // Don't target the player themselves
            if (entity.equals(player)) {
                continue;
            }

            double distance = entity.getLocation().distance(playerLocation);
            if (distance < closestDistance) {
                closestDistance = distance;
                target = entity;
            }
        }

        if (target == null) {
            player.sendMessage("§cNo valid targets within range!");
            return;
        }

        // Apply effects to target
        target.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                effectDuration * 20,
                slownessLevel,
                false,
                false
        ));

        target.addPotionEffect(new PotionEffect(
                PotionEffectType.WEAKNESS,
                effectDuration * 20,
                weaknessLevel,
                false,
                false
        ));

        // Mark target as claimed in CombatListener
        CombatListener combatListener = null;
        for (var listener : plugin.getServer().getPluginManager().getRegistrations(plugin)) {
            if (listener.getListener() instanceof CombatListener) {
                combatListener = (CombatListener) listener.getListener();
                break;
            }
        }

        if (combatListener != null) {
            combatListener.claimTarget(player.getUniqueId(), target.getUniqueId());
        }

        // Particle effect around target
        spawnClaimParticles(target.getLocation());

        // Sound effect
        player.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);

        // Cooldown
        skillManager.setCooldown(player.getUniqueId(), "greedy_claim", cooldown);

        player.sendMessage("§6Target claimed!");
    }

    private static void spawnClaimParticles(Location location) {
        // Spiral particles around the target
        for (double y = 0; y <= 2; y += 0.3) {
            location.getWorld().spawnParticle(
                    Particle.REDSTONE,
                    location.clone().add(0, y, 0),
                    15,
                    0.5,
                    0,
                    0.5,
                    new Particle.DustOptions(Color.OLIVE, 1.0f)
            );
        }
    }
}
