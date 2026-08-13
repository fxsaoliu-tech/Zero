package com.zero.mixin.client;

import com.zero.api.client.ItemFov;
import com.zero.api.client.ZeroClientPlayer;
import com.zero.api.client.event.RenderItemInHandBobEvent;
import com.zero.api.client.event.RenderLevelBobEvent;
import com.zero.api.client.machine.ItemZeroStateMachine;
import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.client.event.CameraSetupEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    @Unique
    private boolean tacz$useFovSetting;
    @Final
    @Shadow
    private Minecraft mc;

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void onBobHurt(float partialTicks, CallbackInfo ci) {
        // 触发其他事件
        boolean cancel;
        if (!tacz$useFovSetting) {
            cancel = MinecraftForge.EVENT_BUS.post(new RenderItemInHandBobEvent.BobHurt());
        } else {
            cancel = MinecraftForge.EVENT_BUS.post(new RenderLevelBobEvent.BobHurt());
        }
        if (cancel) {
            ci.cancel();
        }
    }


    @Inject(method = "applyBobbing", at = @At("HEAD"), cancellable = true)
    private void onBobView(float partialTicks, CallbackInfo ci) {
        boolean cancel;
        if (!tacz$useFovSetting) {
            cancel = MinecraftForge.EVENT_BUS.post(new RenderItemInHandBobEvent.BobView());
        } else {
            cancel = MinecraftForge.EVENT_BUS.post(new RenderLevelBobEvent.BobView());
        }
        if (cancel) {
            ci.cancel();
        }
    }

    @Inject(method = "getFOVModifier", at = @At("HEAD"))
    private void getFOV(float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Float> cir) {
        tacz$useFovSetting = useFOVSetting;
        CameraSetupEvent.worldOrHand = tacz$useFovSetting;
    }

    //在开镜情况下修改玩家的转动视角
    @ModifyArgs(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;turn(FF)V"))
    private void modifyTurn(Args args) {
        float yaw = args.get(0);
        float pitch = args.get(1);
        ItemZeroStateMachine stateMachine = ZeroClientPlayer.getStateMachine();
        if (stateMachine != null) {
            if (stateMachine instanceof ItemFov) {
                float ads = (float) ((ItemFov) stateMachine).getSensitivity();
                yaw *= ads;
                pitch *= ads;
            }
        }
        args.set(0, yaw);
        args.set(1, pitch);
    }
}