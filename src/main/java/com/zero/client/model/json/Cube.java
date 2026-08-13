package com.zero.client.model.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zero.client.model.util.FaceItem;
import com.zero.client.model.util.FaceUVsItem;

import java.util.ArrayList;
import java.util.List;

public class Cube {
    public List<Float> origin;   // [x, y, z]
    public List<Float> size;     // [width, height, depth]
    public List<Float> pivot;    // 可选
    public List<Float> rotation; // 可选
    public FaceUVsItem uv;       // 封装 6 面 UV
    public boolean isBox = false; // 是否是箱型 UV
    public boolean mirror = false;//镜像
    public float inflate = 0f;    // 膨胀值

    public Cube() {
        origin = new ArrayList<>();
        pivot = new ArrayList<>();
        rotation = new ArrayList<>();
        size = new ArrayList<>();
        uv = new FaceUVsItem();
    }

    public void read(JsonObject json) {
        // 原点
        origin.clear();
        if (json.has("origin")) {
            JsonArray arr = json.getAsJsonArray("origin");
            for (JsonElement e : arr) origin.add(e.getAsFloat());
        }
        // 宽高
        size.clear();
        if (json.has("size")) {
            JsonArray arr = json.getAsJsonArray("size");
            for (JsonElement e : arr) size.add(e.getAsFloat());
        }

        // 平移点
        pivot.clear();
        if (json.has("pivot")) {
            JsonArray arr = json.getAsJsonArray("pivot");
            for (JsonElement e : arr) pivot.add(e.getAsFloat());
        }
        // 旋转点
        rotation.clear();
        if (json.has("rotation")) {
            JsonArray arr = json.getAsJsonArray("rotation");
            for (JsonElement e : arr) rotation.add(e.getAsFloat());
        }
        // 膨胀
        if (json.has("inflate")) {
            inflate = json.get("inflate").getAsFloat();
        }

        // 镜像
        if (json.has("mirror")) {
            mirror = json.get("mirror").getAsBoolean();
        }

        // uv贴图
        if (json.has("uv")) {
            uv = new FaceUVsItem();
            JsonElement uvElement = json.get("uv");
            if (uvElement.isJsonArray()) {
                isBox = true;
                JsonArray arr = uvElement.getAsJsonArray();
                float[] uvArray = new float[]{0f, 0f};
                float[] uvSize = new float[]{1f, 1f};
                if (arr.size() >= 2) {
                    uvArray[0] = arr.get(0).getAsFloat();
                    uvArray[1] = arr.get(1).getAsFloat();
                }
                if (json.has("uv_size")) {
                    JsonArray sz = json.getAsJsonArray("uv_size");
                    if (sz.size() >= 2) {
                        uvSize[0] = sz.get(0).getAsFloat();
                        uvSize[1] = sz.get(1).getAsFloat();
                    }
                }
                uv.north = uv.south = uv.east = uv.west = uv.up = uv.down = new FaceItem(uvArray, uvSize);
            } else if (uvElement.isJsonObject()) {
                isBox = false;
                JsonObject uvObj = uvElement.getAsJsonObject();
                if (uvObj.has("north")) {
                    uv.north = parseFace(uvObj.getAsJsonObject("north"));
                }
                if (uvObj.has("south")) {
                    uv.south = parseFace(uvObj.getAsJsonObject("south"));
                }
                if (uvObj.has("east")) {
                    uv.east = parseFace(uvObj.getAsJsonObject("east"));
                }
                if (uvObj.has("west")) {
                    uv.west = parseFace(uvObj.getAsJsonObject("west"));
                }
                if (uvObj.has("up")) {
                    uv.up = parseFace(uvObj.getAsJsonObject("up"));
                }
                if (uvObj.has("down")) {
                    uv.down = parseFace(uvObj.getAsJsonObject("down"));
                }
            }
        }
    }

    private FaceItem parseFace(JsonObject json) {
        float[] uv = new float[]{0, 0};
        float[] uvSize = new float[]{0, 0};
        if (json.has("uv")) {
            JsonArray uvArr = json.getAsJsonArray("uv");
            uv[0] = uvArr.get(0).getAsFloat();
            uv[1] = uvArr.get(1).getAsFloat();
        }
        if (json.has("uv_size")) {
            JsonArray szArr = json.getAsJsonArray("uv_size");
            uvSize[0] = szArr.get(0).getAsFloat();
            uvSize[1] = szArr.get(1).getAsFloat();
        }
        return new FaceItem(uv, uvSize);
    }

    @Override
    public String toString() {
        return "Cube{" +
                "origin=" + origin +
                ", size=" + size +
                ", pivot=" + pivot +
                ", rotation=" + rotation +
                ", isBox=" + isBox +
                ", inflate=" + inflate +
                ", uv=" + uv +
                '}';
    }
}
