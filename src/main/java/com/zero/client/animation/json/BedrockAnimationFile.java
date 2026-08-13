package com.zero.client.animation.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class BedrockAnimationFile {
    public String format_version;
    public Map<String, Animation> animationMap = new HashMap<>();

    public BedrockAnimationFile() {

    }

    /**
     * 从 JSON 顶层解析
     */
    public void read(JsonObject root) {
        if (root.has("format_version")) {
            this.format_version = root.get("format_version").getAsString();
        }
        if (root.has("animations")) {
            JsonObject animations = root.getAsJsonObject("animations");
            for (Map.Entry<String, JsonElement> animEntry : animations.entrySet()) {
                String animName = animEntry.getKey();
                JsonObject animObj = animEntry.getValue().getAsJsonObject();
                Animation anim = new Animation(animName);
                anim.read(animObj);
                animationMap.put(animName, anim);
            }
        }
    }
}
