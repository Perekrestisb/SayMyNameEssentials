package com.saymyname.essentials;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;

/**
 * /pregen <radius> [x] [z] - pregenerates chunks around a point.
 * Processes chunks on the server tick to avoid ConcurrentModificationException.
 */
public class CmdPregen extends CommandBase {
    @Override public String getCommandName() { return "pregen"; }
    @Override public String getCommandUsage(ICommandSender s) { return "/pregen <radius> [centerX] [centerZ]"; }
    @Override public int getRequiredPermissionLevel() { return 4; }

    // Pregen state (runs on server tick)
    private static boolean active = false;
    private static int startCX, startCZ, endCX, endCZ;
    private static int curCX, curCZ;
    private static int totalChunks, processedChunks, generatedChunks;
    private static long startTime;
    private static final int CHUNKS_PER_TICK = 5;

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("stop")) {
            if (active) {
                active = false;
                broadcast("\u00a7c[Pregen] Stopped. " + processedChunks + " processed, " + generatedChunks + " new.");
            } else {
                sender.addChatMessage(new ChatComponentText("\u00a7cPregen not running."));
            }
            return;
        }

        if (active) {
            sender.addChatMessage(new ChatComponentText("\u00a7cPregen already running! /pregen stop"));
            return;
        }

        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText("\u00a7c/pregen <radius> [x] [z]  |  /pregen stop"));
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
        active = true;

        broadcast(String.format("\u00a7a[Pregen] Starting: %d chunks, radius %d around (%d, %d)",
                totalChunks, radius, cx, cz));

        // Register tick handler if not already
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !active) return;

        WorldServer world = MinecraftServer.getServer().worldServers[0];

        for (int i = 0; i < CHUNKS_PER_TICK && active; i++) {
            if (curCX > endCX) {
                // Done!
                active = false;
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;

                // Save and unload
                try {
                    world.theChunkProviderServer.unloadQueuedChunks();
                    world.saveAllChunks(true, null);
                } catch (Exception e) {}

                broadcast(String.format(
                    "\u00a7a[Pregen] Done! %d chunks, %d new in %dm %ds. Run 'dynmap fullrender world' to update map.",
                    processedChunks, generatedChunks, elapsed / 60, elapsed % 60));

                // Start dynmap render automatically
                MinecraftServer.getServer().getCommandManager().executeCommand(
                    MinecraftServer.getServer(), "dynmap fullrender world");
                return;
            }

            try {
                if (!world.theChunkProviderServer.chunkExists(curCX, curCZ)) {
                    world.theChunkProviderServer.loadChunk(curCX, curCZ);
                    generatedChunks++;
                }
            } catch (Exception e) {
                // Skip
            }

            processedChunks++;
            curCZ++;
            if (curCZ > endCZ) {
                curCZ = startCZ;
                curCX++;
            }

            // Unload periodically
            if (processedChunks % 200 == 0) {
                world.theChunkProviderServer.unloadQueuedChunks();
            }
        }

        // Progress report every 500 chunks
        if (processedChunks % 500 == 0 && processedChunks > 0) {
            double pct = (processedChunks * 100.0) / totalChunks;
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            double cps = processedChunks * 1.0 / Math.max(elapsed, 1);
            int eta = (int) ((totalChunks - processedChunks) / Math.max(cps, 0.1));

            broadcast(String.format(
                "\u00a7e[Pregen] %.1f%% (%d/%d) | \u00a7a%d new \u00a7e| ~%dm %ds left",
                pct, processedChunks, totalChunks, generatedChunks, eta / 60, eta % 60));
        }
    }

    private void broadcast(String msg) {
        MinecraftServer.getServer().getConfigurationManager()
            .sendChatMsg(new ChatComponentText(msg));
    }
}
