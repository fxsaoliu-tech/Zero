package com.zero.client.render;

import com.zero.api.client.ZeroClientPlayer;
import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.client.gui.gun.RefitTransform;
import com.zero.client.model.BedrockAttachmentModel;
import com.zero.client.model.BedrockGunModel;
import com.zero.client.model.bedrock.BedrockPart;
import com.zero.client.util.Axis;
import com.zero.client.util.EnumTexturesType;
import com.zero.client.util.math.PerlinNoise;
import com.zero.client.util.ZeroResources;
import com.zero.client.util.math.MathUtil;
import com.zero.client.util.math.SecondOrderDynamics;
import com.zero.server.type.AttachmentType;
import com.zero.server.type.GunType;
import com.zero.server.type.InfoType;
import com.zero.server.type.mode.IAttachmentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class RenderGun implements RenderCustomItem {
    // 用于生成瞄准动作的运动曲线，使动作看起来更平滑
    private static final SecondOrderDynamics AIMING_DYNAMICS = new SecondOrderDynamics(1.2f, 1.2f, 0.5f, 0);
    // 用于打开改装界面时枪械运动的平滑
    private static final SecondOrderDynamics REFIT_OPENING_DYNAMICS = new SecondOrderDynamics(1f, 1.2f, 0.5f, 0);
    // 用于枪械后座的程序动画
    private static final PerlinNoise SHOOT_X_SWAY_NOISE = new PerlinNoise(-0.2f, 0.2f, 400);
    private static final PerlinNoise SHOOT_Y_ROTATION_NOISE = new PerlinNoise(-0.0136f, 0.0136f, 100);
    private static final float SHOOT_Y_SWAY = -0.1f;
    private static final float SHOOT_ANIMATION_TIME = 0.3f;
    public static long shootTimeStamp = -1;
    //用于矩阵
    private static final FloatBuffer MODEL_VIEW = BufferUtils.createFloatBuffer(16);

    @Override
    public void render(CustomItemRenderType type, ItemStack stack, InfoType infoType, EntityLivingBase player) {
        GunType gunType = (GunType) infoType;
        BedrockGunModel gun = gunType.model;
        GlStateManager.pushMatrix();
        float[] scale;
        switch (type) {
            case FRAME:
                if (gunType.frame != null) {
                    scale = gunType.frame;
                } else {
                    scale = new float[]{0.25f, 0.25f, 0.25f};
                }
                GlStateManager.translate(0, 1.5, 0);
                gun.applyNodePathInverse(gun.getFixed(), scale);
                GlStateManager.scale(scale[0], scale[1], scale[2]);
                GlStateManager.translate(0, -1.5, 0);
                break;
            case ENTITY:
                if (gunType.entity != null) {
                    scale = gunType.entity;
                } else {
                    scale = new float[]{0.25f, 0.25f, 0.25f};
                }
                GlStateManager.translate(0, 1.5, 0);
                gun.applyNodePathInverse(gun.getEntity(), scale);
                GlStateManager.scale(scale[0], scale[1], scale[2]);
                GlStateManager.translate(0, -1.5, 0);
                break;
            case EQUIPPED_FIRST_PERSON:
                gunAnim(gun, stack, gunType);
                break;
            case EQUIPPED_THIRD_PERSON:
                if (gunType.third != null) {
                    scale = gunType.third;
                } else {
                    scale = new float[]{0.5f, 0.5f, 0.5f};
                }
                GlStateManager.translate(0, 1.5, 0);
                gun.applyNodePathInverse(gun.getThird_Person_Hand(), scale);
                GlStateManager.scale(scale[0], scale[1], scale[2]);
                GlStateManager.translate(0, -1.5, 0);
                break;
        }
        ResourceLocation location = ZeroResources.getTextures(EnumTexturesType.GUN, gunType.texture);

        if (type != CustomItemRenderType.EQUIPPED_FIRST_PERSON && gunType.textureLod != null) {
            gun = gunType.modelLod;
            location = ZeroResources.getLodTextures(EnumTexturesType.GUN, gunType.textureLod);
        }
        gun.setCurrentTexture(location);
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        gun.render(type, stack, gunType);
        GlStateManager.popMatrix();
    }

    //第一人称枪械动画
    private static void gunAnim(BedrockGunModel model, ItemStack stack, GunType type) {
        // 配合运动曲线，计算改装枪口的打开进度
        float refitScreenOpeningProgress = REFIT_OPENING_DYNAMICS.update(RefitTransform.getOpeningProgress());
        // 配合运动曲线，计算瞄准进度
        float aimingProgress = AIMING_DYNAMICS.update(((GunStateMachine) ZeroClientPlayer.getStateMachine()).getAimingProgress());
        applyShootSwayAndRotation(model, aimingProgress);
        // 应用各种摄像机定位组的变换（默认持枪、瞄准、改装界面等）
        applyFirstPersonPositioningTransform(stack, type, model, aimingProgress, refitScreenOpeningProgress);
        // 应用动画约束变换
        applyAnimationConstraintTransform(model, aimingProgress * (1 - refitScreenOpeningProgress));
    }

    private static void applyShootSwayAndRotation(BedrockGunModel model, float aimingProgress) {
        BedrockPart rootNode = model.getRoot();
        if (rootNode != null) {
            float progress = 1 - (System.currentTimeMillis() - shootTimeStamp) / (SHOOT_ANIMATION_TIME * 1000);
            if (progress < 0) {
                progress = 0;
            }
            progress = (float) MathUtil.easeOutCubic(progress);
            rootNode.offsetX += SHOOT_X_SWAY_NOISE.getValue() / 16 * progress * (1 - aimingProgress);
            // 基岩版模型 y 轴上下颠倒，sway 值取相反数
            rootNode.offsetY += -SHOOT_Y_SWAY / 16 * progress * (1 - aimingProgress);
            rootNode.additionalQuaternion.mul(Axis.YP.rotation(SHOOT_Y_ROTATION_NOISE.getValue() * progress));
        }
    }

    private static void applyFirstPersonPositioningTransform(ItemStack stack, GunType type, BedrockGunModel model, float aimingProgress, float refitScreenOpeningProgress) {
        Matrix4f transformMatrix = new Matrix4f().identity();
        // 应用瞄准定位
        List<BedrockPart> idleNodePath = model.getIdle_view();
        List<BedrockPart> aimingNodePath;

        AttachmentType attachmentType = type.getAttachment(stack, IAttachmentType.SCOPE);

        if (attachmentType == null) {
            // scopeId = iGun.getBuiltInAttachmentId(stack, AttachmentType.SCOPE);
        }
        if (attachmentType == null) {
            // 未安装瞄具，使用机瞄定位组
            aimingNodePath = model.getIronSightPath();
        } else {
            // 安装瞄具，组合瞄具定位组和瞄具视野定位组
            List<BedrockPart> scopeNodePath = model.getScopePosPath();
            if (scopeNodePath != null) {
                aimingNodePath = new ArrayList<>(scopeNodePath);
                BedrockAttachmentModel attachmentModel = attachmentType.getAnimatedModel();
                if (attachmentModel != null && attachmentModel.getScopeViewPath() != null) {
                    aimingNodePath.addAll(attachmentModel.getScopeViewPath());
                }
            } else {
                aimingNodePath = null;
            }
        }
        MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(idleNodePath), transformMatrix, (1.0f - refitScreenOpeningProgress));
        MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(aimingNodePath), transformMatrix, (1.0f - refitScreenOpeningProgress) * aimingProgress);
        // 应用改装界面开启时的定位
        float refitTransformProgress = (float) MathUtil.easeOutCubic(RefitTransform.getTransformProgress());
        IAttachmentType oldType = RefitTransform.getOldTransformType();
        IAttachmentType currentType = RefitTransform.getCurrentTransformType();
        List<BedrockPart> fromNode = model.getRefitAttachmentViewPath(oldType);
        List<BedrockPart> toNode = model.getRefitAttachmentViewPath(currentType);
        MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(fromNode), transformMatrix, refitScreenOpeningProgress);
        MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(toNode), transformMatrix, refitScreenOpeningProgress * refitTransformProgress);

        GL11.glTranslatef(0.0f, 1.5f, 0.0f);
        MODEL_VIEW.clear();
        transformMatrix.get(MODEL_VIEW);
        MODEL_VIEW.rewind();
        GL11.glMultMatrix(MODEL_VIEW);
        GL11.glTranslatef(0.0f, -1.5f, 0.0f);
    }

    @Nonnull
    private static Matrix4f getPositioningNodeInverse(List<BedrockPart> nodePath) {
        Matrix4f matrix = new Matrix4f().identity();

        if (nodePath == null || nodePath.isEmpty()) {
            return matrix;
        }
        for (int i = nodePath.size() - 1; i >= 0; i--) {
            BedrockPart part = nodePath.get(i);
            // =========================
            // 1. 反向旋转
            // =========================
            matrix.rotateX(-part.xRot);
            matrix.rotateY(-part.yRot);
            matrix.rotateZ(-part.zRot);
            // =========================
            // 2. 反向位移（单位换算 1/16）
            // =========================
            float x = -part.x / 16.0f;
            float y;
            float z = -part.z / 16.0f;
            // root特殊偏移处理（保留你原逻辑）
            if (part.getParent() != null) {
                y = -part.y / 16.0f;
            } else {
                y = (1.5f - part.y / 16.0f);
            }
            matrix.translate(x, y, z);
        }
        return matrix;
    }

    private static void getAnimationConstraintTransform(List<BedrockPart> nodePath, Vector3f originTranslation, Vector3f animatedTranslation, Vector3f rotation) {
        if (nodePath == null || nodePath.isEmpty()) {
            return;
        }
        Matrix4f animeMatrix = new Matrix4f().identity();
        Matrix4f originMatrix = new Matrix4f().identity();
        BedrockPart constrainNode = nodePath.get(nodePath.size() - 1);
        for (BedrockPart part : nodePath) {
            if (part != constrainNode) {
                animeMatrix.translate(part.offsetX, part.offsetY, part.offsetZ);
            }
            if (part.getParent() != null) {
                animeMatrix.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
            } else {
                animeMatrix.translate(part.x / 16.0F, part.y / 16.0F - 1.5F, part.z / 16.0F);
            }

            if (part != constrainNode && part.additionalQuaternion != null) {
                animeMatrix.rotate(part.additionalQuaternion);
            }
            animeMatrix.rotateZ(part.zRot).rotateY(part.yRot).rotateX(part.xRot);

            if (part.getParent() != null) {
                originMatrix.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
            } else {
                originMatrix.translate(part.x / 16.0F, part.y / 16.0F - 1.5F, part.z / 16.0F);
            }
            originMatrix.rotateZ(part.zRot).rotateY(part.yRot).rotateX(part.xRot);
        }

        animeMatrix.getTranslation(animatedTranslation);
        originMatrix.getTranslation(originTranslation);

        Vector3f animatedRotation = MathUtil.getEulerAngles(animeMatrix);
        Vector3f originRotation = MathUtil.getEulerAngles(originMatrix);
        animatedRotation.sub(originRotation);
        rotation.set(animatedRotation.x(), animatedRotation.y(), animatedRotation.z());
    }

    public static void applyAnimationConstraintTransform(BedrockGunModel gunModel, float weight) {
        List<BedrockPart> nodePath = gunModel.getConstraintPath();
        if (nodePath == null || nodePath.isEmpty() || gunModel.getConstraintObject() == null) {
            return;
        }

        Vector3f originTranslation = new Vector3f();
        Vector3f animatedTranslation = new Vector3f();
        Vector3f rotation = new Vector3f();
        Vector3f translationICA = gunModel.getConstraintObject().translationConstraint;
        Vector3f rotationICA = gunModel.getConstraintObject().rotationConstraint;

        getAnimationConstraintTransform(nodePath, originTranslation, animatedTranslation, rotation);

        MODEL_VIEW.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODEL_VIEW);

        Matrix4f mvnMatrix = new Matrix4f().set(MODEL_VIEW);

        // 配合约束系数，计算约束位移需要的反向位移
        Vector3f inverseTranslation = new Vector3f(originTranslation).sub(animatedTranslation);
        inverseTranslation.mulDirection(mvnMatrix);
        inverseTranslation.mul(translationICA.x() - 1f, translationICA.y() - 1f, 1f - translationICA.z());
        // 计算约束旋转需要的反向旋转。因需要插值，获取的是欧拉角
        Vector3f inverseRotation = new Vector3f(rotation);
        inverseRotation.mul(rotationICA.x() - 1, rotationICA.y() - 1, rotationICA.z() - 1);

        GlStateManager.translate(animatedTranslation.x(), animatedTranslation.y() + 1.5f, animatedTranslation.z());
        GL11.glRotated(Math.toDegrees(inverseRotation.x() * weight), 1, 0, 0);
        GL11.glRotated(Math.toDegrees(inverseRotation.y() * weight), 0, 1, 0);
        GL11.glRotated(Math.toDegrees(inverseRotation.z() * weight), 0, 0, 1);
        GlStateManager.translate(-animatedTranslation.x(), -animatedTranslation.y() - 1.5f, -animatedTranslation.z());

        MODEL_VIEW.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODEL_VIEW);
        Matrix4f poseMatrix = new Matrix4f().set(MODEL_VIEW);

        poseMatrix.m30(poseMatrix.m30() - inverseTranslation.x() * weight);
        poseMatrix.m31(poseMatrix.m31() - inverseTranslation.y() * weight);
        poseMatrix.m32(poseMatrix.m32() + inverseTranslation.z() * weight);

        GL11.glLoadIdentity();

        MODEL_VIEW.clear();
        poseMatrix.get(MODEL_VIEW);
        MODEL_VIEW.rewind();
        GL11.glMultMatrix(MODEL_VIEW);
    }

}