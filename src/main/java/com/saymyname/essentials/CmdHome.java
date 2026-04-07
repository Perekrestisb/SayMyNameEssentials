package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Map;

public class CmdHome extends CommandBase {

    @Override
    public String getCommandName() {
        return "home";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/home [name]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = (EntityPlayerMP) sender;
        String name = args.length > 0 ? args[0].toLowerCase() : "home";
        String uuid = player.getUniqueID().toString();

        Map<String, double[]> homes = DataStore.getHomes(uuid);

        if (!homes.containsKey(name)) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Дом \"" + name + "\" не найден! Используйте /homes для списка."));
            return;
        }

        double[] pos = homes.get(name);
        DataStore.setBackLocation(uuid, player.posX, player.posY, player.posZ, player.dimension);
        TpUtil.teleport(player, pos[0], pos[1], pos[2], (int) pos[3]);
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Телепортация к дому \"" + name + "\"!"));
    }
}
