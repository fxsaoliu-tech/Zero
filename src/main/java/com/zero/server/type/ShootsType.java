package com.zero.server.type;

import com.zero.server.file.FileList;

public class ShootsType extends InfoType {
    //可掉落物品
    public String useDropItem = "";//使用后掉落物品
    //子弹属性
    public boolean breaksGlass = false;//可以打碎玻璃
    //物理特性
    public float fallSpeed = 0.25f;//重力效果
    public float hitBoxSize = 0.25f;//命中判定的体积大小（半径）
    //爆炸
    public boolean hitExplosion = false;//击中爆炸
    public boolean explosionBreaksBlocks = false;//可以爆破方块
    public int explosionRadius = 0;//爆炸范围
    //粒子
    public boolean trailParticles = false;//是否启用飞行轨迹粒子
    public String trailParticleType;//粒子属性
    //闪光,照明,燃烧,烟雾
    public boolean canGlitter = false;//能否闪光
    public boolean canLighting = false;//能否照明
    public int lightingRadius = 0;//照明范围
    public boolean canHitBurn = false;//击中目标燃烧
    public int burnRadius = 0;//燃烧范围
    public boolean canSmoke = false;//能否生成烟雾
    public int smokeRadius = 0;//烟雾范围

    public ShootsType() {
    }

    @Override
    public ShootsType loadContent(FileList fileList) {
        super.loadContent(fileList);
        return this;
    }

    @Override
    protected void read(String[] content, FileList file) {
        super.read(content, file);
        useDropItem = Read(content, "useDropItem", useDropItem, file);

        breaksGlass = Read(content, "breaksGlass", breaksGlass, file);

        fallSpeed = Read(content, "fallSpeed", fallSpeed, file);
        hitBoxSize = Read(content, "hitBoxSize", hitBoxSize, file);

        hitExplosion = Read(content, "hitExplosion", hitExplosion, file);
        explosionBreaksBlocks = Read(content, "explosionBreaksBlocks", explosionBreaksBlocks, file);
        explosionRadius = Read(content, "explosionRadius", explosionRadius, file);

        trailParticles = Read(content, "trailParticles", trailParticles, file);
        trailParticleType = Read(content, "trailParticleType", trailParticleType, file);

        canGlitter = Read(content, "canGlitter", canGlitter, file);
        canLighting = Read(content, "canLighting", canLighting, file);
        lightingRadius = Read(content, "lightingRadius", lightingRadius, file);
        canHitBurn = Read(content, "canHitBurn", canHitBurn, file);
        burnRadius = Read(content, "burnRadius", burnRadius, file);
        canSmoke = Read(content, "canSmoke", canSmoke, file);
        smokeRadius = Read(content, "smokeRadius", smokeRadius, file);
    }
}
