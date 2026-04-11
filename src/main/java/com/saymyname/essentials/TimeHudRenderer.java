package com.saymyname.essentials;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

@SideOnly(Side.CLIENT)
public class TimeHudRenderer {

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.gameSettings.showDebugInfo) return;

        long worldTime = mc.theWorld.getWorldTime() % 24000;

        // Convert MC ticks to hours/minutes (0 ticks = 6:00 AM)
        int totalMinutes = (int) ((worldTime / 1000.0) * 60) + 360; // +6h offset
        int hours = (totalMinutes / 60) % 24;
        int minutes = totalMinutes % 60;

        String timeStr = String.format("%02d:%02d", hours, minutes);

        // Day/night icon
        boolean isDay = worldTime >= 0 && worldTime < 12000;
        String icon = isDay ? "\u2600" : "\u263D"; // ☀ / ☽
        // Fallback: MC font may not have these, use text
        String label = isDay ? "Day" : "Night";

        String display = timeStr + " | " + label;

        FontRenderer fr = mc.fontRenderer;
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        int textWidth = fr.getStringWidth(display);

        // Position: top-right, below JourneyMap minimap
        // JourneyMap minimap default size ~128px from top-right
        int x = sr.getScaledWidth() - textWidth - 8;
        int y = 145; // below minimap (default ~128px tall + gap)

        // Background box
        int padding = 3;
        drawRect(x - padding, y - padding,
                x + textWidth + padding, y + fr.FONT_HEIGHT + padding,
                0x80000000); // semi-transparent black

        // Time text with color: day=gold, night=light blue
        int color = isDay ? 0xFFDD44 : 0x88BBFF;
        fr.drawStringWithShadow(display, x, y, color);
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }
}
