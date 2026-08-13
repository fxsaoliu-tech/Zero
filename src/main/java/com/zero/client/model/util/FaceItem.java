package com.zero.client.model.util;

public class FaceItem {
    public static final FaceItem EMPTY = new FaceItem(new float[]{0, 0}, new float[]{0, 0});

    private float[] uv;
    private float[] uvSize;

    public FaceItem(float[] uv, float[] uvSize) {
        this.uv = uv;
        this.uvSize = uvSize;
    }

    public static FaceItem single16X() {
        return new FaceItem(new float[]{0, 0}, new float[]{16, 16});
    }

    public float[] getUv() {
        return uv;
    }

    public float[] getUvSize() {
        return uvSize;
    }
}