package com.zero.client.model.functional;

import com.zero.client.model.BedrockAttachmentModel;
import com.zero.client.model.BedrockGunModel;
import com.zero.client.render.CustomItemRenderType;
import com.zero.client.util.EnumTexturesType;
import com.zero.client.util.ZeroResources;
import com.zero.server.type.AttachmentType;
import com.zero.server.type.GunType;
import com.zero.server.type.mode.IAttachmentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.EnumMap;

public class AttachmentRender implements IFunctionalRenderer {
    private final BedrockGunModel bedrockGunModel;
    private final IAttachmentType attachmentType;

    public AttachmentRender(BedrockGunModel bedrockGunModel, IAttachmentType type) {
        this.bedrockGunModel = bedrockGunModel;
        this.attachmentType = type;
    }


    public static void renderAttachment(ItemStack attachmentItem, ItemStack gunItem, CustomItemRenderType customItemRenderType) {
        GlStateManager.translate(0, -1.5, 0);
        AttachmentType type = AttachmentType.getFromItemStack(attachmentItem);
        if (type == null) {
            return;
        }
        BedrockAttachmentModel attachmentModel = type.getAnimatedModel();
        ResourceLocation texture = ZeroResources.getTextures(EnumTexturesType.ATTACHMENT, type.texture);
        if (attachmentModel == null) {
            return;
        }

        if (customItemRenderType != CustomItemRenderType.EQUIPPED_FIRST_PERSON && type.attachmentModelLod != null) {
            attachmentModel = type.attachmentModelLod;
            texture = ZeroResources.getTextures(EnumTexturesType.ATTACHMENT, type.texture);
        }

        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        attachmentModel.render(customItemRenderType);

        GunType gunType = GunType.getGunType(gunItem);

        if (gunType != null) {
            ResourceLocation location = ZeroResources.getTextures(EnumTexturesType.GUN, gunType.texture);
            if (customItemRenderType != CustomItemRenderType.EQUIPPED_FIRST_PERSON && gunType.modelLod != null) {
                location = ZeroResources.getLodTextures(EnumTexturesType.GUN, gunType.textureLod);
            }
            Minecraft.getMinecraft().renderEngine.bindTexture(location);
        }
    }

    @Override
    public void render(CustomItemRenderType type) {
        EnumMap<IAttachmentType, ItemStack> currentAttachmentItem = bedrockGunModel.getCurrentAttachmentItem();
        ItemStack attachmentItem = currentAttachmentItem.get(attachmentType);
        if (attachmentItem == null || attachmentItem.isEmpty()) return;
        FloatBuffer capturedModelView = captureCurrentModelView();

        bedrockGunModel.delegateRender((delegateLight) -> {
            GlStateManager.pushMatrix();
            capturedModelView.rewind();
            GL11.glLoadMatrix(capturedModelView);
            renderAttachment(attachmentItem, bedrockGunModel.getCurrentGunItem(), type);
            GlStateManager.popMatrix();
        });
    }

    private static FloatBuffer captureCurrentModelView() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
        buffer.rewind();
        return buffer;
    }
}
