package com.zero.client.model;


import com.zero.Zero;
import com.zero.api.client.ItemFov;
import com.zero.api.client.KeepingItemRenderer;
import com.zero.api.client.ZeroClientPlayer;
import com.zero.api.client.machine.ItemZeroStateMachine;
import com.zero.client.model.bedrock.BedrockPart;
import com.zero.client.model.bedrock.BedrockPartWrapper;
import com.zero.client.model.json.BedrockJson;
import com.zero.client.model.functional.BeamRenderer;
import com.zero.client.render.CustomItemRenderType;
import com.zero.client.util.EnumTexturesType;
import com.zero.client.util.ZeroResources;
import com.zero.client.util.render.GlZero;
import com.zero.server.item.ItemAttachment;
import com.zero.server.item.ItemZero;
import com.zero.server.type.AttachmentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BedrockAttachmentModel extends BedrockAnimatedModel {
    private static final ResourceLocation COLOR = new ResourceLocation(Zero.MOD_ID, "textures/default_laser.png");
    private static final String SCOPE_VIEW_NODE = "scope_view";
    private static final String SCOPE_BODY_NODE = "scope_body";
    private static final String OCULAR_RING_NODE = "ocular_ring";
    private static final String DIVISION_NODE = "division";
    private static final String OCULAR_NODE = "ocular";

    protected @Nullable List<BedrockPart> scopeViewPath;
    protected @Nullable List<BedrockPart> scopeBodyPath;
    protected @Nullable List<BedrockPart> ocularRingPath;
    protected @Nullable List<BedrockPart> ocularNodePath;
    protected @Nullable List<BedrockPart> divisionNodePath;

    private static final Pattern LASER_BEAM_PATTERN = Pattern.compile("^laser_beam(_(\\d+))?$");

    protected @Nullable List<List<BedrockPart>> laserBeamPaths;

    private boolean isScope = false;
    private boolean isSight = false;

    private ItemStack stack;

    private static final FloatBuffer MODEL_VIEW_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final Matrix4f MODEL_VIEW_MATRIX4F = new Matrix4f();

    public BedrockAttachmentModel(BedrockJson json) {
        super(json);
        laserBeamPaths = new ArrayList<>();
        scopeViewPath = getPath(this.getBedrockPartWrapper(SCOPE_VIEW_NODE));
        scopeBodyPath = getPath(this.getBedrockPartWrapper(SCOPE_BODY_NODE));
        ocularRingPath = getPath(this.getBedrockPartWrapper(OCULAR_RING_NODE));
        ocularNodePath = getPath(this.getBedrockPartWrapper(OCULAR_NODE));
        divisionNodePath = getPath(this.getBedrockPartWrapper(DIVISION_NODE));
        if (divisionNodePath != null) {
            divisionNodePath.get(divisionNodePath.size() - 1).visible = false;
        }
        for (Map.Entry<String, BedrockPartWrapper> entry : modelMap.entrySet()) {
            if (LASER_BEAM_PATTERN.matcher(entry.getKey()).find()) {
                laserBeamPaths.add(getPath(entry.getValue()));
            }
        }
    }

    @Nullable
    public List<BedrockPart> getScopeViewPath() {
        return scopeViewPath;
    }

    public void setIsScope(boolean isScope) {
        if (isScope) {
            this.isSight = false;
            this.isScope = true;
        } else {
            this.isSight = true;
            this.isScope = false;
        }
    }

    public void setItemStack(@Nullable ItemStack stack) {
        this.stack = stack;
    }


    @Override
    public void render(CustomItemRenderType type) {
        //先短暂使用false
        if (type == CustomItemRenderType.EQUIPPED_FIRST_PERSON) {
            if (isScope) {
                renderScope(type);
            } else if (isSight) {
                renderSight(type);
            } else {
                super.render(type);
            }
        } else {
            if (scopeBodyPath != null) {
                renderTempPartThird(scopeBodyPath);
            }
            if (ocularRingPath != null) {
                renderTempPartThird(ocularRingPath);
            }
            super.render(type);
        }
        if (!isScope && !isSight && laserBeamPaths != null && !laserBeamPaths.isEmpty()) {
            BeamRenderer.renderLaserBeam(stack, laserBeamPaths.get(0));
        }
    }

    private void renderSight(CustomItemRenderType type) {
        GlZero.enableItemEntityStencilTest();
        if (ocularNodePath != null) {
            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(false);
            // 清空模板缓冲区、准备绘制模板缓冲
            GL11.glClearStencil(0);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
            GL11.glStencilMask(0xFF);
            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
            // 绘制目镜
            renderTempPart(ocularNodePath);
            // 恢复渲染状态
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            GL11.glDepthMask(true);
            GL11.glColorMask(true, true, true, true);
        }
        // 渲染划分
        if (divisionNodePath != null) {
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            renderTempPart(divisionNodePath);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        GlZero.disableItemEntityStencilTest();
        // 渲染其他部分
        if (scopeBodyPath != null) {
            renderTempPart(scopeBodyPath);
        }
        super.render(type);
    }

    private void renderScope(CustomItemRenderType type) {
        GlZero.enableItemEntityStencilTest();
        // 清空模板缓冲区、准备绘制模板缓冲
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        if (ocularRingPath != null) {
            GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            // 渲染目镜外环
            renderTempPart(ocularRingPath);
        }
        if (ocularNodePath != null) {
            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(false);
            GL11.glStencilMask(0xFF);
            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_REPLACE, GL11.GL_REPLACE);
            // 绘制目镜
            renderTempPart(ocularNodePath);
            // 恢复渲染状态
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            GL11.glDepthMask(true);
            GL11.glColorMask(true, true, true, true);
        }
        if (scopeBodyPath != null) {
            GL11.glStencilFunc(GL11.GL_EQUAL, 0, 0xFF);
            renderTempPart(scopeBodyPath);
        }
        if (ocularNodePath != null) {
            // 渲染圆形模板层前
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(false);
            drawCircularLayer(type);
            GL11.glDepthMask(true);
            GL11.glColorMask(true, true, true, true);
        }
        //渲染目镜黑色遮罩
        if (ocularNodePath != null) {
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            renderTempPart(ocularNodePath);
        }
        // 渲染划分
        if (divisionNodePath != null) {
            GL11.glStencilFunc(GL11.GL_EQUAL, 2, 0xFF);
            renderTempPart(divisionNodePath);
        }
        GL11.glStencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        GlZero.disableItemEntityStencilTest();
        super.render(type);
    }

    //渲染圆形模板层
    private void drawCircularLayer(CustomItemRenderType type) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        Vector3f ocularCenter = getBedrockPartCenter(ocularNodePath);
        float centerX = ocularCenter.x * 16 * 90;
        float centerY = ocularCenter.y * 16 * 90;
        // 80是一个随便找的大小合适的数值。
        float scopeViewRadiusModifier = 1;
        float rad = 80 * scopeViewRadiusModifier;
        if (Minecraft.getMinecraft().player != null) {
            ItemStack stack = KeepingItemRenderer.getRenderer().getCurrentItem();
            if (ItemZero.isZero(stack)) {
                ItemZeroStateMachine machine = ZeroClientPlayer.getStateMachine();
                if (machine != null) {
                    if (machine instanceof ItemFov) {
                        rad *= ((ItemFov) machine).getAimingProgress();
                    }
                }
            }
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(COLOR);

        bufferBuilder.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(centerX, centerY, -90).tex(0, 0).endVertex();
        for (int i = 0; i <= 90; i++) {
            float angle = (float) i * ((float) Math.PI * 2F) / 90.0f;
            float sin = MathHelper.sin(angle);
            float cos = MathHelper.cos(angle);
            bufferBuilder.pos(centerX + cos * rad, centerY + sin * rad, -90).tex(0, 0).endVertex();
        }

        tessellator.draw();

        if (stack != null && !stack.isEmpty()) {
            if (stack.getItem() instanceof ItemAttachment) {
                ItemAttachment attachment = (ItemAttachment) stack.getItem();
                AttachmentType attachmentType = attachment.getType();
                ResourceLocation resourceLocation = ZeroResources.getTextures(EnumTexturesType.ATTACHMENT, attachment.getType().texture);
                if (type != CustomItemRenderType.EQUIPPED_FIRST_PERSON && attachmentType.attachmentModelLod != null) {
                    resourceLocation = ZeroResources.getLodTextures(EnumTexturesType.ATTACHMENT, attachment.getType().textureLod);
                }
                Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
            }
        }
    }

    private Vector3f getBedrockPartCenter(List<BedrockPart> path) {
        GlStateManager.pushMatrix();
        for (BedrockPart part : path) {
            part.applyTranslateAndRotate();
        }
        MODEL_VIEW_BUFFER.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER);
        MODEL_VIEW_BUFFER.rewind();
        MODEL_VIEW_MATRIX4F.set(MODEL_VIEW_BUFFER);
        GlStateManager.popMatrix();
        return new Vector3f(MODEL_VIEW_MATRIX4F.m30(), MODEL_VIEW_MATRIX4F.m31(), MODEL_VIEW_MATRIX4F.m32());
    }

    private void renderTempPart(List<BedrockPart> path) {
        if (path == null) {
            return;
        }
        GlStateManager.pushMatrix();
        for (int i = 0; i < path.size() - 1; i++) {
            path.get(i).applyTranslateAndRotate();
        }
        BedrockPart part = path.get(path.size() - 1);
        part.visible = true;
        part.render(CustomItemRenderType.EQUIPPED_FIRST_PERSON);
        part.visible = false;
        GlStateManager.popMatrix();
    }

    private void renderTempPartThird(List<BedrockPart> path) {
        if (path == null) {
            return;
        }
        GlStateManager.pushMatrix();
        for (int i = 0; i < path.size() - 1; i++) {
            path.get(i).applyTranslateAndRotate();
        }
        BedrockPart part = path.get(path.size() - 1);
        part.visible = true;
        part.render(CustomItemRenderType.EQUIPPED_THIRD_PERSON);
        part.visible = false;
        GlStateManager.popMatrix();
    }

}