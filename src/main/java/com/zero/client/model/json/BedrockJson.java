package com.zero.client.model.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 对应整个 Bedrock geometry JSON 文件
 */
public class BedrockJson {

    public String format_version;
    public Description description;      // 文件只有一个 description
    public List<Bone> bones = new ArrayList<>(); // 对应 description 的 bones 列表

    public BedrockJson() {}

    /** 从 JSON 顶层解析 */
    public void read(JsonObject json) {
        if (json.has("format_version")) {
            this.format_version = json.get("format_version").getAsString();
        }
        if (json.has("minecraft:geometry")) {
            JsonArray geoArr = json.getAsJsonArray("minecraft:geometry");
            if (geoArr.size() == 0) return;

            JsonObject geoObj = geoArr.get(0).getAsJsonObject(); // 只取第一个

            // 描述
            description = new Description();
            if (geoObj.has("description")) {
                description.read(geoObj.getAsJsonObject("description"));
            }

            // 骨骼
            bones.clear();
            if (geoObj.has("bones")) {
                JsonArray bonesArr = geoObj.getAsJsonArray("bones");
                for (JsonElement be : bonesArr) {
                    Bone b = new Bone();
                    b.read(be.getAsJsonObject());
                    bones.add(b);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "BedrockJson{format_version='" + format_version + "'\n" +
                "Description: " + description + "\n" +
                "Bones: " + bones + "\n" +
                "}";
    }
}
