package com.zero.client.animation.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zero.client.animation.AnimationPlayType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Animation {
    private final String name;//动画名称
    private float length = 0F;//动画长度
    private AnimationPlayType type = AnimationPlayType.PLAY_ONCE_STOP;//播放属性
    private final Map<String, AnimationChannel> channels = new HashMap<>();//骨骼动画通道
    private final Map<Float, String> soundEffects = new HashMap<>();//声音事件


    public Animation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public float getLength() {
        return length;
    }

    public AnimationPlayType getType() {
        return type;
    }

    public Map<Float, String> getSoundEffects() {
        return soundEffects;
    }

    public Map<String, AnimationChannel> getChannels() {
        return channels;
    }


    public void read(JsonObject json) {
        //读取动画类型
        if (json.has("loop")) {
            this.type = this.setType(json.get("loop").getAsString());
        }
        //读取动画长度 单位秒
        if (json.has("animation_length")) {
            this.length = (json.get("animation_length").getAsFloat());
        }
        //骨骼 动画关键帧
        if (json.has("bones")) {
            JsonObject bones = json.getAsJsonObject("bones");
            for (Map.Entry<String, JsonElement> boneEntry : bones.entrySet()) {
                String boneName = boneEntry.getKey();
                JsonObject boneObj = boneEntry.getValue().getAsJsonObject();
                AnimationChannel channel = this.getOrCreateChannel(boneName);
                channel.read(boneObj);
            }
        }
        //读取音效
        if (json.has("sound_effects")) {
            JsonObject sounds = json.getAsJsonObject("sound_effects");
            for (Map.Entry<String, JsonElement> entry : sounds.entrySet()) {
                float time = Float.parseFloat(entry.getKey());
                if (entry.getValue().isJsonObject()) {
                    JsonObject obj = entry.getValue().getAsJsonObject();
                    if (obj.has("effect")) {
                        soundEffects.put(time, obj.get("effect").getAsString());
                    }
                }
            }
        }
    }

    private AnimationChannel getOrCreateChannel(String boneName) {
        return this.channels.computeIfAbsent(boneName, (k) -> new AnimationChannel());
    }

    private AnimationPlayType setType(String boneName) {
        switch (boneName) {
            case "true":
                return AnimationPlayType.LOOP;
            case "false":
                return AnimationPlayType.PLAY_ONCE_STOP;
            case "hold_on_last_frame":
                return AnimationPlayType.PLAY_ONCE_HOLD;
        }
        return AnimationPlayType.PLAY_ONCE_STOP;
    }
}
