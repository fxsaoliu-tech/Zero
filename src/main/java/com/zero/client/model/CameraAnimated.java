package com.zero.client.model;

import com.zero.client.animation.json.BonePose;
import com.zero.client.model.bedrock.BedrockPartWrapper;
import com.zero.client.util.math.MathUtil;
import org.joml.Quaternionf;

/**
 * 统一处理摄像机动画
 */
public class CameraAnimated {
    /**
     * 摄像机节点对应的模型（可选）
     */
    public BedrockPartWrapper cameraRenderer;

    /**
     * 摄像机旋转四元数（世界空间）
     */
    public Quaternionf rotationQuaternion = new Quaternionf(0f, 0f, 0f, 1f);

    public CameraAnimated() {
    }

    /**
     * 更新摄像机姿态（动画系统调用）
     */
    public void updateFromBonePose(BonePose pose) {
        if (pose == null) return;
        if (pose.rotation == null) return;
        float[] rotation = pose.rotation;
        rotationQuaternion.set(rotation[0], rotation[1], rotation[2], rotation[3]);
    }

    /**
     * 获取摄像机四元数（渲染层使用）
     */
    public Quaternionf getRotationQuaternion() {
        return rotationQuaternion;
    }
}
