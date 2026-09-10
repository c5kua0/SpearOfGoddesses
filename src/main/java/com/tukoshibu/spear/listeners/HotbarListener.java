package com.tukoshibu.spear.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.tukoshibu.spear.SpearPlugin;
import com.tukoshibu.spear.managers.SkillManager;
import com.tukoshibu.spear.skills.AuthorityOfGreed;
import com.tukoshibu.spear.skills.GreedyClaim;
import com.tukoshibu.spear.skills.EverythingIsMine;

public class HotbarListener implements Listener {

    private final SpearPlugin plugin;
    private final SkillManager skillManager;

    public HotbarListener(SpearPlugin plugin, SkillManager skillManager) {
        this.plugin = plugin;
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasItem() || event.getClickedBlock() != null) {
            return;
        }

        Player player = event.getPlayer();

        // Only process if player is the owner
        if (!skillManager.isOwner(player)) {
            return;
        }

        // Get the hotbar slot (0-8, where 0 is slot 1, 8 is slot 9)
        int hotbarSlot = player.getInventory().getHeldItemSlot();
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        // Check right-click action
        if (!event.getAction().isRightClick()) {
            return;
        }

        event.setCancelled(true);

        // Slot 5 (index 4) - Spear + Skill 1 (Greed)
        if (hotbarSlot == 4) {
            if (plugin.isSpear(heldItem)) {
                handleGreedToggle(player);
            }
            return;
        }

        // Slot 6 (index 5) - Skill 2 (Authority of Greed)
        if (hotbarSlot == 5) {
            handleAuthorityOfGreed(player);
            return;
        }

        // Slot 7 (index 6) - Skill 3 (Greedy Claim)
        if (hotbarSlot == 6) {
            handleGreedyClaim(player);
            return;
        }

        // Slot 8 (index 7) - Ultimate (Everything is Mine)
        if (hotbarSlot == 7) {
            handleEverythingIsMine(player);
            return;
        }

        // Slot 9 (index 8) - Turn Off Skills
        if (hotbarSlot == 8) {
            handleDisableSkills(player);
            return;
        }
    }

    private void handleGreedToggle(Player player) {
        boolean currentState = skillManager.isGreedActive(player.getUniqueId());
        boolean newState = !currentState;

        skillManager.setGreedActive(player.getUniqueId(), newState);

        if (newState) {
            player.sendMessage("§6§lGreed §rhas been activated!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        } else {
            player.sendMessage("§7§lGreed §rhas been deactivated!");
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        }
    }

    private void handleAuthorityOfGreed(Player player) {
        if (skillManager.hasCooldown(player.getUniqueId(), "authority_of_greed")) {
            long remaining = skillManager.getRemainingCooldown(player.getUniqueId(), "authority_of_greed");
            player.sendMessage("§6Authority of Greed §ris on cooldown for §c" + remaining + "s§r!");
            return;
        }

        AuthorityOfGreed.activate(player, plugin, skillManager);
    }

    private void handleGreedyClaim(Player player) {
        if (skillManager.hasCooldown(player.getUniqueId(), "greedy_claim")) {
            long remaining = skillManager.getRemainingCooldown(player.getUniqueId(), "greedy_claim");
            player.sendMessage("§6Greedy Claim §ris on cooldown for §c" + remaining + "s§r!");
            return;
        }

        GreedyClaim.activate(player, plugin, skillManager);
    }

    private void handleEverythingIsMine(Player player) {
        if (skillManager.hasUltimateCooldown(player.getUniqueId())) {
            long remaining = skillManager.getRemainingUltimateCooldown(player.getUniqueId());
            player.sendMessage("§6Everything is Mine §ris on cooldown for §c" + remaining + "s§r!");
            return;
        }

        EverythingIsMine.activate(player, plugin, skillManager);
    }

    private void handleDisableSkills(Player player) {
        skillManager.setGreedActive(player.getUniqueId(), false);
        player.sendMessage("§7§lGreed skills disabled.");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
    }
}
