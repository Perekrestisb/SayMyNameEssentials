package com.saymyname.essentials;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

public class CmdRtp extends CommandBase {
    private static final Random rand = new Random();
    private static final int MIN_RANGE = 500;
    private static final int MAX_RANGE = 5000;
    private static final int MAX_ATTEMPTS = 20;

    @Override public String getCommandName() { return "rtp"; }
    @Override public String getCommandUsage(ICommandSender sender) { return "/rtp"; }
    @Override public int getRequiredPermissionLevel() { return 0; }
    @Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) sender;
        World world = player.worldObj;

        // Only allow in overworld (dim 0)
        if (player.dimension != 0) {
            TpUtil.err(player, "RTP работает только в обычном мире!");
            return;
        }

        TpUtil.msg(player, "Поиск безопасного места...");

        // Save back location
        DataStore.saveBack(player);

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // Random position in range
            int dx = (rand.nextInt(MAX_RANGE - MIN_RANGE) + MIN_RANGE) * (rand.nextBoolean() ? 1 : -1);
            int dz = (rand.nextInt(MAX_RANGE - MIN_RANGE) + MIN_RANGE) * (rand.nextBoolean() ? 1 : -1);
            int x = (int) player.posX + dx;
            int z = (int) player.posZ + dz;

            // Find safe Y
            int y = findSafeY(world, x, z);
            if (y > 0) {
                TpUtil.teleport(player, x + 0.5, y + 1.0, z + 0.5, 0, player.rotationYaw, player.rotationPitch);
                TpUtil.msg(player, "Телепортация! (" + x + ", " + y + ", " + z + ")");
                return;
            }
        }

        TpUtil.err(player, "Не удалось найти безопасное место. Попробуй ещё раз!");
    }

    private int findSafeY(World world, int x, int z) {
        // Search from top down for safe spot
        for (int y = 250; y > 4; y--) {
            Block blockBelow = world.getBlock(x, y - 1, z);
            Block blockFeet = world.getBlock(x, y, z);
            Block blockHead = world.getBlock(x, y + 1, z);

            // Ground must be solid, not dangerous
            if (blockBelow == null || blockBelow == Blocks.air) continue;
            if (blockBelow == Blocks.lava || blockBelow == Blocks.flowing_lava) continue;
            if (blockBelow == Blocks.cactus) continue;
            if (blockBelow == Blocks.fire) continue;

            // Must be solid (not water, not leaves)
            if (!blockBelow.getMaterial().isSolid()) continue;

            // Feet and head must be air or passable
            if (blockFeet != Blocks.air && blockFeet.getMaterial() != Material.plants
                && blockFeet.getMaterial() != Material.vine) continue;
            if (blockHead != Blocks.air && blockHead.getMaterial() != Material.plants
                && blockHead.getMaterial() != Material.vine) continue;

            // Don't spawn in caves (must see sky within 10 blocks)
            boolean seeSky = false;
            for (int checkY = y + 2; checkY < Math.min(y + 30, 256); checkY++) {
                if (world.getBlock(x, checkY, z) == Blocks.air) {
                    if (world.canBlockSeeTheSky(x, checkY, z)) {
                        seeSky = true;
                        break;
                    }
                }
            }
            if (!seeSky) continue;

            return y;
        }
        return -1;
    }
}
