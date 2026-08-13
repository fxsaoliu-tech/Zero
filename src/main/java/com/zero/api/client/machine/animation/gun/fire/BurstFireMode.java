package com.zero.api.client.machine.animation.gun.fire;

import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.server.item.ItemGun;
import com.zero.server.type.mode.GunFireType;
import com.zero.server.util.ZeroTimer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class BurstFireMode implements FireMode {
    private ZeroTimer burstCooldown = new ZeroTimer();
    private ZeroTimer shootTime = new ZeroTimer();
    private int burstShotsLeft = 0;

    private ItemStack itemStack;
    private ItemGun itemGun;
    private GunStateMachine gunStateMachine;

    @Override
    public void onUpdate(EntityPlayer player, ItemStack itemStack, ItemGun gun, GunStateMachine stateMachine, boolean leftMouseHeld, boolean lastLeftMouseHeld) {
        if (leftMouseHeld && !lastLeftMouseHeld) {
            burstShotsLeft = Math.min(gun.getCurrentAmmo(itemStack), 3);
            shootTime.start((60000L / gun.getType().shootRpm) * burstShotsLeft + burstShotsLeft * 35L);
            this.itemStack = itemStack;
            this.itemGun = gun;
            this.gunStateMachine = stateMachine;
        }
    }

    @Override
    public void renderTick() {
        if (shootTime.isRunning()) {
            if (shootTime.isFinished()) {
                shootTime.reset();
                itemGun = null;
                itemStack = null;
                gunStateMachine = null;
            }
        }
        if (burstCooldown.isRunning()) {
            if (burstCooldown.isFinished()) {
                burstCooldown.reset();
            }
        }
        if (itemStack == null || itemGun == null || gunStateMachine == null) {
            return;
        }
        if (burstShotsLeft > 0 && !burstCooldown.isRunning()) {
            burstShotsLeft--;
            FireMode.shoot(itemStack, itemGun, gunStateMachine, true);
            burstCooldown.start((60000 / itemGun.getType().shootRpm) + 25);
        }
    }

    @Override
    public boolean canShoot() {
        return !shootTime.isRunning();
    }

    @Override
    public GunFireType getFireType() {
        return GunFireType.BURST;
    }
}
