package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CmdClearlag extends CommandBase {
    @Override public String getCommandName() { return "clearlag"; }
    @Override public String getCommandUsage(ICommandSender sender) { return "/clearlag [items|mobs|all]"; }
    @Override public int getRequiredPermissionLevel() { return 2; }

    // Boss entity class names - never kill these
    private static final Set<String> BOSS_NAMES = new HashSet<String>();
    static {
        // DivineRPG bosses
        BOSS_NAMES.add("entityanciententity");
        BOSS_NAMES.add("entitythewatcher");
        BOSS_NAMES.add("entityenderwatcher");
        BOSS_NAMES.add("entitykaros");
        BOSS_NAMES.add("entitydramix");
        BOSS_NAMES.add("entityparasecta");
        BOSS_NAMES.add("entitykingofscorchers");
        BOSS_NAMES.add("entityhivequeen");
        BOSS_NAMES.add("entitykingcrab");
        // Twilight Forest bosses
        BOSS_NAMES.add("entitytflich");
        BOSS_NAMES.add("entitytfnaga");
        BOSS_NAMES.add("entitytfhydra");
        BOSS_NAMES.add("entitytfhydrahead");
        BOSS_NAMES.add("entitytfhydraneck");
        BOSS_NAMES.add("entitytfhydrapart");
        BOSS_NAMES.add("entitytfurghast");
        BOSS_NAMES.add("entitytfminoshroom");
        BOSS_NAMES.add("entitytfknightphantom");
        BOSS_NAMES.add("entitytfsnowqueen");
        BOSS_NAMES.add("entitytfyetialpha");
        BOSS_NAMES.add("entitytfkingspider");
        // Thaumcraft bosses
        BOSS_NAMES.add("entityeldritchgolem");
        BOSS_NAMES.add("entityeldritchwarden");
        BOSS_NAMES.add("entitycultistleader");
        BOSS_NAMES.add("entitytaintaclegiant");
        // Witchery bosses
        BOSS_NAMES.add("entitylilith");
        BOSS_NAMES.add("entitylordoftorment");
        BOSS_NAMES.add("entityleonard");
        BOSS_NAMES.add("entitybaba");
        // Vanilla bosses
        BOSS_NAMES.add("entitywither");
        BOSS_NAMES.add("entitydragon");
        BOSS_NAMES.add("entitywitherboss");
        // NPC merchants (DivineRPG) - don't kill
        BOSS_NAMES.add("entityjackoman");
        BOSS_NAMES.add("entityworkshoptinkerer");
        BOSS_NAMES.add("entityworkshopmerchant");
        BOSS_NAMES.add("entityzelius");
        BOSS_NAMES.add("entitycaptainmerik");
        BOSS_NAMES.add("entitydatticon");
        BOSS_NAMES.add("entityvamacheron");
        BOSS_NAMES.add("entitythemaster");
        BOSS_NAMES.add("entitywargeneral");
        BOSS_NAMES.add("entityhunger");
        BOSS_NAMES.add("entityleorna");
        BOSS_NAMES.add("entitylordvatticus");
        BOSS_NAMES.add("entitymandragora");
    }

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

                if (doMobs && entity instanceof IMob) {
                    if (isBoss(entity)) continue;
                    toRemove.add(entity);
                    killedMobs++;
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

        server.getConfigurationManager().sendChatMsg(new ChatComponentText(result));
        sender.addChatMessage(new ChatComponentText(
                "Cleared: " + killedItems + " items, " + killedXp + " xp, " + killedMobs + " mobs"));
    }

    private boolean isBoss(Entity entity) {
        // 1. Minecraft boss interface (health bar at top of screen)
        if (entity instanceof IBossDisplayData) return true;

        // 2. Check class hierarchy for boss superclasses
        Class<?> cls = entity.getClass();
        while (cls != null && cls != Entity.class) {
            String className = cls.getSimpleName().toLowerCase();
            // DivineRPG boss base class
            if (className.equals("entitydivinerpgboss")) return true;
            // Thaumcraft boss base class
            if (className.equals("entitythaumcraftboss")) return true;
            // Generic "boss" in class name
            if (className.contains("boss") && !className.contains("bossbar")) return true;
            cls = cls.getSuperclass();
        }

        // 3. Check interfaces for IDivineRPGBoss
        for (Class<?> iface : entity.getClass().getInterfaces()) {
            if (iface.getSimpleName().equals("IDivineRPGBoss")) return true;
        }

        // 4. Explicit name list
        String simpleName = entity.getClass().getSimpleName().toLowerCase();
        if (BOSS_NAMES.contains(simpleName)) return true;

        // 5. Check if entity has a custom name (player-named mobs, quest mobs)
        if (entity instanceof EntityLivingBase) {
            String tag = ((EntityLivingBase) entity).getCustomNameTag();
            if (tag != null && !tag.isEmpty()) return true;
        }

        return false;
    }
}
