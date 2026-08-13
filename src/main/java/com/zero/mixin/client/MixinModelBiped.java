package com.zero.mixin.client;

import com.zero.api.client.KeepingItemRenderer;
import com.zero.api.third.GunThirdPersonManager;
import com.zero.server.item.ItemZero;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public class MixinModelBiped {
    @Shadow
    public ModelRenderer bipedHead;
    @Shadow
    public ModelRenderer bipedBody;
    @Shadow
    public ModelRenderer bipedRightArm;
    @Shadow
    public ModelRenderer bipedLeftArm;

    @Inject(method = "setRotationAngles", at = @At(value = "TAIL"))
    public void setRotationAnglesTail(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo ci) {
        if (ageInTicks == 0) {
            if (!(entityIn instanceof EntityPlayer)) {
                return;
            }
        }
        GunThirdPersonManager.setRotationAnglesHead(entityIn, bipedRightArm, bipedLeftArm, bipedBody, bipedHead);
        if (!(entityIn instanceof EntityPlayer)) {
            return;
        }
        // 自己的第一人称渲染系统
        ItemStack currentItem = KeepingItemRenderer.getRenderer().getCurrentItem();
        // 1.12.2 第一人称判断
        if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            if (currentItem != ItemStack.EMPTY) {
                if (currentItem.getItem() instanceof ItemZero) {
                    reset(bipedRightArm);
                    reset(bipedLeftArm);
                }
            }
        }
    }


    @Unique
    public void reset(ModelRenderer part) {
        part.rotateAngleX = 0.0F;
        part.rotateAngleY = 0.0F;
        part.rotateAngleZ = 0.0F;
    }

}
