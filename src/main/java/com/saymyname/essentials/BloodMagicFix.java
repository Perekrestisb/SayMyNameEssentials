package com.saymyname.essentials;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.lang.reflect.Method;

/**
 * Fixes Blood Magic sacrificial dagger for Witchery vampires.
 * Problem: dagger takes blood cells instead of HP for vampires.
 * Fix: when a vampire uses the dagger, deal vanilla damage directly,
 * bypassing Witchery's blood system.
 */
public class BloodMagicFix {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_AIR
            && event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        EntityPlayer player = event.entityPlayer;
        if (player.worldObj.isRemote) return;

        ItemStack held = player.getHeldItem();
        if (held == null) return;

        // Check if holding Blood Magic sacrificial dagger
        String itemClass = held.getItem().getClass().getName();
        if (!itemClass.contains("SacrificialDagger")) return;

        // Check if player is a Witchery vampire
        int vampLevel = getVampireLevel(player);
        if (vampLevel <= 0) return;

        // Cancel the normal dagger action (which takes blood cells)
        event.setCanceled(true);

        // Deal 2 HP (1 heart) of direct damage, bypassing Witchery blood system
        // Use generic damage that Witchery won't intercept
        float currentHP = player.getHealth();
        if (currentHP > 2.0F) {
            player.setHealth(currentHP - 2.0F);

            // Now fill the Blood Altar with LP
            // Try to call Blood Magic's altar fill method
            fillNearbyAltar(player, 200); // 200 LP per stab (same as normal)
        }
    }

    private int getVampireLevel(EntityPlayer player) {
        try {
            Class<?> extClass = Class.forName("com.emoniph.witchery.common.ExtendedPlayer");
            Method getMethod = extClass.getMethod("get", EntityPlayer.class);
            Object extPlayer = getMethod.invoke(null, player);
            if (extPlayer == null) return 0;
            Method getVampLevel = extClass.getMethod("getVampireLevel");
            return (Integer) getVampLevel.invoke(extPlayer);
        } catch (Exception e) {
            return 0;
        }
    }

    private void fillNearbyAltar(EntityPlayer player, int amount) {
        try {
            // Find nearby Blood Altar tile entity and add LP
            Class<?> sacHelperClass = Class.forName(
                "WayofTime.alchemicalWizardry.api.sacrifice.PlayerSacrificeHandler");
            // Try the static method to find and fill altar
            Method findAltar = sacHelperClass.getMethod(
                "findAndFillAltar", net.minecraft.world.World.class,
                EntityPlayer.class, int.class);
            findAltar.invoke(null, player.worldObj, player, amount);
        } catch (Exception e) {
            // If API not available, try alternative
            try {
                Class<?> soulNetworkClass = Class.forName(
                    "WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler");
                Method addBlood = soulNetworkClass.getMethod(
                    "addCurrentEssenceToMaximum",
                    String.class, int.class, int.class);
                addBlood.invoke(null, player.getCommandSenderName(), amount, Integer.MAX_VALUE);
            } catch (Exception e2) {
                // Last resort: just reduce HP without LP fill
            }
        }
    }
}
