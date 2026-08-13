package com.zero.client.animation.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zero.client.animation.AnimationPlayer;

public class Keyframe {
    /**
     * 时间点（秒）
     */
    public float time;

    /**
     * 关键帧值
     */
    public float[] value;
    public float[] pre;
    public float[] post;

    /**
     * 插值语义
     */
    public InterpolationType interpolation = InterpolationType.LINEAR;


    public Keyframe(float time) {
        this.time = time;
    }

    public float[] sample(float time) {
        if (time >= this.time && post != null) {
            return post;
        }
        // 小于当前帧 → 用 pre
        if (time < this.time && pre != null) {
            return pre;
        }
        return value;
    }

    /**
     * 从 JSON 读取关键帧数据
     */
    public void read(JsonElement json) {
        if (json.isJsonPrimitive()) {
            float v = json.getAsFloat();
            value = new float[]{v, v, v};
        } else if (json.isJsonArray()) {
            value = jsonArrayToFloatArray(json.getAsJsonArray());
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("post") && obj.has("pre")) {
                pre = jsonArrayToFloatArray(obj.getAsJsonArray("pre"));
                post = jsonArrayToFloatArray(obj.getAsJsonArray("post"));
            } else if (obj.has("pre")) {
                value = jsonArrayToFloatArray(obj.getAsJsonArray("pre"));
            } else if (obj.has("post")) {
                value = jsonArrayToFloatArray(obj.getAsJsonArray("post"));
            }
            if (obj.has("lerp_mode")) {
                String mode = obj.get("lerp_mode").getAsString();
                if (mode.equals("catmullrom")) {
                    interpolation = InterpolationType.CATMULL_ROM;
                } else {
                    interpolation = InterpolationType.LINEAR;
                }
            }
        }
    }

    private float[] jsonArrayToFloatArray(JsonArray array) {
        float[] result = new float[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = array.get(i).getAsFloat();
        }
        return result;
    }

}
