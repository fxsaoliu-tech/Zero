package com.zero.client.render;

import com.zero.client.event.ClientRenderItemEvent;
import com.zero.client.model.BedrockAnimatedModel;
import com.zero.server.file.FileType;
import com.zero.server.item.ItemZero;
import com.zero.server.type.InfoType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderEntityItemZero extends RenderEntityItem {

    public RenderEntityItemZero(RenderManager renderManager, RenderItem renderItem) {
        super(renderManager, renderItem);
    }

    @Override
    public void doRender(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ItemStack stack = entity.getItem();

        if (stack.getItem() instanceof ItemZero) {
            InfoType infoType = ((ItemZero) stack.getItem()).getType();
            RenderCustomItem render = ClientRenderItemEvent.customRenders[FileType.getIndex(infoType).ordinal()];
            if (infoType.existModel() && render != null) {
                BedrockAnimatedModel model = infoType.getAnimatedModel();
                model.setRenderHand(false);
                GlStateManager.pushMatrix();
                float hover = MathHelper.sin((entity.ticksExisted + partialTicks) / 10.0F) * 0.1F + 0.1F;
                GlStateManager.translate(x, y + 0.25D + hover, z);
                GlStateManager.rotate(entity.ticksExisted + partialTicks, 0F, 1F, 0F);
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,1.5,0);
                GlStateManager.rotate(180, 0, 0, 1);
                render.render(CustomItemRenderType.ENTITY, stack, infoType, null);
                GlStateManager.popMatrix();
                GlStateManager.popMatrix();
            }
        } else {
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    public static class Factory implements IRenderFactory<EntityItem> {

        @Override
        public Render<? super EntityItem> createRenderFor(RenderManager manager) {
            return new RenderEntityItemZero(manager, Minecraft.getMinecraft().getRenderItem());
        }
    }

}
