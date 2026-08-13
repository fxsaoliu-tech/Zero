package com.zero.client.model.functional;

import com.zero.Zero;
import com.zero.client.gui.config.ZeroConfig;
import com.zero.client.model.bedrock.BedrockPart;
import com.zero.server.item.ItemAttachment;
import com.zero.server.type.AttachmentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

public class BeamRenderer {
    private final static ResourceLocation texture = new ResourceLocation(Zero.MOD_ID, "textures/default_laser.png");

    public static void renderLaserBeam(ItemStack stack, @Nonnull List<BedrockPart> path) {
        if (stack == null) {
            return;
        }
        if (stack.isEmpty()) {
            return;
        }
        Item item = stack.getItem();
        if (!(item instanceof ItemAttachment)) {
            return;
        }
        AttachmentType attachment = ((ItemAttachment) item).getType();

        if (attachment.color == null) {
            return;
        }
        GlStateManager.pushMatrix();
        // 应用 BedrockPart 变换
        for (BedrockPart part : path) {
            part.applyTranslateAndRotate();
        }
        stringVertex(-ZeroConfig.beam_length, ZeroConfig.beam_width, attachment.color[0], attachment.color[1], attachment.color[2]);
        GlStateManager.popMatrix();
    }

    private static void stringVertex(float z, float width, float r, float g, float b) {
        float lastX = OpenGlHelper.lastBrightnessX;
        float lastY = OpenGlHelper.lastBrightnessY;
        // 关闭标准光照，让顶点颜色/纹理全亮
        GlStateManager.disableLighting();
        // 将 lightmap 推到最大（240,240 是 Minecraft 的最大亮度）
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        float halfWidth = width / 2f;
        // 设置全局颜色（所有顶点共用）
        GlStateManager.color(r, g, b, 1);

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        // ---- 面1: X负方向 ----
        addQuad(buffer, -halfWidth, -halfWidth, 0, -halfWidth, halfWidth, 0, -halfWidth, halfWidth, z, -halfWidth, -halfWidth, z, 0, 0, 0, 1, 1, 1, 1, 0);
        // ---- 面2: Y正方向 ----
        addQuad(buffer, -halfWidth, halfWidth, 0, halfWidth, halfWidth, 0, halfWidth, halfWidth, z, -halfWidth, halfWidth, z, 0, 0, 0, 1, 1, 1, 1, 0);
        // ---- 面3: X正方向 ----
        addQuad(buffer, halfWidth, halfWidth, 0, halfWidth, -halfWidth, 0, halfWidth, -halfWidth, z, halfWidth, halfWidth, z, 0, 0, 0, 1, 1, 1, 1, 0);
        // ---- 面4: Y负方向 ----
        addQuad(buffer, halfWidth, -halfWidth, 0, -halfWidth, -halfWidth, 0, -halfWidth, -halfWidth, z, halfWidth, -halfWidth, z, 0, 0, 0, 1, 1, 1, 1, 0);
        tessellator.draw();

        // 恢复 lightmap（注意使用 lightmapTexUnit）
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
        // 恢复标准光照
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();

        GlStateManager.color(1, 1, 1, 1);
    }

    /**
     * 辅助：向 BufferBuilder 添加一个四边形（带纹理坐标）
     */
    private static void addQuad(BufferBuilder buffer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4) {
        buffer.pos(x1, y1, z1).tex(u1, v1).endVertex();
        buffer.pos(x2, y2, z2).tex(u2, v2).endVertex();
        buffer.pos(x3, y3, z3).tex(u3, v3).endVertex();
        buffer.pos(x4, y4, z4).tex(u4, v4).endVertex();
    }
}
