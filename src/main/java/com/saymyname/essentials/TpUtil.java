package com.saymyname.essentials;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class TpUtil {
    public static void teleport(EntityPlayerMP player, double x, double y, double z, int dim, float yaw, float pitch) {
        if (player.dimension != dim) {
            MinecraftServer server = MinecraftServer.getServer();
            server.getConfigurationManager().transferPlayerToDimension(player, dim, server.worldServerForDimension(dim).getDefaultTeleporter());
        }
        player.playerNetServerHandler.setPlayerLocation(x, y, z, yaw, pitch);
    }

    public static void msg(EntityPlayerMP player, String text, EnumChatFormatting color) {
        player.addChatMessage(new ChatComponentText(color + text));
    }

    public static void msg(EntityPlayerMP player, String text) {
        msg(player, text, EnumChatFormatting.GREEN);
    }

    public static void err(EntityPlayerMP player, String text) {
        msg(player, text, EnumChatFormatting.RED);
    }
}
