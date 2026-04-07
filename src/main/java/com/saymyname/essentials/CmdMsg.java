package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class CmdMsg extends CommandBase {

    @Override
    public String getCommandName() {
        return "m";
    }

    @Override
    public List getCommandAliases() {
        return Arrays.asList("msg");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/m <player> <message>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = (EntityPlayerMP) sender;

        if (args.length < 2) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Использование: /m <игрок> <сообщение>"));
            return;
        }

        String targetName = args[0];
        EntityPlayerMP target = MinecraftServer.getServer().getConfigurationManager().func_152612_a(targetName);

        if (target == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Игрок \"" + targetName + "\" не в сети!"));
            return;
        }

        if (target.getCommandSenderName().equals(player.getCommandSenderName())) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Нельзя написать самому себе!"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) sb.append(" ");
            sb.append(args[i]);
        }
        String message = sb.toString();

        String senderName = player.getCommandSenderName();
        String receiverName = target.getCommandSenderName();

        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "[Я -> " + receiverName + "] " + message));
        target.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "[" + senderName + " -> Мне] " + message));

        DataStore.setReplyTarget(target.getUniqueID().toString(), player.getUniqueID().toString());
        DataStore.setReplyTarget(player.getUniqueID().toString(), target.getUniqueID().toString());
    }
}
