package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Map;

public class CmdDelHome extends CommandBase {

    @Override
    public String getCommandName() {
        return "delhome";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/delhome <name>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = (EntityPlayerMP) sender;

        if (args.length < 1) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Использование: /delhome <имя>"));
            return;
        }

        String name = args[0].toLowerCase();
        String uuid = player.getUniqueID().toString();
        Map<String, double[]> homes = DataStore.getHomes(uuid);

        if (!homes.containsKey(name)) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Дом \"" + name + "\" не найден!"));
            return;
        }

        homes.remove(name);
        DataStore.saveHomes(uuid, homes);
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Дом \"" + name + "\" удалён!"));
    }
}
