package com.zero.client.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.EnumHand;

public class RenderHelper {

    public static int toARGB(int a, int r, int g, int b) {
        return (clamp(a) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    public static void drawImage(int x, int y, int w, int h, float tileW, float tileH) {
        Gui.drawScaledCustomSizeModalRect(x, y, 0f, 0f, (int) tileW, (int) tileH, w, h, tileW, tileH);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public static void renderFirstPersonArm(EntityPlayerSP player, EnumHand hand) {
        Minecraft mc = Minecraft.getMinecraft();
        RenderManager manager = mc.getRenderManager();
        Render<?> render = manager.getEntityRenderObject(player);
        if (render instanceof RenderPlayer) {
            RenderPlayer renderPlayer = (RenderPlayer) render;
            mc.renderEngine.bindTexture(renderPlayer.getEntityTexture(player));
            if (hand == EnumHand.MAIN_HAND) {
                renderPlayer.renderRightArm(player);
            } else {
                renderPlayer.renderLeftArm(player);
            }
        }
    }

}
