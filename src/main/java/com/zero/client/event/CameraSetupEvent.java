package com.zero.client.event;

import com.zero.Zero;
import com.zero.api.client.ItemFov;
import com.zero.api.client.KeepingItemRenderer;
import com.zero.api.client.ZeroClientPlayer;
import com.zero.api.client.event.BeforeRenderHandEvent;
import com.zero.api.client.machine.ItemZeroStateMachine;
import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.client.util.math.SecondOrderDynamics;
import com.zero.server.item.ItemZero;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CameraSetupEvent {
    public static boolean worldOrHand;

    public CameraSetupEvent() {
        Zero.eventRegister(this);
    }

    @SubscribeEvent
    public void renderCameraView(EntityViewRenderEvent.CameraSetup cameraSetup) {
        if (!Minecraft.getMinecraft().gameSettings.viewBobbing) {
            return;
        }
        ZeroClientPlayer player = ZeroClientPlayer.getPlayer();
        if (player == null) {
            return;
        }
        ItemStack stack = KeepingItemRenderer.getRenderer().getCurrentItem();
        if (ItemZero.isZero(stack)) {
            ItemZeroStateMachine machine = ZeroClientPlayer.getStateMachine();
            if (machine != null) {
                machine.applyLevelCameraAnimation(cameraSetup, (EntityPlayerSP) player);
            }
        }
    }

    @SubscribeEvent
    public void beforeRenderHand(BeforeRenderHandEvent event) {
        if (!Minecraft.getMinecraft().gameSettings.viewBobbing) {
            return;
        }
        ZeroClientPlayer player = ZeroClientPlayer.getPlayer();
        if (player == null) {
            return;
        }
        ItemStack stack = KeepingItemRenderer.getRenderer().getCurrentItem();
        if (ItemZero.isZero(stack)) {
            ItemZeroStateMachine machine = ZeroClientPlayer.getStateMachine();
            if (machine != null) {
                machine.applyItemInHandCameraAnimation(event, (EntityPlayerSP) player);
            }
        }
    }

    @SubscribeEvent
    public void applyScopeFov(EntityViewRenderEvent.FOVModifier fovModifier) {
        ZeroClientPlayer player = ZeroClientPlayer.getPlayer();
        if (player == null) {
            return;
        }
        float originalFOV = fovModifier.getFOV();
        ItemStack stack = KeepingItemRenderer.getRenderer().getCurrentItem();
        if (ItemZero.isZero(stack)) {
            ItemZeroStateMachine machine = ZeroClientPlayer.getStateMachine();
            if (machine instanceof ItemFov) {
                ItemFov itemFov = (ItemFov) machine;
                if (worldOrHand) {
                    fovModifier.setFOV(itemFov.getWorldFov((originalFOV)));
                } else {
                    fovModifier.setFOV(itemFov.getHandFov((originalFOV)));
                }
            }
        }
    }

}
