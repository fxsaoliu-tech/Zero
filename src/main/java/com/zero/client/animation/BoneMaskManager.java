package com.zero.client.animation;

import com.zero.client.animation.json.BonePose;
import com.zero.client.model.constant.AnimatedConstant;
import com.zero.client.util.math.MathUtil;

import java.util.*;

public class BoneMaskManager {
    private final List<String> list = new ArrayList<>();

    public BoneMaskManager(String... name) {
        list.addAll(Arrays.asList(name));
    }


    /**
     * 用来屏蔽部分骨骼
     *
     * @param src 源
     * @return 返回处理后的骨骼
     */
    public Map<String, BonePose> applyMask(Map<String, BonePose> src) {
        Map<String, BonePose> dst = new HashMap<>();
        for (Map.Entry<String, BonePose> entry : src.entrySet()) {
            // 去除list的骨骼
            if (!list.contains(entry.getKey())) {
                dst.put(entry.getKey(), entry.getValue());
            }
        }
        return dst;
    }

    /**
     * 混合两个骨骼集合（默认混合逻辑）
     *
     * @param a 动作 A 的骨骼
     * @param b 动作 B 的骨骼
     * @return 混合后的骨骼集合
     */
    public Map<String, BonePose> blend(Map<String, BonePose> a, Map<String, BonePose> b) {
        Map<String, BonePose> result = new HashMap<>();
        for (Map.Entry<String, BonePose> entry : a.entrySet()) {
            String key = entry.getKey();
            BonePose poseA = entry.getValue().copy();
            if (b.containsKey(key)) {
                BonePose poseB = b.get(key).copy();
                if (key.equalsIgnoreCase(AnimatedConstant.CONSTRAINT_NODE)) {
                    result.put(key, blendConstraintPose(poseA, poseB));
                } else if (key.equalsIgnoreCase(AnimatedConstant.CAMERA)) {
                    result.put(key, blendCameraPose(poseA, poseB));
                } else {
                    result.put(key, blendPoseData(poseA, poseB));
                }
            } else {
                if (key.equalsIgnoreCase(AnimatedConstant.CONSTRAINT_NODE)) {
                    result.put(key, constraintPose(poseA));
                } else if (key.equalsIgnoreCase(AnimatedConstant.CAMERA)) {
                    result.put(key, cameraPose(poseA));
                } else {
                    result.put(key, poseA);
                }
            }
        }
        // 添加 b 中 a 没有的骨骼
        for (Map.Entry<String, BonePose> entry : b.entrySet()) {
            String key = entry.getKey();
            if (!result.containsKey(key)) {
                result.put(key, entry.getValue().copy());
            }
        }
        return result;
    }

    private BonePose blendPoseData(BonePose a, BonePose b) {
        BonePose result = new BonePose();
        // 位置叠加
        if (a.position != null && b.position != null) {
            result.position = MathUtil.add(a.position, b.position);
        } else if (a.position != null) {
            result.position = a.position;
        } else if (b.position != null) {
            result.position = b.position;
        }
        // 缩放取最大值
        if (a.scale != null && b.scale != null) {
            result.scale = MathUtil.multiply(a.scale, b.scale);
        } else if (a.scale != null) {
            result.scale = a.scale;
        } else if (b.scale != null) {
            result.scale = b.scale;
        }
        // 旋转四元数叠加
        if (a.rotation != null && b.rotation != null) {
            result.rotation = MathUtil.toQuaternion(MathUtil.add(MathUtil.toEulerAngles(a.rotation), MathUtil.toEulerAngles(b.rotation)));
        } else if (a.rotation != null) {
            result.rotation = a.rotation;
        } else if (b.rotation != null) {
            result.rotation = b.rotation;
        }
        return result;
    }

    private BonePose blendCameraPose(BonePose a, BonePose b) {
        BonePose result = new BonePose();
        if (a.rotation != null && b.rotation != null) {
            float[] valuesA = cameraPose(a).rotation;
            float[] valuesB = cameraPose(b).rotation;
            result.rotation = MathUtil.blendQuaternion(valuesA, valuesB);
        } else if (a.rotation != null) {
            result.rotation = cameraPose(a).rotation;
        } else if (b.rotation != null) {
            result.rotation = cameraPose(b).rotation;
        }
        return result;
    }

    private BonePose cameraPose(BonePose a) {
        BonePose result = new BonePose();
        if (a.rotation != null) {
            float[] values = new float[3];
            if (a.rotation.length == 4) {
                values = MathUtil.toEulerAngles(a.rotation);
            }
            float xRot = values[0];
            float yRot = values[1];
            float zRot = -values[2];
            result.rotation = MathUtil.toQuaternion(xRot, yRot, zRot);
        }
        return result;
    }

    //约束组的处理
    private BonePose blendConstraintPose(BonePose a, BonePose b) {
        BonePose result = new BonePose();
        // 位置取最大值
        if (a.position != null && b.position != null) {
            result.position = MathUtil.max(a.position, b.position);
        } else if (a.position != null) {
            result.position = a.position;
        } else if (b.position != null) {
            result.position = b.position;
        }
        // 旋转取最大值
        if (a.rotation != null && b.rotation != null) {
            float[] aa = MathUtil.toEulerAngles(a.rotation);
            float[] bb = MathUtil.toEulerAngles(b.rotation);
            float[] cc = MathUtil.max(toDegreePositive(aa), toDegreePositive(bb));
            for (int i = 0; i < aa.length; i++) {
                cc[i] = (float) Math.toRadians(cc[i]);
            }
            result.rotation = MathUtil.toQuaternion(cc);
        } else if (a.rotation != null) {
            result.rotation = constraintPose(a.copy()).rotation;
        } else if (b.rotation != null) {
            result.rotation = constraintPose(b.copy()).rotation;
        }
        return result;
    }

    private BonePose constraintPose(BonePose a) {
        BonePose result = new BonePose();
        if (a.position != null) {
            result.position = a.position;
        }
        if (a.rotation != null) {
            float[] aa = new float[3];
            if (a.rotation.length == 4) {
                aa = MathUtil.toEulerAngles(a.rotation);
            }
            aa = toDegreePositive(aa);
            for (int i = 0; i < aa.length; i++) {
                aa[i] = (float) Math.toRadians(aa[i]);
            }
            result.rotation = MathUtil.toQuaternion(aa);
        }
        return result;
    }

    private float[] toDegreePositive(float[] a) {
        float[] result = new float[3];
        for (int i = 0; i < a.length; i++) {
            result[i] = (float) (Math.round(MathUtil.toDegreePositive(a[i]) * 10000.0) / 10000.0);
        }
        return result;
    }
}