package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CmdTpaccept extends CommandBase {

    @Override
    public String getCommandName() {
        return "tpaccept";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tpaccept";
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
        Long timestamp = CmdTpa.requestTimestamps.get(uuid);

        if (requesterUuid == null || timestamp == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "У вас нет входящих запросов на телепортацию!"));
            return;
        }

        if (System.currentTimeMillis() - timestamp > 60000) {
            CmdTpa.pendingRequests.remove(uuid);
            CmdTpa.requestTimestamps.remove(uuid);
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Запрос на телепортацию истёк!"));
            return;
        }

        EntityPlayerMP requester = TpUtil.getPlayerByUuid(requesterUuid);

        if (requester == null) {
            CmdTpa.pendingRequests.remove(uuid);
            CmdTpa.requestTimestamps.remove(uuid);
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Игрок, отправивший запрос, не в сети!"));
            return;
        }

        CmdTpa.pendingRequests.remove(uuid);
        CmdTpa.requestTimestamps.remove(uuid);

        DataStore.setBackLocation(requester.getUniqueID().toString(), requester.posX, requester.posY, requester.posZ, requester.dimension);
        TpUtil.teleport(requester, player.posX, player.posY, player.posZ, player.dimension);

        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Запрос на телепортацию принят!"));
        requester.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Телепортация к " + player.getCommandSenderName() + "!"));
    }
}
