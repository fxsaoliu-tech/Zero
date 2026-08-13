package com.zero.client.render;

import com.zero.Zero;
import com.zero.client.model.BedrockAmmoModel;
import com.zero.client.model.bedrock.BedrockModel;
import com.zero.client.util.EnumTexturesType;
import com.zero.client.util.ZeroResources;
import com.zero.client.util.math.MathUtil;
import com.zero.server.entity.EntityBullet;
import com.zero.server.file.FileList;
import com.zero.server.item.ItemGun;
import com.zero.server.type.BulletType;
import com.zero.server.type.GunType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.joml.Vector3f;

public class RenderBullet extends Render<EntityBullet> {
    private static final ResourceLocation Default = new ResourceLocation(Zero.MOD_ID, "textures/default_bullet.png");
    private static BedrockModel tracer_bullet;

    public RenderBullet(RenderManager renderManager) {
        super(renderManager);
        shadowSize = 0.5F;
    }

    public void render(EntityBullet bullet, double x, double y, double z, float entityYaw, float partialTicks) {
        BulletType type = bullet.getBulletType();
        if (type == null) {
            return;
        }
        if (tracer_bullet == null) {
            tracer_bullet = new BedrockModel(Zero.server.loadModel(new FileList("default", "default_bullet", null), "default_bullet"));
        }
        BedrockAmmoModel model = type.ammoModel;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(MathUtil.lerp(bullet.prevRotationYaw, bullet.rotationYaw, partialTicks) - 180f, 0.0F, 1, 0);
        GlStateManager.rotate(MathUtil.lerp(bullet.prevRotationPitch, bullet.rotationPitch, partialTicks), 1, 0, 0);
        GlStateManager.translate(0F, 1.5F, 0F);
        GlStateManager.scale(-1F, -1F, 1F);
        if (type.color != null) {
            GlStateManager.color(type.color[0], type.color[1], type.color[2], 1);
        }
        if (bullet.tickAir > 1) {
            if (model != null) {
                bindEntityTexture(bullet);
                model.render(CustomItemRenderType.ENTITY);
            } else {
                bindTexture(Default);
                tracer_bullet.render(CustomItemRenderType.ENTITY);
            }
        }
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();
    }


    @Override
    public void doRender(EntityBullet entity, double x, double y, double z, float entityYaw, float partialTicks) {
        render(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    public ResourceLocation getEntityTexture(EntityBullet entity) {
        if (entity.getBulletType() == null) {
            return null;
        }
        return ZeroResources.getTextures(EnumTexturesType.BULLET, entity.getBulletType().texture);
    }

    public static class Factory implements IRenderFactory<EntityBullet> {
        @Override
        public Render<EntityBullet> createRenderFor(RenderManager manager) {
            return new RenderBullet(manager);
        }
    }
}
