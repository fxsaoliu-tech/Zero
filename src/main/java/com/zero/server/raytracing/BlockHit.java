package com.zero.server.raytracing;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;

public class BlockHit extends BulletHit{
    private RayTraceResult hit;
    private IBlockState state;

    public BlockHit(RayTraceResult hit, double hitLambda, IBlockState  blockstate) {
        super(hitLambda);
        this.hit = hit;
        this.state = blockstate;
    }

    @Override
    public Entity GetEntity() {
        return null;
    }

    public IBlockState getState() {
        return state;
    }

    public RayTraceResult getHit() {
        return hit;
    }
}
