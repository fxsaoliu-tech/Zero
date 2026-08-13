package com.zero.api.client.machine.animation.gun;

import com.zero.Zero;
import com.zero.client.ClientProxy;
import com.zero.client.event.ClientHudRender;
import com.zero.client.gui.config.ZeroConfig;
import com.zero.server.item.ItemGun;
import com.zero.server.type.IScope;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;

public class AimAnimation {
    private final Minecraft mc = ClientProxy.getMinecraft();
    // 当前瞄准进度 0~1
    private float aimProgress = 0f;
    // 当前是否处于瞄准目标状态
    private boolean aiming = false;
    // 是否已经恢复默认镜头状态
    private boolean restored = true;
    // 上一帧渲染时间
    private long lastFrameNs = -1;
    // 瞄准动画总时长（纳秒）
    private long durationNs;
    // 当前镜头
    private IScope currentScope;
    // 缩放倍率
    private float zoomLevel;
    // FOV 缩放倍率
    private float fovZoomLevel;
    // 鼠标右键状态
    private boolean rightMouseHeld;
    private boolean lastRightMouseHeld;
    //第三人称切换到第一人称
    private boolean is;
    // false = 点击切换
    // true  = 按住瞄准
    public static boolean aimMode = ZeroConfig.aim;

    public AimAnimation() {
    }

    /**
     * 输入处理
     */
    private boolean canProcessInput() {
        return mc != null && mc.player != null && !mc.player.isDead && mc.currentScreen == null;
    }

    public void onInput(EntityPlayer player) {
        if (!canProcessInput()) {
            rightMouseHeld = false;
            lastRightMouseHeld = false;
            stopAiming();
            return;
        }
        aimMode = ZeroConfig.aim;
        ItemStack stack = player.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemGun) {
            ItemGun item = (ItemGun) stack.getItem();
            IScope scope = item.getType().getCurrentScope(stack);
            if (scope != currentScope) {
                currentScope = scope;
                // 获取镜头参数
                zoomLevel = Math.max(currentScope.getViewFov(), 70);
                fovZoomLevel = Math.max(currentScope.getFOVFactor(), 1.0f);
                // 转换为纳秒
                durationNs = (long) (Math.max(currentScope.getAimSpeed(), 0.05f) * 1_000_000_000L);
                lastFrameNs = System.nanoTime();
            }
        }
        lastRightMouseHeld = rightMouseHeld;
        rightMouseHeld = mc.gameSettings.keyBindUseItem.isKeyDown();
        boolean nextAiming;
        if (aimMode) {
            nextAiming = rightMouseHeld;
        } else {
            nextAiming = aiming;
            if (rightMouseHeld && !lastRightMouseHeld) {
                nextAiming = !aiming;
            }
        }
        if (rightMouseHeld && player.isSprinting()) {
            player.setSprinting(false);
        }
        aiming = nextAiming;
        if (aiming) {
            if (mc.gameSettings.thirdPersonView >= 1 && !is) {
                is = true;
                mc.gameSettings.thirdPersonView = 0;
            }
        } else {
            if (mc.gameSettings.thirdPersonView == 0 && is) {
                is = false;
                mc.gameSettings.thirdPersonView = 1;
            }
        }
    }

    /**
     * 每帧渲染更新
     */
    public void updateCameraRender() {
        if (mc == null || mc.entityRenderer == null || mc.gameSettings == null || currentScope == null || lastFrameNs == -1) {
            return;
        }
        if (mc.currentScreen != null || mc.player == null || mc.player.isDead) {
            stopAiming();
        }
        // 当前时间
        long now = System.nanoTime();
        // 计算帧时间差
        float delta = (now - lastFrameNs) / (float) durationNs;
        // 更新上一帧时间
        lastFrameNs = now;
        // 推进或回退瞄准进度
        aimProgress = MathHelper.clamp(aiming ? aimProgress + delta : aimProgress - delta, 0f, 1f);
        // 完全退出瞄准
        if (aimProgress <= 0.001f) {
            aimProgress = 0f;
            // 只恢复一次
            if (!restored) {
                restored = true;
            }
        }
    }

    //获取世界fov缩放
    public float getWorldFov(float originalFOV) {
        if (currentScope == null) {
            return originalFOV;
        }
        float targetFov = originalFOV / fovZoomLevel;
        float targetFactor = targetFov / originalFOV;
        return originalFOV * (1.0f + (targetFactor - 1.0f) * aimProgress);
    }

    //获取手部模型的fov缩放
    public float getHandFov(float originalFOV) {
        if (currentScope == null) {
            return originalFOV;
        }
        return (float) MathHelper.clampedLerp(originalFOV, zoomLevel, aimProgress);
    }

    //获取开镜的鼠标移动系数
    public double getSensitivity() {
        if (currentScope == null) {
            return 1;
        }
        // 开镜灵敏度系数
        double sensitivityMultiplier = ZeroConfig.sensitivityMultiplier;
        sensitivityMultiplier = 1 + (sensitivityMultiplier - 1) * aimProgress;
        // 两种状态下的 fov 计算
        float originalFov = Minecraft.getMinecraft().gameSettings.fovSetting;
        float currentFov = getWorldFov(originalFov);
        // 荧幕距离系数，MC 和 COD 一样使用 MDV 标准，默认为 MDV133（系数为 1.33）
        double coefficient = ZeroConfig.coefficient;
        double a = Math.atan(Math.tan(Math.toRadians(currentFov * 0.5)) * coefficient);
        double b = Math.atan(Math.tan(Math.toRadians(originalFov * 0.5)) * coefficient);
        return (a / b) * sensitivityMultiplier;
    }

    /**
     * 停止瞄准
     */
    public void stopAiming() {
        aiming = false;
    }

    /**
     * 获取当前瞄准进度
     */
    public float getAimingProgress() {
        if (aimProgress < 0.01f) {
            return 0f;
        }
        return aimProgress;
    }
}