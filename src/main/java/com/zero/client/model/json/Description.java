package com.zero.client.model.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 对应 geometry 中的 description 部分
 */
public class Description {

    public String identifier;
    public float texture_width;
    public float texture_height;
    public float visible_bounds_width;
    public float visible_bounds_height;
    public List<Float> visible_bounds_offset;

    public Description() {
    }

    /**
     * 从 JSON 对象读取数据
     *
     * @param json 已经定位到 description 的 JsonObject
     */
    public void read(JsonObject json) {
        if (json == null) return;

        this.identifier = json.has("identifier") ? json.get("identifier").getAsString() : "unknown";
        this.texture_width = json.has("texture_width") ? json.get("texture_width").getAsFloat() : 64f;
        this.texture_height = json.has("texture_height") ? json.get("texture_height").getAsFloat() : 64f;
        this.visible_bounds_width = (json.has("visible_bounds_width") ? json.get("visible_bounds_width").getAsFloat() : 0f) / 2;
        this.visible_bounds_height = (json.has("visible_bounds_height") ? json.get("visible_bounds_height").getAsFloat() : 0f) / 2;

        this.visible_bounds_offset = new ArrayList<>();
        if (json.has("visible_bounds_offset")) {
            JsonArray arr = json.getAsJsonArray("visible_bounds_offset");
            for (JsonElement e : arr) {
                this.visible_bounds_offset.add(e.getAsFloat());
            }
        }
    }

    @Override
    public String toString() {
        return "Description{" +
                "identifier='" + identifier + '\'' +
                ", texture_width=" + texture_width +
                ", texture_height=" + texture_height +
                ", visible_bounds_width=" + visible_bounds_width +
                ", visible_bounds_height=" + visible_bounds_height +
                ", visible_bounds_offset=" + visible_bounds_offset +
                '}';
    }
}
