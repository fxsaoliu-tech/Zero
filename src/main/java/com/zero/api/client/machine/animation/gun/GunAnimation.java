package com.zero.api.client.machine.animation.gun;

import com.zero.Zero;
import com.zero.api.client.machine.EnumState;
import com.zero.client.ClientProxy;
import com.zero.client.animation.AnimationController;
import com.zero.client.animation.AnimationPlayType;
import com.zero.client.animation.data.AnimationStateText;
import com.zero.client.sound.gun.GunSoundPlayManager;
import com.zero.server.item.ItemGun;
import com.zero.server.util.ZeroTimer;
import com.zero.network.gun.PacketGunReload;
import com.zero.server.type.GunType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class GunAnimation {
    public final GunStateMachine stateMachine;
    private final ItemGun itemGun;
    private final AimAnimation animation;
    private final GunType gunType;
    private final EntityPlayer player;

    private final ShootAnimation shoot;
    //记录一下存不存在检视情况
    public boolean inspect = false;
    //重载
    private ZeroTimer reloadTimer = new ZeroTimer();
    //切换快慢机
    private ZeroTimer triggerTimer = new ZeroTimer();


    public GunAnimation(GunStateMachine stateMachine) {
        this.stateMachine = stateMachine;
        this.itemGun = stateMachine.gun;
        this.animation = new AimAnimation();
        this.gunType = stateMachine.type;
        this.player = ClientProxy.getMinecraft().player;
        this.shoot = new ShootAnimation(this);
    }

    public void updateTick(EntityPlayer player) {
        if (stateMachine.holder.isBuy()) {
            animation.onInput(player);

            if (reloadTimer.isRunning()) {
                if (reloadTimer.isFinished()) {
                    long reloadEndTime = (long) (itemGun.getCurrentAmmo(player.getHeldItemMainhand()) < 1 ? gunType.reloadEmptyTime : gunType.reloadTacticalTime) * 1000;
                    Zero.getPacketHandler().sendToServer(new PacketGunReload(reloadEndTime));
                    reloadTimer.reset();
                }
            }
            if (triggerTimer.isRunning()) {
                if (triggerTimer.isFinished()) {
                    triggerTimer.reset();
                    this.playStaticTiggerAnimation(stateMachine.getController());
                }
            }
        }
    }

    public void updateRender(EntityPlayer player) {
        animation.updateCameraRender();
        if (stateMachine.holder.isBuy()) {
            shoot.renderOnUpdate();
            shoot.onInput(player);
        }
    }

    public void tryExit(ItemStack stack, long putAwayTime) {
        animation.stopAiming();
    }

    public void stopInspect() {
        stateMachine.triggerAnimation(EnumState.INSPECT_RETREAT);
    }


    //重载弹夹播放的音效
    public void playReloadAnimation(int track, AnimationController controller) {
        closeSprint();
        ItemStack stack = player.getHeldItemMainhand();
        if (itemGun.getCurrentAmmo(stack) < 1) {
            controller.runAnimation(track, AnimationStateText.RELOAD_EMPTY, AnimationPlayType.PLAY_ONCE_STOP, 0.2f);
        } else {
            controller.runAnimation(track, AnimationStateText.RELOAD_TACTICAL, AnimationPlayType.PLAY_ONCE_STOP, 0.2f);
        }
        GunSoundPlayManager.playReloadSound(player, itemGun, stack, 1, 1, 16);
    }

    //播放检视
    public void playBoltAnimation(int track, AnimationController controller) {
        GunSoundPlayManager.playBoltSound(player, itemGun, 1, 1, 16);
        controller.runAnimation(track, AnimationStateText.BOLT, AnimationPlayType.PLAY_ONCE_STOP, 0.2f);
    }

    //播放检视的
    public void playInspectAnimation(int track, AnimationController controller) {
        closeSprint();
        ItemStack stack = player.getHeldItemMainhand();
        if (itemGun.getCurrentAmmo(stack) < 1) {
            controller.runAnimation(track, AnimationStateText.INSPECT_EMPTY, AnimationPlayType.PLAY_ONCE_STOP, 0.2f);
        } else {
            controller.runAnimation(track, AnimationStateText.INSPECT, AnimationPlayType.PLAY_ONCE_STOP, 0.2f);
        }
        GunSoundPlayManager.playInspectSound(player, itemGun, stack, 1, 1, 16);
    }

    //播放切换扳机
    public void playTiggerAnimation(int track, AnimationController controller) {
        closeSprint();
        stopInspect();
        gunType.switchFireType(player.getHeldItemMainhand());
        switch (gunType.getFireType(player.getHeldItemMainhand())) {
            case SEMI:
                controller.runAnimation(track, AnimationStateText.SEMI, AnimationPlayType.PLAY_ONCE_STOP, 0);
            case BURST:
                controller.runAnimation(track, AnimationStateText.BURST, AnimationPlayType.PLAY_ONCE_STOP, 0);
            case AUTO:
                controller.runAnimation(track, AnimationStateText.AUTO, AnimationPlayType.PLAY_ONCE_STOP, 0);
                break;
        }
        triggerTimer.start((long) (0.3 * 1000));
        GunSoundPlayManager.playFireSelectSound(player, gunType);
    }

    public void playStaticTiggerAnimation(AnimationController controller) {
        String name = "static_semi";
        switch (gunType.getFireType(player.getHeldItemMainhand())) {
            case BURST:
                name = "static_burst";
                break;
            case SEMI:
                name = "static_semi";
                break;
            case AUTO:
                name = "static_auto";
                break;
        }
        controller.runAnimation(3, name, AnimationPlayType.PLAY_ONCE_STOP, 0);
    }

    private void closeSprint() {
        if (player.isSprinting()) {
            stateMachine.triggerAnimation(EnumState.WALK);
            player.setSprinting(false);
        }
    }

    public boolean banSprint() {
        return inspect || reloadTimer.isRunning();
    }


    public void beginReload() {
        reloadTimer.start((long) ((itemGun.getCurrentAmmo(player.getHeldItemMainhand()) < 1 ? gunType.reloadEmptyTime : gunType.reloadTacticalTime) * 1000));
    }

    public boolean canReload() {
        return !reloadTimer.isRunning() && canShoot();
    }

    public boolean canShoot() {
        return shoot.canShoot();
    }

    public boolean canTrigger() {
        return !triggerTimer.isRunning() && canReload();
    }

    public boolean isInspect() {
        return canReload() && shoot.canShoot();
    }

    public AimAnimation getAnimation() {
        return animation;
    }
}
