package com.zero.client.model.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 对应 geometry 中 bones 部分
 */
public class Bone {

    public String name;
    public String parent; // 可选
    public List<Float> pivot;      // [x, y, z]
    public List<Float> rotation;   // [x, y, z] 可选
    public List<Cube> cubes;       // 可选

    public Bone() {
        cubes = new ArrayList<>();
    }

    /**
     * 读取 JSON 数据
     * @param json Bone 对应的 JsonObject
     */
    public void read(JsonObject json) {
        if (json == null) return;

        this.name = json.has("name") ? json.get("name").getAsString() : "unknown";
        this.parent = json.has("parent") ? json.get("parent").getAsString() : null;

        // 平移点
        this.pivot = new ArrayList<>();
        if (json.has("pivot")) {
            JsonArray arr = json.getAsJsonArray("pivot");
            for (JsonElement e : arr) {
                this.pivot.add(e.getAsFloat());
            }
        }

        // 旋转
        this.rotation = new ArrayList<>();
        if (json.has("rotation")) {
            JsonArray arr = json.getAsJsonArray("rotation");
            for (JsonElement e : arr) {
                this.rotation.add(e.getAsFloat());
            }
        }

        // 立方体
        if (json.has("cubes")) {
            JsonArray arr = json.getAsJsonArray("cubes");
            for (JsonElement e : arr) {
                Cube cube = new Cube();
                cube.read(e.getAsJsonObject());
                cubes.add(cube);
            }
        }
    }

    @Override
    public String toString() {
        return "Bone{" +
                "name='" + name + '\'' +
                ", parent='" + parent + '\'' +
                ", pivot=" + pivot +
                ", rotation=" + rotation +
                ", cubes=" + cubes +
                '}';
    }
}
