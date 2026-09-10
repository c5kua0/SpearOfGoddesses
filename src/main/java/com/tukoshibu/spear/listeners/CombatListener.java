package com.tukoshibu.spear.listeners;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.tukoshibu.spear.SpearPlugin;
import com.tukoshibu.spear.managers.SkillManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatListener implements Listener {

    private final SpearPlugin plugin;
    private final SkillManager skillManager;
    private final Map<UUID, Long> claimedTargets = new HashMap<>();
    private final Map<UUID, UUID> claimedTargetPlayer = new HashMap<>();

    public CombatListener(SpearPlugin plugin, SkillManager skillManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();

        // Only process if attacker is the owner
        if (!skillManager.isOwner(attacker)) {
            return;
        }

        LivingEntity victim = (LivingEntity) event.getEntity();

        // Don't affect the owner themselves
        if (victim instanceof Player && ((Player) victim).getName().equalsIgnoreCase(plugin.getOwner())) {
            return;
        }

        ItemStack weapon = attacker.getInventory().getItemInMainHand();

        // Check if wielding the Spear
        if (!plugin.isSpear(weapon)) {
            return;
        }

        // Normal Attack - Spear of Goddesses
        handleNormalAttack(attacker, victim);

        // Greed Skill - Steal beneficial effects
        if (skillManager.isGreedActive(attacker.getUniqueId())) {
            handleGreedSteal(attacker, victim);
        }

        // Greedy Claim bonus damage
        if (isTargetClaimed(attacker.getUniqueId(), victim.getUniqueId())) {
            double bonusDamage = plugin.getConfig().getDouble("greedy-claim.bonus-damage", 5.0);
            event.setDamage(event.getDamage() + bonusDamage);
            
            // Visual effect for claimed damage
            spawnClaimedDamageEffect(victim.getLocation());
        }
    }

    private void handleNormalAttack(Player attacker, LivingEntity victim) {
        double baseDamage = plugin.getConfig().getDouble("normal-attack.base-damage", 23.0);
        int poisonDuration = plugin.getConfig().getInt("normal-attack.poison-duration", 4);
        int fireDuration = plugin.getConfig().getInt("normal-attack.fire-duration", 3);
        double divineLightningChance = plugin.getConfig().getDouble("normal-attack.divine-lightning-chance", 0.05);

        // Apply Poison
        victim.addPotionEffect(new PotionEffect(
                PotionEffectType.POISON,
                poisonDuration * 20,
                0,
                false,
                false
        ));

        // Apply Fire
        victim.setFireTicks(fireDuration * 20);

        // Divine Lightning (5% chance)
        if (Math.random() < divineLightningChance) {
            triggerDivineLightning(victim);
        }
    }

    private void triggerDivineLightning(LivingEntity target) {
        Location loc = target.getLocation();

        // Visual lightning effect
        target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 30, 0.5, 1.0, 0.5, 0.1);
        target.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);

        // No additional damage - just visual and sound
    }

    private void handleGreedSteal(Player attacker, LivingEntity victim) {
        // Get all active potion effects
        for (PotionEffect effect : victim.getActivePotionEffects()) {
            PotionEffectType effectType = effect.getType();

            // Don't steal harmful effects
            if (isHarmfulEffect(effectType)) {
                continue;
            }

            // Steal the effect
            int stolenDuration = plugin.getConfig().getInt("greed.stolen-effect-duration", 10);
            attacker.addPotionEffect(new PotionEffect(
                    effectType,
                    stolenDuration * 20,
                    effect.getAmplifier(),
                    false,
                    false
            ));

            // Remove from victim
            victim.removePotionEffect(effectType);

            // Particle effect
            spawnGreedParticles(attacker.getLocation());

            // Only steal one effect per hit
            break;
        }
    }

    private boolean isHarmfulEffect(PotionEffectType type) {
        if (type == null) return false;

        return type == PotionEffectType.POISON ||
               type == PotionEffectType.WITHER ||
               type == PotionEffectType.SLOWNESS ||
               type == PotionEffectType.WEAKNESS ||
               type == PotionEffectType.MINING_FATIGUE ||
               type == PotionEffectType.NAUSEA ||
               type == PotionEffectType.BLINDNESS ||
               type == PotionEffectType.HUNGER ||
               type == PotionEffectType.LEVITATION;
    }

    private void spawnGreedParticles(Location location) {
        location.getWorld().spawnParticle(
                Particle.REDSTONE,
                location,
                20,
                0.5,
                1.0,
                0.5,
                new Particle.DustOptions(Color.YELLOW, 1.0f)
        );
    }

    private void spawnClaimedDamageEffect(Location location) {
        location.getWorld().spawnParticle(
                Particle.REDSTONE,
                location,
                15,
                0.5,
                0.5,
                0.5,
                new Particle.DustOptions(Color.OLIVE, 1.0f)
        );
    }

    public void claimTarget(UUID playerUUID, UUID targetUUID) {
        claimedTargets.put(targetUUID, System.currentTimeMillis());
        claimedTargetPlayer.put(targetUUID, playerUUID);
    }

    public boolean isTargetClaimed(UUID playerUUID, UUID targetUUID) {
        if (!claimedTargets.containsKey(targetUUID)) {
            return false;
        }

        // Check if claim is still valid (8 seconds duration from Greedy Claim config)
        long claimTime = claimedTargets.get(targetUUID);
        int effectDuration = plugin.getConfig().getInt("greedy-claim.effect-duration", 8);
        long expirationTime = claimTime + (effectDuration * 1000);

        if (System.currentTimeMillis() > expirationTime) {
            claimedTargets.remove(targetUUID);
            claimedTargetPlayer.remove(targetUUID);
            return false;
        }

        return claimedTargetPlayer.get(targetUUID).equals(playerUUID);
    }

    public void removeClaimedTarget(UUID targetUUID) {
        claimedTargets.remove(targetUUID);
        claimedTargetPlayer.remove(targetUUID);
    }
}
