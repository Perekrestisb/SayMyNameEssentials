package com.saymyname.essentials;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

@SideOnly(Side.CLIENT)
public class TimeHudRenderer {

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.gameSettings.showDebugInfo) return;
        // Don't render if a GUI screen is open
        if (mc.currentScreen != null) return;

        long worldTime = mc.theWorld.getWorldTime() % 24000;

        // Convert MC ticks to hours/minutes (0 ticks = 6:00 AM)
        int totalMinutes = (int) ((worldTime / 1000.0) * 60) + 360;
        int hours = (totalMinutes / 60) % 24;
        int minutes = totalMinutes % 60;

        String timeStr = String.format("%02d:%02d", hours, minutes);

        boolean isDay = worldTime >= 0 && worldTime < 12000;
        String label = isDay ? "Day" : "Night";
        String display = timeStr + " | " + label;

        FontRenderer fr = mc.fontRenderer;
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        int textWidth = fr.getStringWidth(display);
        int screenWidth = sr.getScaledWidth();

        // Position: centered horizontally at top of screen, just below any minimap
        // This avoids overlap with JourneyMap regardless of minimap size
        int x = (screenWidth - textWidth) / 2;
        int y = 4;

        // Background
        int pad = 3;
        Gui.drawRect(x - pad, y - pad, x + textWidth + pad, y + fr.FONT_HEIGHT + pad, 0x90000000);

        // Text: gold for day, light blue for night
        int color = isDay ? 0xFFDD44 : 0x88BBFF;
        fr.drawStringWithShadow(display, x, y, color);
    }
}
