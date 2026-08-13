package com.zero.api.client.machine.animation.gun.fire;

import com.zero.api.client.machine.EnumState;
import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.server.item.ItemGun;
import com.zero.server.type.mode.GunFireType;
import com.zero.server.util.ZeroTimer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class BoltFireMode implements FireMode {
    private ZeroTimer shootTime = new ZeroTimer();
    private ZeroTimer bolt = new ZeroTimer();
    private GunStateMachine stateMachine;

    @Override
    public void onUpdate(EntityPlayer player, ItemStack itemStack, ItemGun gun, GunStateMachine stateMachine, boolean leftMouseHeld, boolean lastLeftMouseHeld) {
        if(leftMouseHeld && !lastLeftMouseHeld){
            FireMode.shoot(itemStack, gun, stateMachine, true);
            shootTime.start((60000 / gun.getType().shootRpm));
            bolt.start((long) (gun.getType().boltTime * 1000));
            this.stateMachine = stateMachine;
        }
    }

    @Override
    public void renderTick() {
        if (shootTime.isRunning()) {
            if (shootTime.isFinished()) {
                shootTime.reset();
                stateMachine.triggerAnimation(EnumState.BOLT);
            }
        }
        if (bolt.isRunning()) {
            if (bolt.isFinished()) {
                bolt.reset();
            }
        }
    }

    @Override
    public boolean canShoot() {
        return !bolt.isRunning();
    }

    @Override
    public GunFireType getFireType() {
        return GunFireType.BOLT;
    }
}
