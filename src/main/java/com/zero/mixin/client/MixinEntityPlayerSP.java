package com.zero.mixin.client;

import com.zero.api.PlayerItemZeroDataHolder;
import com.zero.api.client.ZeroClientPlayer;
import com.zero.api.client.machine.ItemZeroStateMachine;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP implements ZeroClientPlayer {
    @Unique
    private final EntityPlayerSP player = (EntityPlayerSP) (Object) this;
    @Unique
    private final PlayerItemZeroDataHolder dataHolder = new PlayerItemZeroDataHolder(player);


    @Inject(method = "onUpdate", at = @At("HEAD"))
    public void onUpdateDataHolder(CallbackInfo ci) {
        EntityPlayerSP player = (EntityPlayerSP) (Object) this;
        if (player.world.isRemote) {
            dataHolder.onUpdate();
        }
    }

    @Unique
    @Override
    public void onUpdateRender() {
        dataHolder.updatePut();
    }

    @ModifyArg(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setSprinting(Z)V"), index = 0)
    private boolean modifySprintingArg(boolean sprinting) {
        if (sprinting) {
            sprinting = dataHolder.getSprinting(sprinting);
        }
        return sprinting;
    }

    @Unique
    @Override
    public ItemZeroStateMachine getItemZero() {
        return dataHolder.getStateMachine();
    }

    @Unique
    @Override
    public void clearDate() {
        dataHolder.clear();
    }
}