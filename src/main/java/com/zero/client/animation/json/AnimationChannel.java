package com.zero.client.animation.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zero.client.util.math.MathUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 单个骨骼在一个动画里的通道
 */
public class AnimationChannel {
    public List<Keyframe> position = new ArrayList<>();
    public List<Keyframe> rotation = new ArrayList<>();
    public List<Keyframe> scale = new ArrayList<>();

    public void read(JsonObject json) {
        if (json == null) return;
        readChannel(json, "position", position);
        readChannel(json, "rotation", rotation);
        readChannel(json, "scale", scale);

        postProcess();
    }

    private void readChannel(JsonObject json, String name, List<Keyframe> out) {
        if (json.has(name)) {
            parseJsonKeyframes(json.get(name), out);
        }
    }

    /**
     * 后处理
     */
    private void postProcess() {

        sortKeyframes(position);
        sortKeyframes(rotation);
        sortKeyframes(scale);

        processRotation();
    }

    /**
     * 关键帧排序
     */
    private void sortKeyframes(List<Keyframe> list) {
        list.sort(Comparator.comparingDouble(k -> k.time));
    }

    private void processRotation() {
        for (Keyframe kf : rotation) {
            // 插值类型转换
            if (kf.interpolation == InterpolationType.LINEAR) {
                kf.interpolation = InterpolationType.SPHERICAL_LINEAR;
            } else if (kf.interpolation == InterpolationType.CATMULL_ROM) {
                kf.interpolation = InterpolationType.SPHERICAL_SQUAD;
            }
            // 欧拉角转四元数
            kf.value = eulerToQuaternion(kf.value);
            kf.pre = eulerToQuaternion(kf.pre);
            kf.post = eulerToQuaternion(kf.post);
        }
    }

    private float[] eulerToQuaternion(float[] euler) {
        if (euler == null) return null;
        euler[0] = (float) Math.toRadians(euler[0]);
        euler[1] = (float) Math.toRadians(euler[1]);
        euler[2] = (float) Math.toRadians(euler[2]);
        return MathUtil.toQuaternion(euler);
    }

    /**
     * 解析关键帧
     */
    private void parseJsonKeyframes(JsonElement elem, List<Keyframe> out) {

        if (elem == null) return;

        // 单关键帧
        if (elem.isJsonArray() || elem.isJsonPrimitive()) {
            Keyframe k = new Keyframe(0);
            k.read(elem);
            out.add(k);
            return;
        }

        if (!elem.isJsonObject()) return;

        JsonObject obj = elem.getAsJsonObject();

        for (Map.Entry<String, JsonElement> e : obj.entrySet()) {

            float time = Float.parseFloat(e.getKey());

            Keyframe k = new Keyframe(time);
            k.read(e.getValue());

            out.add(k);
        }
    }
}