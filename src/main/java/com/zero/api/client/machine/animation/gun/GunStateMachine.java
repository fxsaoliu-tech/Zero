package com.zero.api.client.machine.animation.gun;

import com.zero.api.PlayerItemZeroDataHolder;
import com.zero.api.client.ItemFov;
import com.zero.api.client.machine.ItemZeroStateMachine;
import com.zero.api.client.machine.block.BaseStateBlock;
import com.zero.api.client.machine.block.StateBlock;
import com.zero.api.client.machine.block.gun.FireModeStateBlock;
import com.zero.api.client.machine.block.gun.GunFireStateBlock;
import com.zero.api.client.machine.block.gun.MainStateBlock;
import com.zero.api.client.machine.block.gun.MoveStateBlock;
import com.zero.api.client.machine.state.AnimationState;
import com.zero.api.client.machine.state.BaseState;
import com.zero.api.client.machine.state.gun.fire.GunFireState;
import com.zero.api.client.machine.state.gun.main.DrawState;
import com.zero.api.client.machine.state.gun.main.FinalState;
import com.zero.api.client.machine.state.gun.main.IdleState;
import com.zero.api.client.machine.state.gun.main.InspectState;
import com.zero.api.client.machine.state.gun.move.BreatheSate;
import com.zero.api.client.machine.state.gun.move.SprintSate;
import com.zero.api.client.machine.state.gun.move.WalkSate;
import com.zero.api.client.machine.EnumState;
import com.zero.client.ClientProxy;
import com.zero.client.animation.AnimationController;
import com.zero.client.animation.BoneMaskManager;
import com.zero.client.animation.json.BonePose;
import com.zero.api.client.KeepingItemRenderer;
import com.zero.client.sound.gun.GunSoundPlayManager;
import com.zero.server.item.ItemGun;
import com.zero.server.type.GunType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.*;

public class GunStateMachine extends ItemZeroStateMachine implements ItemFov {
    private final GunAnimation animation;
    public ItemGun gun;
    public GunType type;
    //动画控制器
    private final AnimationController controller;
    //状态块(包含动画生命周期)
    private final List<StateBlock> list = new ArrayList<>();
    private final Map<EnumState, AnimationState> animations = new HashMap<>();
    //剔除部分骨骼
    private final BoneMaskManager boneMaskManager = new BoneMaskManager("lefthand", "righthand");


    public GunStateMachine(ItemGun zero, PlayerItemZeroDataHolder holder) {
        super(holder, zero);
        gun = zero;
        type = zero.getType();
        animation = new GunAnimation(this);
        controller = new AnimationController(type.gunAnimationData);
        animations.putIfAbsent(EnumState.BASE, new BaseState());
        animations.putIfAbsent(EnumState.IDLE, new IdleState(this));
        animations.putIfAbsent(EnumState.DRAW, new DrawState());
        animations.putIfAbsent(EnumState.FINAL, new FinalState());
        animations.putIfAbsent(EnumState.INSPECT, new InspectState(animation));
        //移动
        animations.putIfAbsent(EnumState.BREATHE, new BreatheSate());
        animations.putIfAbsent(EnumState.WALK, new WalkSate(this));
        animations.putIfAbsent(EnumState.SPRINT, new SprintSate(this));

        animations.putIfAbsent(EnumState.SHOOT, new GunFireState());

        list.add(new BaseStateBlock(controller));
        list.add(new MainStateBlock(controller));
        list.add(new MoveStateBlock(this, controller));
        list.add(new FireModeStateBlock());
        list.add(new GunFireStateBlock());
    }

    @Override
    public void updateRender(EntityPlayer player) {
        animation.updateRender(player);
        controller.update();
        for (StateBlock state : list) {
            state.onUpdate(controller, player);
        }
    }

    @Override
    public void updateTick(EntityPlayer player) {
        animation.updateTick(player);
        for (StateBlock state : list) {
            state.onTick(controller, player);
        }
    }

    @Override
    public void tryInit(EntityPlayer player) {
        this.triggerAnimation(EnumState.DRAW);
        this.animation.playStaticTiggerAnimation(controller);
        GunSoundPlayManager.playDrawSound(ClientProxy.getMinecraft().player, this.gun, 1, 1, 16);
    }

    @Override
    public void tryExit(ItemStack stack, long putAwayTime) {
        animation.tryExit(stack, putAwayTime);
        GunSoundPlayManager.playPutAwaySound(ClientProxy.getMinecraft().player, this.gun, 1, 1, 16);
        this.triggerAnimation(EnumState.PUT_AWAY);
        this.triggerAnimation(EnumState.BREATHE);
        KeepingItemRenderer.getRenderer().keep(stack, putAwayTime);
    }

    @Override
    public void triggerAnimation(EnumState type) {
        for (StateBlock state : list) {
            state.triggerAnimation(controller, type, animations);
        }
    }

    protected AnimationController getController() {
        return controller;
    }

    @Override
    public boolean canInspect() {
        return holder.isBuy() && animation.isInspect();
    }

    @Override
    public boolean getSprinting(boolean sprinting) {
        if (this.getAimingProgress() > 0.1 || animation.banSprint()) {
            return false;
        }
        return sprinting;
    }

    @Override
    public float getWorldFov(float originalFOV) {
        return animation.getAnimation().getWorldFov(originalFOV);
    }

    @Override
    public float getHandFov(float originalFOV) {
        return animation.getAnimation().getHandFov(originalFOV);
    }

    @Override
    public double getSensitivity() {
        return animation.getAnimation().getSensitivity();
    }

    @Override
    public boolean isCloseCrossHairs() {
        return getAimingProgress() > 0.75 || animation.banSprint();
    }

    //获取瞄准进度
    @Override
    public float getAimingProgress() {
        return animation.getAnimation().getAimingProgress();
    }

    public GunAnimation getAnimation() {
        return animation;
    }

    @Override
    public Map<String, BonePose> sampleBonePose() {
        Map<String, BonePose> baseState = list.get(0).sampleBonePose(controller);
        Map<String, BonePose> mainState = list.get(1).sampleBonePose(controller);
        Map<String, BonePose> moveState = list.get(2).sampleBonePose(controller);
        Map<String, BonePose> gunFireMode = list.get(3).sampleBonePose(controller);
        Map<String, BonePose> gunFireState = list.get(4).sampleBonePose(controller);

        Map<String, BonePose> result2;
        if (mainState.isEmpty()) {
            result2 = baseState;
        } else {
            result2 = boneMaskManager.blend(boneMaskManager.applyMask(baseState), mainState);
        }
        return boneMaskManager.blend(boneMaskManager.blend(boneMaskManager.blend(result2, moveState), gunFireState), gunFireMode);
    }
}
