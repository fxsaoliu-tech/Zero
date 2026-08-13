package com.zero.api.third;

import com.zero.server.item.ItemGun;
import com.zero.server.type.mode.GunFireType;
import com.zero.server.type.tab.EnumTabType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class GunThirdPersonManager {

    public static void setRotationAnglesHead(Entity entity1, ModelRenderer rightArm, ModelRenderer leftArm, ModelRenderer body, ModelRenderer head) {
        if (Minecraft.getMinecraft().isGamePaused()) {
            return;
        }
        if (entity1 instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) entity1;
            ItemStack itemStack = entity.getHeldItemMainhand();
            if (itemStack.isEmpty() || !(itemStack.getItem() instanceof ItemGun)) {
                return;
            }
            if (entity instanceof EntityPlayerSP) {
                EntityPlayerSP player = (EntityPlayerSP) entity;
                if (player.isSpectator() || player.isElytraFlying() || player.isInWater() || player.isInLava() || player.isOnLadder() || player.isPlayerSleeping()) {
                    return;
                }
            }
            ItemGun gun = (ItemGun) itemStack.getItem();

            GunFireType gunFireType = gun.getType().getFireType(itemStack);
            if (gunFireType == GunFireType.GATLING && gun.getType().enumTabType == EnumTabType.MACHINE) {
                defaultMachineHold(entity, rightArm, leftArm, body, head);
            } else {
                defaultHold(entity, rightArm, leftArm, body, head);
            }
        }
    }


    public static void defaultHold(Entity entity, ModelRenderer rightArm, ModelRenderer leftArm, ModelRenderer body, ModelRenderer head) {
        rightArm.rotateAngleY = -0.1F + head.rotateAngleY;
        leftArm.rotateAngleY = 0.1F + head.rotateAngleY + 0.4F;
        rightArm.rotateAngleX = -1.57F + head.rotateAngleX;
        leftArm.rotateAngleX = -1.57F + head.rotateAngleX;
    }

    public static void defaultMachineHold(Entity entity, ModelRenderer rightArm, ModelRenderer leftArm, ModelRenderer body, ModelRenderer head) {
        body.rotateAngleY = head.rotateAngleY + 0.8f;

        double cosTheta = Math.cos(-body.rotateAngleY);
        double sinTheta = Math.sin(-body.rotateAngleY);

        float x = rightArm.rotationPointX;
        rightArm.rotationPointX = (float) (x * cosTheta);
        rightArm.rotationPointZ = (float) (x * sinTheta);

        rightArm.rotateAngleY = -1.0F + body.rotateAngleY;
        rightArm.rotateAngleX = -0.1F + body.rotateAngleX;

        float x2 = leftArm.rotationPointX;
        leftArm.rotationPointX = (float) (x2 * cosTheta);
        leftArm.rotationPointZ = (float) (x2 * sinTheta);

        leftArm.rotateAngleY = -0.1F + body.rotateAngleY;
        leftArm.rotateAngleX = -1F + body.rotateAngleX;
    }

}
