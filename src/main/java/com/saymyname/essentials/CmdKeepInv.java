package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * /keepinv <player> — toggle per-player keepInventory.
 * OP-only command. Uses DataStore for persistence.
 */
public class CmdKeepInv extends CommandBase {

    @Override
    public String getCommandName() { return "keepinv"; }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/keepinv <player> — включить/выключить сохранение инвентаря при смерти";
    }

    @Override
    public int getRequiredPermissionLevel() { return 2; } // OP only

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Использование: /keepinv <player>"));
            return;
        }

        String targetName = args[0];
        EntityPlayerMP target = MinecraftServer.getServer().getConfigurationManager().func_152612_a(targetName);

        if (target == null) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Игрок " + targetName + " не найден"));
            return;
        }

        String uuid = target.getUniqueID().toString();
        boolean current = KeepInvHandler.hasKeepInv(uuid);

        if (current) {
            KeepInvHandler.removeKeepInv(uuid);
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "KeepInventory " + EnumChatFormatting.RED + "выключен" + EnumChatFormatting.YELLOW + " для " + targetName));
            target.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Сохранение инвентаря при смерти " + EnumChatFormatting.RED + "выключено"));
        } else {
            KeepInvHandler.addKeepInv(uuid);
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "KeepInventory " + EnumChatFormatting.GREEN + "включен" + EnumChatFormatting.YELLOW + " для " + targetName));
            target.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Сохранение инвентаря при смерти " + EnumChatFormatting.GREEN + "включено"));
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        }
        return new ArrayList();
    }
}
