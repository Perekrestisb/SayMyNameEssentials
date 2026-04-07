package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CmdBack extends CommandBase {

    @Override
    public String getCommandName() {
        return "back";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/back";
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
        String uuid = player.getUniqueID().toString();
        double[] back = DataStore.getBackLocation(uuid);

        if (back == null) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Нет сохранённой позиции для возврата!"));
            return;
        }

        DataStore.setBackLocation(uuid, player.posX, player.posY, player.posZ, player.dimension);
        TpUtil.teleport(player, back[0], back[1], back[2], (int) back[3], player.rotationYaw, player.rotationPitch);
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Телепортация на предыдущую позицию!"));
    }
}
