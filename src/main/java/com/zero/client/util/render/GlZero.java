package com.zero.client.util.render;

import com.zero.client.event.ClientRenderItemEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GlZero {
    private static final FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final Matrix4f TMP_MATRIX = new Matrix4f();

    public static void applyQuaternion(Quaternionf q) {
        if (q.w == 1f && q.x == 0f && q.y == 0f && q.z == 0f) {
            return;
        }
        MATRIX_BUFFER.clear();

        TMP_MATRIX.identity();
        TMP_MATRIX.rotation(q);
        TMP_MATRIX.get(MATRIX_BUFFER);

        GL11.glMultMatrix(MATRIX_BUFFER);
    }


    public static boolean enableItemEntityStencilTest() {
        if (ClientRenderItemEvent.hasOptifine() && net.optifine.shaders.Shaders.shaderPackLoaded) {
            int depthTextureId = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);
            int stencilAttachmentType = GL30.glGetFramebufferAttachmentParameteri(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
            if (depthTextureId != GL11.GL_NONE && stencilAttachmentType == GL11.GL_NONE) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTextureId);
                int dataType = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_DEPTH_TYPE);
                if (dataType == GL30.GL_UNSIGNED_NORMALIZED) {
                    int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
                    int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
                    // 将原来的 Depth Texture 重新定义为 Depth24 + Stencil8
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH24_STENCIL8, width, height, 0, GL30.GL_DEPTH_STENCIL, GL30.GL_UNSIGNED_INT_24_8, (ByteBuffer) null);
                    // 重新挂载
                    GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL11.GL_TEXTURE_2D, depthTextureId, 0);
                }
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            }
        } else {
            Framebuffer framebuffer = Minecraft.getMinecraft().getFramebuffer();
            if (!framebuffer.isStencilEnabled()) {
                framebuffer.enableStencil();
            }
        }
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        return true;
    }

    public static void disableItemEntityStencilTest() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

}
