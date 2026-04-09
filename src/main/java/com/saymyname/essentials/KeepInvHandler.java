package com.saymyname.essentials;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles per-player keepInventory via PlayerDropsEvent.
 * Stores UUIDs in a simple text file.
 */
public class KeepInvHandler {

    private static final Set<String> keepInvPlayers = new HashSet<String>();
    private static File saveFile;

    public static void init(File configDir) {
        saveFile = new File(configDir, "keepinv.txt");
        load();
        MinecraftForge.EVENT_BUS.register(new KeepInvHandler());
    }

    public static boolean hasKeepInv(String uuid) {
        return keepInvPlayers.contains(uuid);
    }

    public static void addKeepInv(String uuid) {
        keepInvPlayers.add(uuid);
        save();
    }

    public static void removeKeepInv(String uuid) {
        keepInvPlayers.remove(uuid);
        save();
    }

    private static void load() {
        if (saveFile == null || !saveFile.exists()) return;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(saveFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) keepInvPlayers.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void save() {
        if (saveFile == null) return;
        try {
            saveFile.getParentFile().mkdirs();
            PrintWriter writer = new PrintWriter(new FileWriter(saveFile));
            for (String uuid : keepInvPlayers) {
                writer.println(uuid);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancel item drops for players with keepInventory enabled.
     * HIGH priority to run before other mods clear the drops list.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDrops(PlayerDropsEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.entityLiving;
        String uuid = player.getUniqueID().toString();

        if (hasKeepInv(uuid)) {
            // Cancel the drops event — items stay in inventory
            event.setCanceled(true);
        }
    }
}
