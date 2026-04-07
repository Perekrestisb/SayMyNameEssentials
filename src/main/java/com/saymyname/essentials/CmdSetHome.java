package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Map;

public class CmdSetHome extends CommandBase {

    @Override
    public String getCommandName() {
        return "sethome";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sethome [name]";
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

        if (homes.containsKey(name)) {
            homes.put(name, new double[]{player.posX, player.posY, player.posZ, player.dimension});
            DataStore.saveHomes(uuid, homes);
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Дом \"" + name + "\" обновлён!"));
            return;
        }

        if (homes.size() >= 5) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Максимум 5 домов! Удалите один через /delhome <имя>."));
            return;
        }

        homes.put(name, new double[]{player.posX, player.posY, player.posZ, player.dimension});
        DataStore.saveHomes(uuid, homes);
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Дом \"" + name + "\" сохранён!"));
    }
}
