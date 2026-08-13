package com.zero.client.gui.gun;

import com.zero.server.type.mode.IAttachmentType;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public class RefitTransform {
    // 以下参数、变量用于改装窗口动画插值
    private static final float REFIT_SCREEN_TRANSFORM_TIMES = 0.25f;
    private static float refitScreenTransformProgress = 1;
    private static long refitScreenTransformTimestamp = -1;
    private static IAttachmentType oldTransformType = IAttachmentType.NONE;
    private static IAttachmentType currentTransformType = IAttachmentType.NONE;
    private static float refitScreenOpeningProgress = 0;
    private static long refitScreenOpeningTimestamp = -1;

    public static void init() {
        refitScreenTransformProgress = 1;
        refitScreenTransformTimestamp = System.currentTimeMillis();
        oldTransformType = IAttachmentType.NONE;
        currentTransformType = IAttachmentType.NONE;
    }

    public static float getOpeningProgress() {
        return refitScreenOpeningProgress;
    }

    @Nonnull
    public static IAttachmentType getCurrentTransformType() {
        return currentTransformType;
    }

    @Nonnull
    public static IAttachmentType getOldTransformType() {
        return oldTransformType;
    }

    public static float getTransformProgress() {
        return refitScreenTransformProgress;
    }

    public static boolean changeRefitScreenView(IAttachmentType attachmentType) {
        if (refitScreenTransformProgress != 1 || refitScreenOpeningProgress != 1) {
            return false;
        }
        oldTransformType = currentTransformType;
        currentTransformType = attachmentType;
        refitScreenTransformProgress = 0;
        refitScreenTransformTimestamp = System.currentTimeMillis();
        return true;
    }

    public static void tickInterpolation() {
        if (refitScreenOpeningTimestamp == -1) {
            refitScreenOpeningTimestamp = System.currentTimeMillis();
        }
        if (Minecraft.getMinecraft().currentScreen instanceof GunAttachmentScreen) {
            refitScreenOpeningProgress += (System.currentTimeMillis() - refitScreenOpeningTimestamp) / (REFIT_SCREEN_TRANSFORM_TIMES * 1000);
            if (refitScreenOpeningProgress > 1) {
                refitScreenOpeningProgress = 1;
            }
        } else {
            refitScreenOpeningProgress -= (System.currentTimeMillis() - refitScreenOpeningTimestamp) / (REFIT_SCREEN_TRANSFORM_TIMES * 1000);
            if (refitScreenOpeningProgress < 0) {
                refitScreenOpeningProgress = 0;
            }
        }
        refitScreenOpeningTimestamp = System.currentTimeMillis();

        if (refitScreenTransformTimestamp == -1) {
            refitScreenTransformTimestamp = System.currentTimeMillis();
        }
        refitScreenTransformProgress += (System.currentTimeMillis() - refitScreenTransformTimestamp) / (REFIT_SCREEN_TRANSFORM_TIMES * 1000);
        if (refitScreenTransformProgress > 1) {
            refitScreenTransformProgress = 1;
        }
        refitScreenTransformTimestamp = System.currentTimeMillis();
    }
}