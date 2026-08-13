package com.zero.api.client.machine;

import com.zero.api.PlayerItemZeroDataHolder;
import com.zero.api.client.event.BeforeRenderHandEvent;
import com.zero.client.animation.json.BonePose;
import com.zero.client.event.ClientTickEvent;
import com.zero.client.model.BedrockAnimatedModel;
import com.zero.client.util.math.MathUtil;
import com.zero.client.util.render.GlZero;
import com.zero.server.item.ItemZero;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.joml.Quaternionf;

import java.util.Map;

public abstract class ItemZeroStateMachine {
    public PlayerItemZeroDataHolder holder;
    private ItemZero item;
    //行走瞄点
    private float walkDistAnchor = 0;


    public ItemZeroStateMachine(PlayerItemZeroDataHolder holder, ItemZero item) {
        this.holder = holder;
        this.item = item;
    }

    //在渲染线程更新
    public abstract void updateRender(EntityPlayer player);

    //逻辑层更新
    public abstract void updateTick(EntityPlayer player);

    //本状态机初始化
    public abstract void tryInit(EntityPlayer player);

    //状态机退出时间
    public abstract void tryExit(ItemStack stack, long putAwayTime);

    //输入其他动画
    public abstract void triggerAnimation(EnumState type);

    // 玩家前进键 W 是否按下
    public boolean isInputUp() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        return player != null && player.movementInput != null && player.movementInput.forwardKeyDown;
    }

    // 玩家后退键 S 是否按下
    public boolean isInputDown() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        return player != null && player.movementInput != null && player.movementInput.backKeyDown;
    }

    // 玩家左移键 A 是否按下
    public boolean isInputLeft() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        return player != null && player.movementInput != null && player.movementInput.leftKeyDown;
    }

    // 玩家右移键 D 是否按下
    public boolean isInputRight() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        return player != null && player.movementInput != null && player.movementInput.rightKeyDown;
    }

    // 玩家跳跃键 Space 是否按下
    public boolean isInputJumping() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        return player != null && player.movementInput != null && player.movementInput.jump;
    }

    // 玩家是否匍匐（Shift）
    public boolean isCrawl() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        return player != null && player.movementInput != null && player.movementInput.sneak;
    }

    // 玩家是否接触地面
    public boolean isOnGround() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        return player != null && !player.onGround;
    }

    public void anchorWalkDist() {
        this.walkDistAnchor = this.getCurrentWalkDist();
    }

    private float getCurrentWalkDist() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        return player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * ClientTickEvent.partialTicks;
    }

    public float getMoveProgress() {
        return this.getCurrentWalkDist() - this.walkDistAnchor;
    }

    /**
     * 应用状态机的世界摄像机动画，暂时只用于玩家
     */
    public void applyLevelCameraAnimation(EntityViewRenderEvent.CameraSetup event, EntityPlayerSP player) {
        this.applyLevelCameraAnimation(event, 1);
    }

    public void applyLevelCameraAnimation(EntityViewRenderEvent.CameraSetup event, float multiplier) {
        BedrockAnimatedModel model = item.getType().getAnimatedModel();
        if (model == null) {
            return;
        }
        Quaternionf q = MathUtil.multiplyQuaternion(model.getCameraAnimated().getRotationQuaternion(), multiplier);
        double yaw = Math.asin(2 * (q.w() * q.y() - q.x() * q.z()));
        double pitch = Math.atan2(2 * (q.w() * q.x() + q.y() * q.z()), 1 - 2 * (q.x() * q.x() + q.y() * q.y()));
        double roll = Math.atan2(2 * (q.w() * q.z() + q.x() * q.y()), 1 - 2 * (q.y() * q.y() + q.z() * q.z()));
        yaw = Math.toDegrees(yaw);
        pitch = Math.toDegrees(pitch);
        roll = Math.toDegrees(roll);
        event.setYaw((float) yaw + event.getYaw());
        event.setPitch((float) pitch + event.getPitch());
        event.setRoll((float) roll + event.getRoll());
    }

    /**
     * 应用状态机的手持物品摄像机动画，暂时只用于玩家
     */
    public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, EntityPlayerSP player) {
        this.applyItemInHandCameraAnimation(event, 1);
    }

    public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, float multiplier) {
        BedrockAnimatedModel model = item.getType().getAnimatedModel();
        if (model == null) {
            return;
        }
        Quaternionf q = MathUtil.multiplyQuaternion(model.getCameraAnimated().getRotationQuaternion(), multiplier);
        GlZero.applyQuaternion(q);
        model.cleanCameraAnimationTransform();
    }

    //可以检视
    public abstract boolean canInspect();

    //是否需要对跑步特殊处理
    public abstract boolean getSprinting(boolean sprinting);

    //是否隐藏十字星
    public abstract boolean isCloseCrossHairs();

    //最终输出的动画骨骼
    public abstract Map<String, BonePose> sampleBonePose();

}
