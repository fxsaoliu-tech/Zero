package com.zero.server.raytracing;

import net.minecraft.entity.Entity;

public class EntityHit extends BulletHit{
    private Entity entity;

    public EntityHit(Entity entity,double hitLambda) {
        super(hitLambda);
        this.entity = entity;
    }

    @Override
    public Entity GetEntity() {
        return entity;
    }
}
