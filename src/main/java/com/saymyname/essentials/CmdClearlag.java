package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.IMob;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;

public class CmdClearlag extends CommandBase {
    @Override public String getCommandName() { return "clearlag"; }
    @Override public String getCommandUsage(ICommandSender sender) { return "/clearlag [items|mobs|all]"; }
    @Override public int getRequiredPermissionLevel() { return 2; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        String mode = args.length > 0 ? args[0].toLowerCase() : "all";

        boolean doItems = mode.equals("items") || mode.equals("all");
        boolean doMobs = mode.equals("mobs") || mode.equals("all");

        int killedItems = 0;
        int killedXp = 0;
        int killedMobs = 0;

        MinecraftServer server = MinecraftServer.getServer();

        for (WorldServer world : server.worldServers) {
            // Collect entities to remove (avoid ConcurrentModification)
            List<Entity> toRemove = new ArrayList<Entity>();

            for (Object obj : world.loadedEntityList) {
                Entity entity = (Entity) obj;

                if (doItems) {
                    if (entity instanceof EntityItem) {
                        toRemove.add(entity);
                        killedItems++;
                        continue;
                    }
                    if (entity instanceof EntityXPOrb) {
                        toRemove.add(entity);
                        killedXp++;
                        continue;
                    }
                }

                if (doMobs) {
                    if (entity instanceof IMob) {
                        // Skip bosses by checking custom name or health thresholds
                        String entityName = entity.getClass().getSimpleName();
                        if (isBoss(entityName)) continue;

                        toRemove.add(entity);
                        killedMobs++;
                    }
                }
            }

            for (Entity e : toRemove) {
                e.setDead();
            }
        }

        String result = "\u00a7a[\u0421\u0435\u0440\u0432\u0435\u0440] \u00a7f\u041e\u0447\u0438\u0441\u0442\u043a\u0430 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u0430! "
                + "\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432: " + killedItems
                + ", XP: " + killedXp
                + ", \u041c\u043e\u0431\u043e\u0432: " + killedMobs;

        // Broadcast to all players
        server.getConfigurationManager().sendChatMsg(new ChatComponentText(result));

        // Also send to command sender (for RCON response)
        sender.addChatMessage(new ChatComponentText(
                "Cleared: " + killedItems + " items, " + killedXp + " xp, " + killedMobs + " mobs"));
    }

    private boolean isBoss(String name) {
        String lower = name.toLowerCase();
        return lower.contains("anciententity")
                || lower.contains("thewatcher")
                || lower.contains("karos")
                || lower.contains("dramix")
                || lower.contains("parasecta")
                || lower.contains("twilightlich")
                || lower.contains("naga")
                || lower.contains("hydra")
                || lower.contains("urghast")
                || lower.contains("minoshroom")
                || lower.contains("phantomknight")
                || lower.contains("snowqueen")
                || lower.contains("boss")
                || lower.contains("wither");
    }
}
