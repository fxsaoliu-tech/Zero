package com.zero.client.model.functional;

import com.zero.client.model.BedrockGunModel;
import com.zero.client.model.bedrock.BedrockModel;
import com.zero.client.model.display.ShellText;
import com.zero.client.render.CustomItemRenderType;
import com.zero.client.util.EnumTexturesType;
import com.zero.client.util.ZeroResources;
import com.zero.server.type.GunType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ShellRender implements IFunctionalRenderer {
    // 抛壳队列
    private final ConcurrentLinkedDeque<Data> SHELL_QUEUE = new ConcurrentLinkedDeque<>();
    public static boolean isSelf = false;

    private final BedrockGunModel bedrockGunModel;

    public ShellRender(BedrockGunModel bedrockGunModel) {
        this.bedrockGunModel = bedrockGunModel;
    }

    public void addShell(Vector3f randomVelocity) {
        if (SHELL_QUEUE.size() > 128) {
            SHELL_QUEUE.pollFirst();
        }
        double xRandom = Math.random() * randomVelocity.x();
        double yRandom = Math.random() * randomVelocity.y();
        double zRandom = Math.random() * randomVelocity.z();
        Vector3f vector3f = new Vector3f((float) xRandom, (float) yRandom, (float) zRandom);
        SHELL_QUEUE.offerLast(new Data(System.currentTimeMillis(), vector3f));
    }

    private void checkShellQueue(long lifeTime) {
        if (!SHELL_QUEUE.isEmpty()) {
            Data data = SHELL_QUEUE.peekFirst();
            if ((System.currentTimeMillis() - data.timeStamp) > lifeTime) {
                SHELL_QUEUE.pollFirst();
                checkShellQueue(lifeTime);
            }
        }
    }

    private void renderShell(GunType gunType, BedrockGunModel gunModel) {
        ShellText shellTextEjection = gunType.shellText;
        if (shellTextEjection == null) {
            SHELL_QUEUE.clear();
            return;
        }
        BedrockModel model = shellTextEjection.getShellModel();
        if (model == null) {
            return;
        }
        long lifeTime = (long) (shellTextEjection.getLivingTime() * 1000);
        // 检查有没有需要踢出去的队列
        checkShellQueue(lifeTime);
        // 各种参数的获取
        Vector3f initialVelocity = shellTextEjection.getInitialVelocity();
        Vector3f acceleration = shellTextEjection.getAcceleration();
        Vector3f angularVelocity = shellTextEjection.getAngularVelocity();

        // 缓存一下 PoseStack
        for (Data data : SHELL_QUEUE) {
            if (data.modelView == null) {
                data.modelView = captureCurrentModelView();
            }
        }
        gunModel.delegateRender(a -> {
            SHELL_QUEUE.forEach(data -> renderSingleShell(data, initialVelocity, acceleration, angularVelocity, model, ZeroResources.getTextures(EnumTexturesType.SHELL,shellTextEjection.getShellTexture())));
        });
    }

    private void renderSingleShell(Data data, Vector3f initialVelocity, Vector3f acceleration, Vector3f angularVelocity, BedrockModel model, ResourceLocation location) {
        // 再检查一次
        if (data.modelView == null) {
            return;
        }
        // 获取存留时间和各种参数
        long remindTime = System.currentTimeMillis() - data.timeStamp;
        double time = remindTime / 1000.0;
        Vector3f randomOffset = data.randomOffset;

        // 位移，满足标准的匀变速直线运动
        double x = (initialVelocity.x() + randomOffset.x()) * time + 0.5 * acceleration.x() * time * time;
        double y = (initialVelocity.y() + randomOffset.y()) * time + 0.5 * acceleration.y() * time * time;
        double z = (initialVelocity.z() + randomOffset.z()) * time + 0.5 * acceleration.z() * time * time;

        // 旋转
        double xw = time * angularVelocity.x();
        double yw = time * angularVelocity.y();
        double zw = time * angularVelocity.z();
        GlStateManager.pushMatrix();

        // 恢复抛壳瞬间记录的模型矩阵
        loadModelView(data.modelView);

        // 位移
        GlStateManager.translate(-x, -y, z);

        // 旋转
        GlStateManager.rotate((float) xw, -1F, 0F, 0F);
        GlStateManager.rotate((float) yw, 0F, -1F, 0F);
        GlStateManager.rotate((float) zw, 0F, 0F, 1F);

        GlStateManager.translate(0F, -1.5F, 0F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(location);

        model.render(null);
        GlStateManager.popMatrix();
    }

    private Matrix4f captureCurrentModelView() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
        buffer.rewind();
        return new Matrix4f().set(buffer);
    }

    private void loadModelView(Matrix4f modelView) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        modelView.get(buffer);
        buffer.rewind();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(buffer);
    }

    @Override
    public void render(CustomItemRenderType type) {
        if (!isSelf) {
            return;
        }
        GunType gunType = GunType.getGunType(bedrockGunModel.getCurrentGunItem());
        if (gunType == null) {
            return;
        }
        this.renderShell(gunType, bedrockGunModel);
    }

    public static class Data {
        public final long timeStamp;
        public final Vector3f randomOffset;
        public Matrix4f modelView = null;

        public Data(long timeStamp, Vector3f randomOffset) {
            this.timeStamp = timeStamp;
            this.randomOffset = randomOffset;
        }
    }

}
