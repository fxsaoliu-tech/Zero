package com.zero.client.animation.json;

/**
 * 骨骼姿态：位置 / 旋转（四元数） / 缩放
 */
public class BonePose {
    public float[] position;
    public float[] rotation;
    public float[] scale;

    public BonePose() {
    }

    // 深拷贝（如果需要）
    public BonePose copy() {
        BonePose b = new BonePose();
        if (this.position != null) {
            b.position = new float[]{this.position[0], this.position[1], this.position[2]};
        }
        if (this.rotation != null) {
            b.rotation = new float[]{this.rotation[0], this.rotation[1], this.rotation[2], this.rotation[3]};
        }
        if (this.scale != null) {
            b.scale = new float[]{this.scale[0], this.scale[1], this.scale[2]};
        }
        return b;
    }
}
