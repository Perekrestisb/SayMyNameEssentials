package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Map;

public class CmdHomes extends CommandBase {

    @Override
    public String getCommandName() {
        return "homes";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/homes";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = (EntityPlayerMP) sender;
        String uuid = player.getUniqueID().toString();
        Map<String, double[]> homes = DataStore.getHomes(uuid);

        if (homes.isEmpty()) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "У вас нет сохранённых домов. Используйте /sethome [имя]."));
            return;
        }

        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "=== Ваши дома (" + homes.size() + "/5) ==="));
        for (Map.Entry<String, double[]> entry : homes.entrySet()) {
            double[] pos = entry.getValue();
            String line = EnumChatFormatting.GREEN + entry.getKey() + EnumChatFormatting.GRAY
                    + " [" + (int) pos[0] + ", " + (int) pos[1] + ", " + (int) pos[2]
                    + "] dim=" + (int) pos[3];
            player.addChatMessage(new ChatComponentText(line));
        }
    }
}
