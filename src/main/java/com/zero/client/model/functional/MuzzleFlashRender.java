package com.zero.client.model.functional;

import com.zero.client.model.BedrockGunModel;
import com.zero.client.model.SlotModel;
import com.zero.client.model.display.MuzzleFlashText;
import com.zero.client.render.CustomItemRenderType;
import com.zero.client.util.EnumTexturesType;
import com.zero.client.util.ZeroResources;
import com.zero.server.type.GunType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class MuzzleFlashRender implements IFunctionalRenderer {
    private static final SlotModel SLOT_MODEL = new SlotModel(true);
    /**
     * 50ms 显示时间
     */
    private static final long TIME_RANGE = 50;
    public static boolean isSelf = false;
    private static long shootTimeStamp = -1;
    private static boolean muzzleFlashStartMark = false;
    private static float muzzleFlashRandomRotate = 0;

    private final BedrockGunModel bedrockGunModel;

    public MuzzleFlashRender(BedrockGunModel bedrockGunModel) {
        this.bedrockGunModel = bedrockGunModel;
    }

    public static void onShoot() {
        // 记录开火时间戳
        shootTimeStamp = System.currentTimeMillis();
        // 记录枪口火焰启动标记
        muzzleFlashStartMark = true;
        // 随机给予枪口火焰的旋转
        muzzleFlashRandomRotate = (float) (Math.random() * 360);
    }


    @Override
    public void render(CustomItemRenderType type) {
        if (!isSelf && !muzzleFlashStartMark) {
            return;
        }
        long time = System.currentTimeMillis() - shootTimeStamp;
        if (time > TIME_RANGE) {
            muzzleFlashStartMark = false;
            return;
        }
        GunType gunType = GunType.getGunType(bedrockGunModel.getCurrentGunItem());
        if (gunType == null) {
            return;
        }
        MuzzleFlashText flash = gunType.muzzleFlashText;
        if (gunType.muzzleFlashText == null) {
            return;
        }
        float scale = 0.5f * flash.getScale();
        float scaleTime = TIME_RANGE / 2.0f;
        scale = time < scaleTime ? (scale * (time / scaleTime)) : scale;

        ResourceLocation resourceLocation = ZeroResources.getTextures(EnumTexturesType.MUZZLE, flash.getMuzzleFlash());

        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        // 渲染背景层
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(muzzleFlashRandomRotate, 0f, 0f, 1f);
        GlStateManager.translate(0f, -1f, 0f);
        SLOT_MODEL.render();
        GlStateManager.popMatrix();

        //渲染叠加层
        GlStateManager.pushMatrix();
        float glowScale = scale / 2f;
        GlStateManager.scale(glowScale, glowScale, glowScale);
        GlStateManager.rotate(muzzleFlashRandomRotate, 0f, 0f, 1f);
        GlStateManager.translate(0f, -0.9f, 0f);
        SLOT_MODEL.render();
        GlStateManager.popMatrix();

        Minecraft.getMinecraft().getTextureManager().bindTexture(bedrockGunModel.getCurrentTexture());
    }
}
