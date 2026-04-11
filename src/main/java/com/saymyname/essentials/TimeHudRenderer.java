package com.saymyname.essentials;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class TimeHudRenderer {

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) return;
        if (mc.gameSettings.showDebugInfo) return;
        if (mc.currentScreen != null) return;

        long worldTime = mc.theWorld.getWorldTime() % 24000;

        // Convert MC ticks to hours/minutes (0 ticks = 6:00 AM)
        int totalMinutes = (int) ((worldTime / 1000.0) * 60) + 360;
        int hours = (totalMinutes / 60) % 24;
        int minutes = totalMinutes % 60;

        String timeStr = String.format("%02d:%02d", hours, minutes);

        boolean isDay = worldTime >= 0 && worldTime < 12000;
        String label = isDay ? "Day" : "Night";
        String display = timeStr + " " + label;

        FontRenderer fr = mc.fontRenderer;
        if (fr == null) return;

        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        int textWidth = fr.getStringWidth(display);
        int screenWidth = sr.getScaledWidth();

        // Position: top center
        int x = (screenWidth - textWidth) / 2;
        int y = 5;

        // Save GL state
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Background box
        int pad = 4;
        drawRect(x - pad, y - pad, x + textWidth + pad, y + fr.FONT_HEIGHT + pad, 0xAA000000);

        // Text
        int color = isDay ? 0xFFFFDD44 : 0xFF88BBFF;
        fr.drawStringWithShadow(display, x, y, color);

        // Restore GL state
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(left, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);
        GL11.glVertex2f(left, top);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
