package com.zero.mixin.client;


import com.zero.client.event.ClientRenderItemEvent;
import com.zero.client.model.BedrockAnimatedModel;
import com.zero.client.render.CustomItemRenderType;
import com.zero.client.render.RenderCustomItem;
import com.zero.server.file.FileType;
import com.zero.server.item.ItemZero;
import com.zero.server.type.InfoType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerHeldItem.class)
public class MixinLayerHeldItem {


    @Inject(method = "renderHeldItem", at = @At("HEAD"), cancellable = true)
    private void tick(EntityLivingBase entityLivingBase, ItemStack itemStack, ItemCameraTransforms.TransformType type, EnumHandSide handSide, CallbackInfo ci) {
        ci.cancel();
        if (!itemStack.isEmpty()) {
            GlStateManager.pushMatrix();

            if (entityLivingBase.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            this.translateToHand(handSide);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            boolean flag = handSide == EnumHandSide.LEFT;
            GlStateManager.translate((float) (flag ? -1 : 1) / 16.0F, 0.125F, -0.625F);

            Item item = itemStack.getItem();
            if (item instanceof ItemZero) {
                InfoType infoType = ((ItemZero) item).getType();
                RenderCustomItem render = ClientRenderItemEvent.customRenders[FileType.getIndex(infoType).ordinal()];
                if (infoType.existModel() && render != null) {
                    if (handSide == EnumHandSide.RIGHT) {
                        BedrockAnimatedModel model = infoType.getAnimatedModel();
                        model.setRenderHand(false);
                        GlStateManager.translate(0.0F, 1.5, 0.0F);
                        GlStateManager.rotate(180.0F, 0.0F, 0, 1);
                        render.render(CustomItemRenderType.EQUIPPED_THIRD_PERSON, itemStack, infoType, null);
                    }
                } else {
                    Minecraft.getMinecraft().getItemRenderer().renderItemSide(entityLivingBase, itemStack, type, flag);
                }
            } else {
                if (!(handSide == EnumHandSide.LEFT && ItemZero.isZero(entityLivingBase.getHeldItem(EnumHand.MAIN_HAND)))) {
                    Minecraft.getMinecraft().getItemRenderer().renderItemSide(entityLivingBase, itemStack, type, flag);
                }
            }
            GlStateManager.popMatrix();
        }
    }

    @Shadow
    protected void translateToHand(EnumHandSide handSide) {
    }


}
