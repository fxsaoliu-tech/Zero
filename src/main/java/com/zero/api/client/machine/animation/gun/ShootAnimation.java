package com.zero.api.client.machine.animation.gun;

import com.zero.api.client.machine.animation.gun.fire.*;
import com.zero.client.ClientProxy;
import com.zero.client.sound.gun.GunSoundPlayManager;
import com.zero.client.util.ZeroResources;
import com.zero.server.util.ZeroTimer;
import com.zero.server.item.ItemGun;
import com.zero.server.type.GunType;
import com.zero.server.type.mode.GunFireType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ShootAnimation {
    // 鼠标左键状态
    private boolean leftMouseHeld;
    private boolean lastLeftMouseHeld;
    //当前使用的
    private FireMode fireMode;
    //这是用来播放空仓音效的
    private boolean dryFireTriggeredThisPress = true;
    private GunAnimation animation;

    private ZeroTimer Sprinting = new ZeroTimer();

    public ShootAnimation(GunAnimation animation) {
        this.animation = animation;
        //默认使用的
        fireMode = new AutoFireMode();
    }


    public void renderOnUpdate() {
        fireMode.renderTick();
        if (Sprinting.isFinished()) {
            Sprinting.reset();
        }

    }

    public void onInput(EntityPlayer player) {
        if (!canShoot() || !animation.canReload() || Sprinting.isRunning()) {
            return;
        }
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemGun)) {
            return;
        }
        ItemGun gun = (ItemGun) stack.getItem();
        GunType type = gun.getType();
        GunFireType fireType = type.getFireType(stack);


        leftMouseHeld = ClientProxy.getMinecraft().gameSettings.keyBindAttack.isKeyDown();

        if (fireMode.getFireType() != fireType) {
            fireMode = getFireMode(fireType);
        }

        //如果在奔跑 要开枪 那么就停止奔跑 给予延迟开火权限
        if (leftMouseHeld) {
            if (player.isSprinting()) {
                player.setSprinting(false);
                Sprinting.start((long) (type.sprintShootTime * 1000));
                return;
            }
        }

        if (leftMouseHeld && isNoBullet(gun, stack, player)) {
            animation.stopInspect();
            fireMode.onUpdate(player, stack, gun, animation.stateMachine, leftMouseHeld, lastLeftMouseHeld);
        }

        //重置锁定的空仓音效
        if (!leftMouseHeld) {
            dryFireTriggeredThisPress = true;
        }
        lastLeftMouseHeld = leftMouseHeld;
    }

    private FireMode getFireMode(GunFireType fireType) {
        switch (fireType) {
            case AUTO:
                return new AutoFireMode();
            case SEMI:
                return new SemiFireMode();
            case BURST:
                return new BurstFireMode();
            case BOLT:
                return new BoltFireMode();
        }
        return new AutoFireMode();
    }

    //是否没有子弹 没有子弹自动播放音效
    private boolean isNoBullet(ItemGun gun, ItemStack itemStack, EntityPlayer player) {
        if (gun.getCurrentAmmo(itemStack) < 1) {
            if (dryFireTriggeredThisPress) {
                dryFireTriggeredThisPress = false;
                GunSoundPlayManager.playDryFireSound(player, gun.getType());
            }
            return false;
        }
        return true;
    }

    public boolean canShoot() {
        return fireMode.canShoot();
    }


}
