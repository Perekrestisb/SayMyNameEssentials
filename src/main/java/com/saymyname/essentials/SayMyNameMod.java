package com.saymyname.essentials;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

import java.io.File;

@Mod(modid = "saymynameessentials", name = "Say My Name Essentials", version = "1.2.0", acceptableRemoteVersions = "*")
public class SayMyNameMod {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MuttonHandler.init();
        KeepInvHandler.init(new File("config"));
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CmdKeepInv());
        event.registerServerCommand(new CmdSetHome());
        event.registerServerCommand(new CmdHome());
        event.registerServerCommand(new CmdDelHome());
        event.registerServerCommand(new CmdHomes());
        event.registerServerCommand(new CmdBack());
        event.registerServerCommand(new CmdSpawn());
        event.registerServerCommand(new CmdTpa());
        event.registerServerCommand(new CmdTpaccept());
        event.registerServerCommand(new CmdTpdeny());
        event.registerServerCommand(new CmdMsg());
        event.registerServerCommand(new CmdReply());
        event.registerServerCommand(new CmdRtp());
    }
}
