package com.zero.server.raytracing;

import com.zero.Zero;
import com.zero.client.gui.config.ZeroConfig;
import com.zero.server.damage_source.EntityDamageSourceZero;
import com.zero.server.entity.EntityBullet;
import com.zero.server.type.BulletType;
import com.zero.server.type.InfoType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class PlayerHitBox {
    //玩家
    public EntityPlayer player;
    //碰撞盒
    public AxisAlignedBB boundingBox;
    //命中部位
    public EnumHitboxType type;

    public PlayerHitBox(EntityPlayer player, AxisAlignedBB alignedBB, EnumHitboxType type) {
        this.player = player;
        this.boundingBox = alignedBB;
        this.type = type;
    }

    public PlayerBulletHit rayTrace(Vec3d start, Vec3d motion, Vector3f position) {
        Vec3d end = start.add(motion);

        AxisAlignedBB worldBox = boundingBox.offset(position.x, position.y, position.z);

        RayTraceResult result = worldBox.calculateIntercept(start, end);

        if (result != null) {
            Vec3d hitPoint = result.hitVec.subtract(start);

            double hitLambda = 1.0D;

            if (motion.x != 0D) {
                hitLambda = hitPoint.x / motion.x;
            } else if (motion.y != 0D) {
                hitLambda = hitPoint.y / motion.y;
            } else if (motion.z != 0D) {
                hitLambda = hitPoint.z / motion.z;
            }

            if (hitLambda < 0D) {
                hitLambda = -hitLambda;
            }

            return new PlayerBulletHit(this, hitLambda);
        }
        return null;
    }

    public float hitByBullet(EntityBullet bullet, float damage, float penetratingPower) {
        BulletType type = bullet.getBulletType();
        if (type.canHitBurn) {
            player.setFire(20);
        }
        for (PotionEffect effect : type.hitEffects) {
            player.addPotionEffect(new PotionEffect(effect));
        }
        float damageModifier = type.shootPenetrate < 0.1F ? penetratingPower / type.shootPenetrate : 1;
        switch (this.type) {
            case HEAD:
                damageModifier *= 1.6F;
                break;
            case LEFT_ARM:
            case RIGHT_ARM:
                damageModifier *= 0.6F;
                break;
            case BODY:
            default:
                break;
        }
        switch (this.type) {
            case BODY:
            case HEAD:
                float hitDamage = damage * damageModifier;
                DamageSource damagesource = bullet.getDamageSource(this.type.equals(EnumHitboxType.HEAD));
                if (player.attackEntityFrom(damagesource, hitDamage)) {
                    player.arrowHitTimer++;
                    player.hurtResistantTime = player.maxHurtResistantTime / 2;
                }
                if (type.shootPenetrate > 0) {
                    return penetratingPower - 1;
                }
        }
        return 0;
    }

    @SideOnly(Side.CLIENT)
    public void renderHitbox(World world, Vector3f pos) {
        if (!ZeroConfig.debug) {
            return;
        }
        AxisAlignedBB box = boundingBox.offset(pos.x, pos.y, pos.z);

        Vector3f[] points = new Vector3f[8];

        points[0] = new Vector3f((float) box.minX, (float) box.minY, (float) box.minZ);
        points[1] = new Vector3f((float) box.maxX, (float) box.minY, (float) box.minZ);
        points[2] = new Vector3f((float) box.minX, (float) box.maxY, (float) box.minZ);
        points[3] = new Vector3f((float) box.maxX, (float) box.maxY, (float) box.minZ);

        points[4] = new Vector3f((float) box.minX, (float) box.minY, (float) box.maxZ);
        points[5] = new Vector3f((float) box.maxX, (float) box.minY, (float) box.maxZ);
        points[6] = new Vector3f((float) box.minX, (float) box.maxY, (float) box.maxZ);
        points[7] = new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ);

        drawLine(points[0], points[1]);
        drawLine(points[0], points[2]);
        drawLine(points[0], points[4]);

        drawLine(points[7], points[6]);
        drawLine(points[7], points[5]);
        drawLine(points[7], points[3]);

        drawLine(points[1], points[3]);
        drawLine(points[1], points[5]);

        drawLine(points[2], points[3]);
        drawLine(points[2], points[6]);

        drawLine(points[4], points[5]);
        drawLine(points[4], points[6]);
    }

    private void drawLine(Vector3f a, Vector3f b) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        buffer.pos(a.x, a.y, a.z).endVertex();
        buffer.pos(b.x, b.y, b.z).endVertex();
        tessellator.draw();
    }

}
