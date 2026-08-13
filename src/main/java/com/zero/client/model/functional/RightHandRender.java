package com.zero.client.model.functional;

import com.zero.client.model.BedrockAnimatedModel;
import com.zero.client.model.BedrockGunModel;
import com.zero.client.model.bedrock.BedrockPartWrapper;
import com.zero.client.render.CustomItemRenderType;
import com.zero.client.util.ZeroResources;
import com.zero.client.util.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHand;

public class RightHandRender implements IFunctionalRenderer {
    public BedrockAnimatedModel gun;

    public RightHandRender(BedrockAnimatedModel gun) {
        this.gun = gun;
    }

    @Override
    public void render(CustomItemRenderType type) {
        if (!gun.getRenderHand()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.rotate(180f, 0f, 0f, 1f);
        RenderHelper.renderFirstPersonArm(Minecraft.getMinecraft().player, EnumHand.MAIN_HAND);
        Minecraft.getMinecraft().renderEngine.bindTexture(gun.getCurrentTexture());
        GlStateManager.popMatrix();
    }
}
