package com.zero.client.model;

import com.zero.client.animation.json.BonePose;
import com.zero.client.util.math.MathUtil;
import org.joml.Vector3f;

public class ConstraintObject {
    public Vector3f translationConstraint = new Vector3f(0, 0, 0);
    public Vector3f rotationConstraint = new Vector3f(0, 0, 0);

    public ConstraintObject() {

    }

    /**
     * 更新ICA姿态（动画系统调用）
     */
    public void updateFromBonePose(BonePose pose) {
        if (pose == null) return;
        //约束组的平移百分比
        if (pose.position == null) {
            return;
        }
        translationConstraint.set(pose.position[0], pose.position[1], pose.position[2]);
        //约束组的旋转百分比
        if (pose.rotation == null) {
            return;
        }
        float[] rotation = new float[3];
        if (pose.rotation.length == 4) {
            rotation = MathUtil.toEulerAngles(pose.rotation);
        }
        rotationConstraint.set(Math.toDegrees(rotation[0]), Math.toDegrees(rotation[1]), Math.toDegrees(rotation[2]));
    }


}
