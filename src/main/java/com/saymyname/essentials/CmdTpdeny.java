package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CmdTpdeny extends CommandBase {

    @Override
    public String getCommandName() {
        return "tpdeny";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tpdeny";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = (EntityPlayerMP) sender;
        String uuid = player.getUniqueID().toString();

        String requesterUuid = CmdTpa.pendingRequests.get(uuid);

        if (requesterUuid == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "У вас нет входящих запросов на телепортацию!"));
            return;
        }

        CmdTpa.pendingRequests.remove(uuid);
        CmdTpa.requestTimestamps.remove(uuid);

        EntityPlayerMP requester = TpUtil.getPlayerByUuid(requesterUuid);

        player.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Запрос на телепортацию отклонён."));
        if (requester != null) {
            requester.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + player.getCommandSenderName() + " отклонил ваш запрос на телепортацию."));
        }
    }
}
