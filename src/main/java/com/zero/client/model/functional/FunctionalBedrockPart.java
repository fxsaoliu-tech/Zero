package com.zero.client.model.functional;

import com.zero.client.model.bedrock.BedrockPart;
import com.zero.client.render.CustomItemRenderType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;

import javax.annotation.Nullable;

public class FunctionalBedrockPart extends BedrockPart {
    public IFunctionalRenderer function;

    public FunctionalBedrockPart(IFunctionalRenderer function, @Nullable String name) {
        super(name);
        this.function = function;
    }

    public FunctionalBedrockPart(IFunctionalRenderer function, BedrockPart part) {
        super(part.name);
        this.function = function;
        this.cubes.addAll(part.cubes);
        this.children.addAll(part.children);
        this.x = part.x;
        this.y = part.y;
        this.z = part.z;
        this.xRot = part.xRot;
        this.yRot = part.yRot;
        this.zRot = part.zRot;
        this.offsetX = part.offsetX;
        this.offsetY = part.offsetY;
        this.offsetZ = part.offsetZ;
        this.visible = part.visible;
        this.illuminated = part.illuminated;
        this.xScale = part.xScale;
        this.yScale = part.yScale;
        this.zScale = part.zScale;
    }

    @Override
    public void render(CustomItemRenderType type) {
        GlStateManager.pushMatrix();
        applyTranslateAndRotate();
        if (function != null) {
            function.render(type);
        } else {
            if (this.visible) {
                float lastX = OpenGlHelper.lastBrightnessX;
                float lastY = OpenGlHelper.lastBrightnessY;

                boolean doIlluminate = this.illuminated;
                    if (doIlluminate) {
                        // 关闭标准光照，让顶点颜色/纹理全亮
                        GlStateManager.disableLighting();
                        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
                    }
                super.compile();
                for (BedrockPart child : this.children) {
                    child.render(type);
                }
                if (doIlluminate) {
                    // 恢复 lightmap（注意使用 lightmapTexUnit）
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
                    // 恢复标准光照
                    GlStateManager.enableLighting();
                }
            }
        }
        GlStateManager.popMatrix();
    }

}
