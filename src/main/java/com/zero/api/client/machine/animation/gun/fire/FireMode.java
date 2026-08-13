package com.zero.api.client.machine.animation.gun.fire;

import com.zero.Zero;
import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.api.client.machine.EnumState;
import com.zero.client.ClientProxy;
import com.zero.client.event.ClientTickEvent;
import com.zero.client.model.functional.MuzzleFlashRender;
import com.zero.client.model.functional.ShellRender;
import com.zero.client.render.RenderGun;
import com.zero.client.sound.gun.GunSoundPlayManager;
import com.zero.client.util.ZeroResources;
import com.zero.network.gun.PacketGunFire;
import com.zero.server.item.ItemGun;
import com.zero.server.type.AttachmentType;
import com.zero.server.type.GunType;
import com.zero.server.type.mode.GunFireType;
import com.zero.server.type.mode.IAttachmentType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface FireMode {

    void onUpdate(EntityPlayer player, ItemStack itemStack, ItemGun gun, GunStateMachine stateMachine, boolean leftMouseHeld, boolean lastLeftMouseHeld);

    void renderTick();

    static void shoot(ItemStack itemStack, ItemGun gun, GunStateMachine stateMachine, boolean canPlaySound) {
        GunType gunType = gun.getType();
        RenderGun.shootTimeStamp = System.currentTimeMillis();

        if (gunType.getAnimatedModel() != null) {
            ShellRender shellRender = gunType.getAnimatedModel().getShellRender(0);
            if (shellRender != null) {
                shellRender.addShell(gunType.shellText.getRandomVelocity());
            }
        }
        boolean silenced = false;
        boolean muzzleFlash = true;
        AttachmentType attachmentType = gunType.getAttachment(itemStack, IAttachmentType.MUZZLE);
        if (attachmentType != null) {
            silenced = attachmentType.silencer;
            muzzleFlash = attachmentType.muzzleFlash;
            if (silenced) {
                muzzleFlash = false;
            }
        }
        if (muzzleFlash) {
            MuzzleFlashRender.onShoot();
        }
        EntityPlayer player = ClientProxy.getMinecraft().player;
        if (canPlaySound) {
            GunSoundPlayManager.playShootSound(player, gun, silenced);
        }
        stateMachine.triggerAnimation(EnumState.SHOOT);
        gun.shootClient(itemStack);
        ClientTickEvent.RECOIL_SYSTEM.addRecoil(player, gunType.getRecoilVertical(itemStack), gunType.getRecoilHorizontal(itemStack));
        Zero.getPacketHandler().sendToServer(new PacketGunFire(gunType.shootRpm, stateMachine.getAimingProgress(), gun.getType().getFireType(itemStack)));
    }

    boolean canShoot();

    GunFireType getFireType();

}
