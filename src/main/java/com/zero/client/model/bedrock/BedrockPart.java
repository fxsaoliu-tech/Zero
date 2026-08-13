package com.zero.client.model.bedrock;

import com.zero.client.render.CustomItemRenderType;
import com.zero.client.util.render.GlZero;
import com.zero.server.type.InfoType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BedrockPart {
    @javax.annotation.Nullable
    public final String name;                        // 部件名称
    public final List<BedrockCube> cubes = new ArrayList<>();    // 包含的立方体
    public final List<BedrockPart> children = new ArrayList<>(); // 子部件
    public float x, y, z;                           // 旋转中心点
    public float xRot, yRot, zRot;                  // 旋转角度（弧度）
    public float offsetX, offsetY, offsetZ;         // 位置偏移
    public boolean visible = true;                  // 是否可见
    public boolean illuminated = false;             // 是否发光
    //父骨骼
    public BedrockPart parent;
    //动画相关数据
    public Quaternionf additionalQuaternion = new Quaternionf(0, 0, 0, 1);
    public float xScale = 1;
    public float yScale = 1;
    public float zScale = 1;

    public BedrockPart(@javax.annotation.Nullable String name) {
        this.name = name;
    }


    /**
     * 设置旋转中心点
     */
    public void setPos(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * 设置旋转角度（弧度）
     */
    public void setRotate(float x, float y, float z) {
        this.xRot = x;
        this.yRot = y;
        this.zRot = z;
    }

    /**
     * 添加子部件
     */
    public void addChild(BedrockPart model) {
        this.children.add(model);
    }

    public void render(CustomItemRenderType type) {
        if (!this.visible) return;

        GlStateManager.pushMatrix();

        // 保存原光照（注意类型：OpenGlHelper.lastBrightnessX/Y 在 1.12 中是 floats）
        float lastX = OpenGlHelper.lastBrightnessX;
        float lastY = OpenGlHelper.lastBrightnessY;

        boolean doIlluminate = this.illuminated;
        try {
            if (doIlluminate) {
                // 关闭标准光照，让顶点颜色/纹理全亮
                GlStateManager.disableLighting();
                // 将 lightmap 推到最大（240,240 是 Minecraft 的最大亮度）
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
            }
            applyTranslateAndRotate();
            if (!this.cubes.isEmpty()) {
                compile();
            }
            for (BedrockPart child : this.children) {
                child.render(type);
            }
        } finally {
            if (doIlluminate) {
                // 恢复 lightmap（注意使用 lightmapTexUnit）
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
                // 恢复标准光照
                GlStateManager.enableLighting();
            }
            GlStateManager.popMatrix();
        }
    }

    /**
     * 正确的变换顺序
     */
    public void applyTranslateAndRotate() {
        // 1. 应用整体偏移（父坐标系下的位置）
        GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);
        // 2. 平移到旋转中心
        GlStateManager.translate(this.x / 16f, this.y / 16f, this.z / 16f);
        // 3. 应用旋转（Z->Y->X）
        if (this.zRot != 0.0F) GlStateManager.rotate((float) Math.toDegrees(this.zRot), 0, 0, 1);
        if (this.yRot != 0.0F) GlStateManager.rotate((float) Math.toDegrees(this.yRot), 0, 1, 0);
        if (this.xRot != 0.0F) GlStateManager.rotate((float) Math.toDegrees(this.xRot), 1, 0, 0);
        // 旋转只用四元数
        GlZero.applyQuaternion(additionalQuaternion);
        // 5. 缩放
        GlStateManager.scale(this.xScale, this.yScale, this.zScale);
    }

    /**
     * 统一为本部件的所有立方体执行begin/draw（每个部件一次begin/draw）
     */
    public void compile() {
        if (this.cubes.isEmpty()) {
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
        for (BedrockCube cube : this.cubes) {
            cube.compile(builder);
        }
        tessellator.draw();
    }

    public BedrockPart getParent() {
        return parent;
    }
}