package com.zero.api.client.machine.animation.gun.fire;

import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.server.util.ZeroTimer;
import com.zero.server.item.ItemGun;
import com.zero.server.type.mode.GunFireType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class AutoFireMode implements FireMode {
    private ZeroTimer shootTime = new ZeroTimer();

    public AutoFireMode() {

    }

    @Override
    public void onUpdate(EntityPlayer player, ItemStack itemStack, ItemGun gun, GunStateMachine stateMachine, boolean leftMouseHeld, boolean lastLeftMouseHeld) {
        if (leftMouseHeld) {
            FireMode.shoot(itemStack, gun, stateMachine, true);
            shootTime.start((60000 / gun.getType().shootRpm));
        }
    }

    @Override
    public void renderTick() {
        if (shootTime.isRunning()) {
            if (shootTime.isFinished()) {
                shootTime.reset();
            }
        }
    }

    @Override
    public boolean canShoot() {
        return !shootTime.isRunning();
    }

    @Override
    public GunFireType getFireType() {
        return GunFireType.AUTO;
    }
}
