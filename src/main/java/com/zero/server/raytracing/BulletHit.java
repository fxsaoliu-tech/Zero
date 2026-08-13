package com.zero.server.raytracing;

import net.minecraft.entity.Entity;

public abstract class BulletHit implements Comparable<BulletHit>{

    public double hitLambda;

    public BulletHit(double hitLambda) {
        this.hitLambda = hitLambda;
    }

    public abstract Entity GetEntity();

    @Deprecated
    public int compareTo(BulletHit o) {
        return Double.compare(this.hitLambda,o.hitLambda);
    }
}
