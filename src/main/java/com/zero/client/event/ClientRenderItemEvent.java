package com.zero.client.event;

import com.zero.Zero;
import com.zero.api.client.KeepingItemRenderer;
import com.zero.api.client.ZeroClientPlayer;
import com.zero.api.client.event.RenderItemInHandBobEvent;
import com.zero.client.gui.gun.RefitTransform;
import com.zero.client.model.functional.ShellRender;
import com.zero.client.util.Axis;
import com.zero.client.model.BedrockAnimatedModel;
import com.zero.client.model.bedrock.BedrockPart;
import com.zero.client.render.CustomItemRenderType;
import com.zero.client.render.RenderCustomItem;
import com.zero.client.render.RenderGun;
import com.zero.client.util.math.MathUtil;
import com.zero.client.util.math.SecondOrderDynamics;
import com.zero.server.file.FileType;
import com.zero.server.item.ItemZero;
import com.zero.server.type.InfoType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class ClientRenderItemEvent {
    private Minecraft mc;
    public static final RenderCustomItem[] customRenders = new RenderCustomItem[FileType.values().length];
    private static boolean isOptiFine = false;
    // 用于跳跃延滞动画的平滑
    private static final SecondOrderDynamics JUMPING_DYNAMICS = new SecondOrderDynamics(0.28f, 1f, 0.65f, 0);
    private static final float JUMPING_Y_SWAY = -2f;
    private static final float JUMPING_SWAY_TIME = 0.3f;
    private static final float LANDING_SWAY_TIME = 0.15f;
    private static float jumpingSwayProgress = 0;
    private static boolean lastOnGround = false;
    private static long jumpingTimeStamp = -1;

    public ClientRenderItemEvent() {
        Zero.eventRegister(this);
        mc = Minecraft.getMinecraft();
        customRenders[FileType.GUN.ordinal()] = new RenderGun();
    }

    @SubscribeEvent
    public void renderItemFrame(RenderItemInFrameEvent event) {
        ItemStack stack = event.getItem();
        if (stack.getItem() instanceof ItemZero) {
            InfoType infoType = ((ItemZero) stack.getItem()).getType();
            RenderCustomItem render = ClientRenderItemEvent.customRenders[FileType.getIndex(infoType).ordinal()];
            if (infoType.existModel() && render != null) {
                event.setCanceled(true);
                BedrockAnimatedModel bedrockAnimatedModel = infoType.getAnimatedModel();
                GlStateManager.pushMatrix();
                bedrockAnimatedModel.setRenderHand(false);
                GlStateManager.translate(0, 1.5, -0.025);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                render.render(CustomItemRenderType.FRAME, stack, infoType, null);
                GlStateManager.popMatrix();
            }
        }
    }

    @SubscribeEvent
    public void cancelItemInHandViewBobbing(RenderItemInHandBobEvent.BobView event) {
        if (mc.player == null) {
            return;
        }
        ItemStack itemStack = KeepingItemRenderer.getRenderer().getCurrentItem();
        if (ItemZero.isZero(itemStack)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void renderHeldItem(RenderSpecificHandEvent event) {
        EntityPlayerSP player = mc.player;
        ItemStack stack = event.getItemStack();
        // 只处理主手 同时如果副手有其他物品也不渲染
        if (!ItemZero.isZero(stack) && event.getHand() != EnumHand.MAIN_HAND && ItemZero.isZero(player.getHeldItemMainhand())) {
            event.setCanceled(true);
            return;
        }
        if (ItemZero.isZero(stack) && event.getHand() == EnumHand.OFF_HAND) {
            event.setCanceled(true);
            return;
        }
        if (event.getHand() == EnumHand.MAIN_HAND && ItemZero.isZero(stack)) {
            InfoType infoType = ((ItemZero) stack.getItem()).getType();
            RenderCustomItem render = ClientRenderItemEvent.customRenders[FileType.getIndex(infoType).ordinal()];
            if (!infoType.existModel() || render == null) {
                return;
            }
            if (ZeroClientPlayer.getStateMachine() == null) {
                return;
            }
            event.setCanceled(true);
            float partialTicks = event.getPartialTicks();

            BedrockAnimatedModel bedrockAnimatedModel = infoType.getAnimatedModel();
            bedrockAnimatedModel.applyAnimation(ZeroClientPlayer.getStateMachine().sampleBonePose());
            GlStateManager.pushMatrix();
            // 逆转原版施加在手上的延滞效果
            float xRotOffset = MathUtil.lerp(player.prevRenderArmPitch, player.renderArmPitch, partialTicks);
            float yRotOffset = MathUtil.lerp(player.prevRenderArmYaw, player.renderArmYaw, partialTicks);
            float xRot = MathUtil.lerp(player.prevRotationPitch, player.rotationPitch, partialTicks) - xRotOffset;
            float yRot = MathUtil.lerp(player.prevRotationYaw, player.rotationYaw, partialTicks) - yRotOffset;

            GlStateManager.rotate(xRot * -0.1F, 1F, 0F, 0F);
            GlStateManager.rotate(yRot * -0.1F, 0F, 1F, 0F);
            // 将非线性压缩 + 惯性写入模型 root（BedrockPart）
            BedrockPart rootNode = bedrockAnimatedModel.getRoot();
            if (rootNode != null) {
                xRot = (float) Math.tanh(xRot / 25) * 25f;
                yRot = (float) Math.tanh(yRot / 25.0) * 25f;
                rootNode.offsetX += yRot * 0.1F / 16F / 3F;
                rootNode.offsetY += -xRot * 0.1F / 16F / 3F;
                rootNode.additionalQuaternion.mul(Axis.XP.rotationDegrees(xRot * 0.05F));
                rootNode.additionalQuaternion.mul(Axis.YP.rotationDegrees(yRot * 0.05F));
            }
            applyJumpingSway(bedrockAnimatedModel, partialTicks);
            GlStateManager.translate(0, 1.5, 0);
            GlStateManager.rotate(180, 0, 0, 1);
            // ============ 调用自定义渲染器 ============
            bedrockAnimatedModel.setRenderHand(true);
            ShellRender.isSelf = true;
            if (RefitTransform.getOpeningProgress() != 0) {
                bedrockAnimatedModel.setRenderHand(false);
            }
            render.render(CustomItemRenderType.EQUIPPED_FIRST_PERSON, event.getItemStack(), infoType, player);
            // --- 确保 overlay 能按预期渲染：清除深度缓冲
            // 这里在模型渲染后清除深度缓冲（不会把已绘制的模型从屏幕上抹掉，
            // 只是清掉 depth buffer，使 overlay 不受 模型 污染
            // 如果启用了高清修复 就不要清空 高清修复解决了深度问题
            if (hasOptifine() && net.optifine.shaders.Shaders.shaderPackLoaded) {
            } else {
                GlStateManager.clearDepth(1.0D);
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            }
            ShellRender.isSelf = false;
            GlStateManager.popMatrix();
            bedrockAnimatedModel.cleanAnimationTransform();
        }
    }

    private static void applyJumpingSway(BedrockAnimatedModel model, float partialTicks) {
        if (jumpingTimeStamp == -1) {
            jumpingTimeStamp = System.currentTimeMillis();
        }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null) {
            double posY = MathUtil.lerp(player.prevPosY, player.posY, partialTicks);
            float velocityY = (float) (posY - player.prevPosY) / partialTicks;
            if (player.onGround) {
                if (!lastOnGround) {
                    jumpingSwayProgress = velocityY / -0.1f;
                    if (jumpingSwayProgress > 1) {
                        jumpingSwayProgress = 1;
                    }
                    lastOnGround = true;
                } else {
                    jumpingSwayProgress -= (System.currentTimeMillis() - jumpingTimeStamp) / (LANDING_SWAY_TIME * 1000);
                    if (jumpingSwayProgress < 0) {
                        jumpingSwayProgress = 0;
                    }
                }
            } else {
                if (lastOnGround) {
                    // 0.42 是玩家自然起跳的速度
                    jumpingSwayProgress = velocityY / 0.42f;
                    if (jumpingSwayProgress > 1) {
                        jumpingSwayProgress = 1;
                    }
                    lastOnGround = false;
                } else {
                    jumpingSwayProgress -= (System.currentTimeMillis() - jumpingTimeStamp) / (JUMPING_SWAY_TIME * 1000);
                    if (jumpingSwayProgress < 0) {
                        jumpingSwayProgress = 0;
                    }
                }
            }
        }
        jumpingTimeStamp = System.currentTimeMillis();
        float ySway = JUMPING_DYNAMICS.update(JUMPING_Y_SWAY * jumpingSwayProgress);
        BedrockPart rootNode = model.getRoot();
        if (rootNode != null) {
            rootNode.offsetY += -ySway / 16;
        }
    }

    //是否存在高清修复optifine
    public static boolean hasOptifine() {
        try {
            if (!isOptiFine) {
                Class.forName("net.optifine.shaders.Shaders");
                isOptiFine = true;
            }
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
