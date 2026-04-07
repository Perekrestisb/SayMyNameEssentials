package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CmdReply extends CommandBase {

    @Override
    public String getCommandName() {
        return "r";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/r <message>";
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

        if (args.length < 1) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Использование: /r <сообщение>"));
            return;
        }

        String uuid = player.getUniqueID().toString();
        String targetUuid = DataStore.getReplyTarget(uuid);

        if (targetUuid == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Некому отвечать! Сначала напишите кому-нибудь через /m."));
            return;
        }

        EntityPlayerMP target = TpUtil.getPlayerByUuid(targetUuid);

        if (target == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Игрок не в сети!"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(" ");
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
