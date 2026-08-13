package com.zero.client.model.functional;

import com.zero.client.model.BedrockAnimatedModel;
import com.zero.client.model.display.TextShowText;
import com.zero.client.render.CustomItemRenderType;
import com.zero.client.util.PapiManager;
import com.zero.client.util.ZeroResources;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class TextShowRender implements IFunctionalRenderer {
    private static final FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);

    private final BedrockAnimatedModel model;
    private final TextShowText textShowText;
    private final ItemStack itemStack;

    public TextShowRender(BedrockAnimatedModel model, TextShowText textShowText, ItemStack itemStack) {
        this.model = model;
        this.textShowText = textShowText;
        this.itemStack = itemStack;
    }

    @Override
    public void render(CustomItemRenderType type) {
        if (type != CustomItemRenderType.EQUIPPED_FIRST_PERSON || itemStack == null) {
            return;
        }
        String text = PapiManager.getText(textShowText.getText(), itemStack);
        if (text == null || text.isEmpty()) return;

        FontRenderer font = Minecraft.getMinecraft().fontRenderer;

        float[] capturedMatrix = new float[16];
        MATRIX_BUFFER.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MATRIX_BUFFER);
        MATRIX_BUFFER.get(capturedMatrix);

        model.delegateRender((delegateLight) -> {

            int width = font.getStringWidth(text);
            int xOffset;

            switch (textShowText.getAlign()) {
                case CENTER:
                    xOffset = width / 2;
                    break;
                case RIGHT:
                    xOffset = width;
                    break;
                default:
                    xOffset = 0;
            }

            GlStateManager.pushMatrix();

            MATRIX_BUFFER.clear();
            MATRIX_BUFFER.put(capturedMatrix);
            MATRIX_BUFFER.flip();
            GL11.glLoadMatrix(MATRIX_BUFFER);

            GlStateManager.rotate(180F, 0F, 0F, 1F);

            float scale = 2f / 300f * textShowText.getScale();
            GlStateManager.scale(scale, -scale, -scale);

            // ===== 光照 =====
            float prevBX = OpenGlHelper.lastBrightnessX;
            float prevBY = OpenGlHelper.lastBrightnessY;

            int packed = Math.min(textShowText.getTextLight(), 15) * 16;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, packed, packed);

            GlStateManager.disableLighting();

            //强制重置颜色（防污染）
            GlStateManager.color(1f, 1f, 1f, 1f);

            font.drawString(text, -xOffset, -font.FONT_HEIGHT / 2f, textShowText.getColor(), textShowText.isShadow());

            //  关键修复2：恢复颜色状态
            GlStateManager.color(1f, 1f, 1f, 1f);

            GlStateManager.enableLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevBX, prevBY);

            GlStateManager.popMatrix();

            Minecraft.getMinecraft().renderEngine.bindTexture(model.getCurrentTexture());
        });
    }
}
