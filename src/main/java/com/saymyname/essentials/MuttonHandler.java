package com.saymyname.essentials;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

/**
 * Adds raw mutton and cooked mutton drops from sheep.
 * Raw: 3 hunger, 0.3 saturation (like raw chicken)
 * Cooked: 6 hunger, 0.8 saturation (like cooked porkchop)
 */
public class MuttonHandler {

    public static Item rawMutton;
    public static Item cookedMutton;

    public static void init() {
        rawMutton = new ItemFood(3, 0.3F, true)
                .setUnlocalizedName("rawMutton")
                .setTextureName("saymynameessentials:rawMutton")
                .setCreativeTab(CreativeTabs.tabFood);

        cookedMutton = new ItemFood(6, 0.8F, true)
                .setUnlocalizedName("cookedMutton")
                .setTextureName("saymynameessentials:cookedMutton")
                .setCreativeTab(CreativeTabs.tabFood);

        GameRegistry.registerItem(rawMutton, "rawMutton");
        GameRegistry.registerItem(cookedMutton, "cookedMutton");

        // Smelting recipe: raw -> cooked
        GameRegistry.addSmelting(rawMutton, new ItemStack(cookedMutton), 0.35F);

        // Register event handler for drops
        MinecraftForge.EVENT_BUS.register(new MuttonHandler());
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (event.entityLiving instanceof EntitySheep) {
            EntitySheep sheep = (EntitySheep) event.entityLiving;

            // Drop 1-2 raw mutton (+ looting bonus)
            int count = 1 + sheep.worldObj.rand.nextInt(2);
            if (event.lootingLevel > 0) {
                count += sheep.worldObj.rand.nextInt(event.lootingLevel + 1);
            }

            // If sheep was on fire, drop cooked mutton
            Item drop = sheep.isBurning() ? cookedMutton : rawMutton;

            for (int i = 0; i < count; i++) {
                sheep.entityDropItem(new ItemStack(drop), 0.0F);
            }
        }
    }
}
