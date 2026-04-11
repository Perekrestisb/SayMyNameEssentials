package com.saymyname.essentials;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void registerRenderers() {
        // RenderTickEvent is on FML bus, not Forge bus
        FMLCommonHandler.instance().bus().register(new TimeHudRenderer());
    }
}
