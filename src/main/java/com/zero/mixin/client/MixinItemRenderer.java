package com.zero.mixin.client;

import com.zero.client.ClientProxy;
import com.zero.api.client.event.BeforeRenderHandEvent;
import com.zero.api.client.KeepingItemRenderer;
import com.zero.server.item.ItemZero;
import net.minecraft.client.renderer.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer implements KeepingItemRenderer {
    @Shadow
    private ItemStack itemStackMainHand;
    @Shadow
    private float equippedProgressMainHand;
    @Shadow
    private float prevEquippedProgressMainHand;
    @Unique
    private ItemStack zero_KeepItem;
    @Unique
    private long zero_KeepTimeMs;
    @Unique
    private long zero_KeepTimestamp;

    //给第一人称物品施加摄像机的功能
    @Inject(method = "renderItemInFirstPerson*", at = @At("HEAD"))
    public void beforeHandRender(float partialTicks,CallbackInfo ci){
        MinecraftForge.EVENT_BUS.post(new BeforeRenderHandEvent());
    }

    /**
     * 在渲染手中物品前打印日志
     */
    @Inject(method = "updateEquippedItem", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (ClientProxy.getMinecraft().player == null) {
            return;
        }
        if (zero_KeepItem != null) {
            long time = System.currentTimeMillis() - zero_KeepTimestamp;
            if (time < zero_KeepTimeMs) {
                equippedProgressMainHand = 1.0f;
                prevEquippedProgressMainHand = 1.0f;
                itemStackMainHand = zero_KeepItem;
                return;
            }
        }
        ItemStack itemStack = ClientProxy.getMinecraft().player.getHeldItemMainhand();
        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemZero) {
            equippedProgressMainHand = 1.0f;
            prevEquippedProgressMainHand = 1.0f;
            itemStackMainHand = itemStack;
        }
    }

    @Unique
    @Override
    public void keep(ItemStack itemStack, long timeMs) {
        long time = System.currentTimeMillis() - zero_KeepTimestamp;
        if (time < zero_KeepTimeMs) {
            return;
        }
        this.zero_KeepTimeMs = timeMs;
        this.zero_KeepTimestamp = System.currentTimeMillis();
        this.zero_KeepItem = itemStack;
        this.itemStackMainHand = itemStack;
    }

    @Unique
    @Override
    public ItemStack getCurrentItem() {
        if (ClientProxy.getMinecraft().player == null) {
            return itemStackMainHand;
        }
        if (zero_KeepItem != null) {
            long time = System.currentTimeMillis() - zero_KeepTimestamp;
            if (time < zero_KeepTimeMs) {
                return zero_KeepItem;
            } else {
                zero_KeepItem = null;
            }
        }
        return itemStackMainHand;
    }
}
