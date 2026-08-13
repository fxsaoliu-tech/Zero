package com.zero.server.raytracing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerBulletHit extends BulletHit {
    private PlayerHitBox playerHitBox;

    public PlayerBulletHit(PlayerHitBox player, double hitLambda) {
        super(hitLambda);
        this.playerHitBox = player;
    }


    @Override
    public EntityPlayer GetEntity() {
        return playerHitBox.player;
    }

    public PlayerHitBox getPlayerHitBox() {
        return playerHitBox;
    }
}
