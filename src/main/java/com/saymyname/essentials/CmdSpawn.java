package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;

public class CmdSpawn extends CommandBase {

    @Override
    public String getCommandName() {
        return "spawn";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/spawn";
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

        DataStore.setBackLocation(uuid, player.posX, player.posY, player.posZ, player.dimension);

        WorldServer overworld = MinecraftServer.getServer().worldServerForDimension(0);
        double x = overworld.getSpawnPoint().posX + 0.5;
        double y = overworld.getSpawnPoint().posY;
        double z = overworld.getSpawnPoint().posZ + 0.5;

        TpUtil.teleport(player, x, y, z, 0, player.rotationYaw, player.rotationPitch);
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Телепортация на спавн!"));
    }
}
