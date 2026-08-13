package com.zero.client.model.bedrock;

import org.joml.Vector3f;

public class BedrockVertex {
    public final Vector3f pos; // 模型坐标
    public final float u;
    public final float v;

    public BedrockVertex(float x, float y, float z, float u, float v) {
        this.pos = new Vector3f(x, y, z);
        this.u = u;
        this.v = v;
    }

    /** 返回一个带新UV的副本 */
    public BedrockVertex remap(float u, float v) {
        return new BedrockVertex(pos.x, pos.y, pos.z, u, v);
    }
}
