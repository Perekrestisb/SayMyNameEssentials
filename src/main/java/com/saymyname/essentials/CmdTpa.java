package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.HashMap;
import java.util.Map;

public class CmdTpa extends CommandBase {

    public static final Map<String, String> pendingRequests = new HashMap<String, String>();
    public static final Map<String, Long> requestTimestamps = new HashMap<String, Long>();

    @Override
    public String getCommandName() {
        return "tpa";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tpa <player>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = (EntityPlayerMP) sender;

        if (args.length < 1) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Использование: /tpa <игрок>"));
            return;
        }

        String targetName = args[0];
        EntityPlayerMP target = MinecraftServer.getServer().getConfigurationManager().func_152612_a(targetName);

        if (target == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Игрок \"" + targetName + "\" не в сети!"));
            return;
        }

        if (target.getCommandSenderName().equals(player.getCommandSenderName())) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Нельзя отправить запрос самому себе!"));
            return;
        }

        String targetUuid = target.getUniqueID().toString();
        pendingRequests.put(targetUuid, player.getUniqueID().toString());
        requestTimestamps.put(targetUuid, System.currentTimeMillis());

        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Запрос на телепортацию отправлен игроку " + target.getCommandSenderName() + "!"));
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "Запрос истечёт через 60 секунд."));

        target.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + player.getCommandSenderName() + " хочет телепортироваться к вам!"));
        target.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "/tpaccept" + EnumChatFormatting.GRAY + " - принять | " + EnumChatFormatting.RED + "/tpdeny" + EnumChatFormatting.GRAY + " - отклонить"));
    }
}
