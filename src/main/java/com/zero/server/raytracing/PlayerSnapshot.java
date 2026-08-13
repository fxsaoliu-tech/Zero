package com.zero.server.raytracing;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerSnapshot {

    public EntityPlayer player;

    public Vector3f position;

    public List<PlayerHitBox> hitboxes;

    public long time;

    public PlayerSnapshot(EntityPlayer p) {
        this.player = p;
        this.position = new Vector3f((float) player.posX, (float) player.posY, (float) player.posZ);
        this.hitboxes = new ArrayList<>();

        if (player.isPlayerSleeping()) {
            hitboxes.add(new PlayerHitBox(player, new AxisAlignedBB(-0.3, 0, -0.3, 0.3, 0.2, 0.3), EnumHitboxType.BODY));
        } else if (player.isElytraFlying()) {
            hitboxes.add(new PlayerHitBox(player, new AxisAlignedBB(-0.3, 0, -0.3, 0.3, 0.6, 0.3), EnumHitboxType.BODY));
        } else if (player.isRiding()) {
            hitboxes.add(new PlayerHitBox(player, new AxisAlignedBB(-0.3, 1.4, -0.3, 0.3, 1.9, 0.3), EnumHitboxType.HEAD));
            hitboxes.add(new PlayerHitBox(player, new AxisAlignedBB(-0.3, 0.5, -0.3, 0.3, 1.4, 0.3), EnumHitboxType.BODY));
        } else if (player.isSneaking()) {
            hitboxes.add(new PlayerHitBox(player, new AxisAlignedBB(-0.3, 1.1, -0.3, 0.3, 1.5, 0.3), EnumHitboxType.HEAD));
            hitboxes.add(new PlayerHitBox(player, new AxisAlignedBB(-0.3, 0, -0.3, 0.3, 1.1, 0.3), EnumHitboxType.BODY));
        } else {
            hitboxes.add(new PlayerHitBox(player, new AxisAlignedBB(-0.3, 1.4, -0.3, 0.3, 1.9, 0.3), EnumHitboxType.HEAD));
            hitboxes.add(new PlayerHitBox(player, new AxisAlignedBB(-0.3, 0, -0.3, 0.3, 1.4, 0.3), EnumHitboxType.BODY));
        }
    }

    public List<BulletHit> raytrace(Vec3d origin, Vec3d motion) {
        List<BulletHit> hits = new ArrayList<>();
        for (PlayerHitBox hitbox : hitboxes) {
            PlayerBulletHit hit = hitbox.rayTrace(origin, motion, position);
            if (hit != null && hit.hitLambda >= 0F && hit.hitLambda <= 1F) {
                hits.add(hit);
            }
        }
        Collections.sort(hits);
        return hits;
    }

    @SideOnly(Side.CLIENT)
    public void renderSnapshot() {
        for (PlayerHitBox hitbox : hitboxes) {
            hitbox.renderHitbox(player.world, position);
        }
    }
}
