package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

/**
 * /pregen <radius> [x] [z] - pregenerates chunks around a point.
 * Runs async-style: processes N chunks per tick to avoid freezing.
 */
public class CmdPregen extends CommandBase {
    @Override public String getCommandName() { return "pregen"; }
    @Override public String getCommandUsage(ICommandSender s) { return "/pregen <radius> [centerX] [centerZ]"; }
    @Override public int getRequiredPermissionLevel() { return 4; } // OP only

    private static volatile boolean running = false;
    private static volatile boolean cancel = false;

    @Override
    public void processCommand(final ICommandSender sender, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("stop")) {
            cancel = true;
            sender.addChatMessage(new ChatComponentText("\u00a7ePregen: stopping..."));
            return;
        }

        if (running) {
            sender.addChatMessage(new ChatComponentText("\u00a7cPregen already running! /pregen stop to cancel"));
            return;
        }

        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText("\u00a7c/pregen <radius> [x] [z]  or  /pregen stop"));
            return;
        }

        final int radius = Integer.parseInt(args[0]);
        final WorldServer world = MinecraftServer.getServer().worldServers[0]; // overworld

        // Default center: world spawn
        final int cx = args.length >= 3 ? Integer.parseInt(args[1]) : world.getWorldInfo().getSpawnX();
        final int cz = args.length >= 3 ? Integer.parseInt(args[2]) : world.getWorldInfo().getSpawnZ();

        final int chunkRadius = radius / 16;
        final int totalChunks = (chunkRadius * 2 + 1) * (chunkRadius * 2 + 1);

        sender.addChatMessage(new ChatComponentText(String.format(
            "\u00a7aPregen: generating %d chunks in radius %d around (%d, %d)...", totalChunks, radius, cx, cz)));

        cancel = false;
        running = true;

        // Run in a separate thread to not freeze the server
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int startCX = (cx >> 4) - chunkRadius;
                int startCZ = (cz >> 4) - chunkRadius;
                int endCX = (cx >> 4) + chunkRadius;
                int endCZ = (cz >> 4) + chunkRadius;
                int count = 0;
                int generated = 0;
                long startTime = System.currentTimeMillis();

                for (int ccx = startCX; ccx <= endCX && !cancel; ccx++) {
                    for (int ccz = startCZ; ccz <= endCZ && !cancel; ccz++) {
                        try {
                            // Check if chunk already exists
                            if (!world.theChunkProviderServer.chunkExists(ccx, ccz)) {
                                // Generate the chunk
                                world.theChunkProviderServer.loadChunk(ccx, ccz);
                                generated++;
                            }
                            count++;

                            // Unload to save memory (keep only recent chunks)
                            if (count % 100 == 0) {
                                world.theChunkProviderServer.unloadQueuedChunks();

                                // Progress report
                                double pct = (count * 100.0) / totalChunks;
                                long elapsed = System.currentTimeMillis() - startTime;
                                double cps = count * 1000.0 / Math.max(elapsed, 1);
                                int remaining = (int)((totalChunks - count) / Math.max(cps, 0.1));

                                MinecraftServer.getServer().getConfigurationManager().sendChatMsg(
                                    new ChatComponentText(String.format(
                                        "\u00a7e[Pregen] %.1f%% (%d/%d) | \u00a7a%d new \u00a7e| ~%dm %ds left",
                                        pct, count, totalChunks, generated,
                                        remaining / 60, remaining % 60)));
                            }

                            // Small delay to not overload the server
                            if (count % 10 == 0) {
                                Thread.sleep(50);
                            }
                        } catch (Exception e) {
                            // Skip problematic chunks
                        }
                    }
                }

                // Save the world
                try {
                    world.saveAllChunks(true, null);
                    world.theChunkProviderServer.unloadQueuedChunks();
                } catch (Exception e) {}

                long totalTime = (System.currentTimeMillis() - startTime) / 1000;
                String msg = cancel
                    ? String.format("\u00a7c[Pregen] Cancelled. %d chunks processed, %d new generated.", count, generated)
                    : String.format("\u00a7a[Pregen] Done! %d chunks processed, %d new generated in %dm %ds.",
                        count, generated, totalTime / 60, totalTime % 60);

                MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(msg));
                running = false;
                cancel = false;
            }
        }, "Pregen-Thread");
        t.setDaemon(true);
        t.start();
    }
}
