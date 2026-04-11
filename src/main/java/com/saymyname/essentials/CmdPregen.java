package com.saymyname.essentials;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;

import java.util.logging.Logger;

/**
 * /pregen <radius> [x] [z] - pregenerates chunks around a point.
 * Runs on server tick, 1 chunk every 2 ticks (gentle mode).
 * Output goes to server log only, not chat.
 */
public class CmdPregen extends CommandBase {
    @Override public String getCommandName() { return "pregen"; }
    @Override public String getCommandUsage(ICommandSender s) { return "/pregen <radius> [centerX] [centerZ]"; }
    @Override public int getRequiredPermissionLevel() { return 4; }

    private static boolean active = false;
    private static int startCX, startCZ, endCX, endCZ;
    private static int curCX, curCZ;
    private static int totalChunks, processedChunks, generatedChunks;
    private static long startTime;
    private static int tickCounter = 0;
    private static boolean registered = false;

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("stop")) {
            if (active) {
                active = false;
                sender.addChatMessage(new ChatComponentText("\u00a7c[Pregen] Stopped. " + processedChunks + "/" + totalChunks + " processed, " + generatedChunks + " new."));
            } else {
                sender.addChatMessage(new ChatComponentText("\u00a7cPregen not running."));
            }
            return;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("status")) {
            if (active) {
                double pct = (processedChunks * 100.0) / totalChunks;
                sender.addChatMessage(new ChatComponentText(String.format(
                    "\u00a7e[Pregen] %.1f%% (%d/%d) | %d new", pct, processedChunks, totalChunks, generatedChunks)));
            } else {
                sender.addChatMessage(new ChatComponentText("\u00a77[Pregen] Not running."));
            }
            return;
        }

        if (active) {
            sender.addChatMessage(new ChatComponentText("\u00a7cPregen running! /pregen stop | /pregen status"));
            return;
        }

        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText("\u00a7c/pregen <radius> [x] [z] | stop | status"));
            return;
        }

        int radius = Integer.parseInt(args[0]);
        WorldServer world = MinecraftServer.getServer().worldServers[0];
        int cx = args.length >= 3 ? Integer.parseInt(args[1]) : world.getWorldInfo().getSpawnX();
        int cz = args.length >= 3 ? Integer.parseInt(args[2]) : world.getWorldInfo().getSpawnZ();

        int chunkRadius = radius / 16;
        startCX = (cx >> 4) - chunkRadius;
        startCZ = (cz >> 4) - chunkRadius;
        endCX = (cx >> 4) + chunkRadius;
        endCZ = (cz >> 4) + chunkRadius;
        curCX = startCX;
        curCZ = startCZ;
        totalChunks = (endCX - startCX + 1) * (endCZ - startCZ + 1);
        processedChunks = 0;
        generatedChunks = 0;
        startTime = System.currentTimeMillis();
        tickCounter = 0;
        active = true;

        sender.addChatMessage(new ChatComponentText(String.format(
            "\u00a7a[Pregen] Starting: %d chunks, radius %d around (%d, %d). Silent mode.",
            totalChunks, radius, cx, cz)));

        if (!registered) {
            FMLCommonHandler.instance().bus().register(this);
            registered = true;
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !active) return;

        // 1 chunk every 2 ticks = 10 chunks/sec (gentle)
        tickCounter++;
        if (tickCounter % 2 != 0) return;

        WorldServer world = MinecraftServer.getServer().worldServers[0];

        if (curCX > endCX) {
            active = false;
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            try {
                world.theChunkProviderServer.unloadQueuedChunks();
                world.saveAllChunks(true, null);
            } catch (Exception e) {}

            // Log only (no chat spam)
            System.out.println(String.format("[Pregen] Done! %d chunks, %d new in %dm %ds",
                processedChunks, generatedChunks, elapsed / 60, elapsed % 60));

            // Auto-start dynmap render
            try {
                MinecraftServer.getServer().getCommandManager().executeCommand(
                    MinecraftServer.getServer(), "dynmap fullrender world");
            } catch (Exception e) {}
            return;
        }

        try {
            if (!world.theChunkProviderServer.chunkExists(curCX, curCZ)) {
                world.theChunkProviderServer.loadChunk(curCX, curCZ);
                generatedChunks++;
            }
        } catch (Exception e) {}

        processedChunks++;
        curCZ++;
        if (curCZ > endCZ) {
            curCZ = startCZ;
            curCX++;
        }

        // Unload periodically
        if (processedChunks % 100 == 0) {
            world.theChunkProviderServer.unloadQueuedChunks();
        }

        // Log progress every 2000 chunks (server log only, not chat)
        if (processedChunks % 2000 == 0 && processedChunks > 0) {
            double pct = (processedChunks * 100.0) / totalChunks;
            System.out.println(String.format("[Pregen] %.1f%% (%d/%d) | %d new",
                pct, processedChunks, totalChunks, generatedChunks));
        }
    }
}
