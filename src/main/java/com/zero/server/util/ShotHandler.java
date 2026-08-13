package com.zero.server.util;

import com.zero.Zero;
import com.zero.network.hit.PacketHitMarker;
import com.zero.server.entity.EntityBullet;
import com.zero.server.explosion.ZeroExplosion;
import com.zero.server.raytracing.BlockHit;
import com.zero.server.raytracing.BulletHit;
import com.zero.server.raytracing.EntityHit;
import com.zero.server.raytracing.PlayerBulletHit;
import com.zero.server.type.BulletType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ShotHandler {

    public static float onHit(World world, Vec3d hitPos, Vec3d shootingDirection, EntityBullet entityBullet, BulletHit bulletHit, float penetratingPower) {
        BulletType bulletType = entityBullet.getBulletType();
        if (bulletHit instanceof PlayerBulletHit) {
            PlayerBulletHit playerHit = (PlayerBulletHit) bulletHit;
            penetratingPower = playerHit.getPlayerHitBox().hitByBullet(entityBullet, entityBullet.getDamage(), penetratingPower);

            EntityPlayerMP player = entityBullet.getPlayer();
            if (player != null) {
                //设置命中反馈
                Zero.getPacketHandler().sendToPlayer(new PacketHitMarker(), player);
            }
        } else if (bulletHit instanceof EntityHit) {
            EntityHit entityHit = (EntityHit) bulletHit;
            if (entityHit.GetEntity() != null) {
                if (entityHit.GetEntity().attackEntityFrom(entityBullet.getDamageSource(false), entityBullet.getDamage()) && entityHit.GetEntity() instanceof EntityLivingBase) {
                    EntityLivingBase living = (EntityLivingBase) entityHit.GetEntity();
                    for (PotionEffect effect : bulletType.hitEffects) {
                        living.addPotionEffect(new PotionEffect(effect));
                    }
                    living.hurtResistantTime = 0;
                }
                if (bulletType.canHitBurn) {
                    entityHit.GetEntity().setFire(20);
                }
                penetratingPower -= 1F;
            }
            EntityPlayerMP player = entityBullet.getPlayer();
            if (player != null) {
                //设置命中反馈
                Zero.getPacketHandler().sendToPlayer(new PacketHitMarker(), player);
            }
        } else if (bulletHit instanceof BlockHit) {
            BlockHit blockHit = (BlockHit) bulletHit;
            RayTraceResult raytraceResult = blockHit.getHit();
            BlockPos pos = raytraceResult.getBlockPos();
            Material mat = blockHit.getState().getMaterial();
            if (bulletType.breaksGlass && mat == Material.GLASS) {
                world.destroyBlock(pos, false);
            }
            IBlockState state = blockHit.getState().getActualState(world, pos);
            penetratingPower -= getBlockPenetrationDecrease(state, pos, world);

            EnumFacing faceing = blockHit.getHit().sideHit;

            Vec3d bulletDir = new Vec3d(shootingDirection.x, shootingDirection.y, shootingDirection.z);
            bulletDir.normalize();
            bulletDir.scale(0.5f);
            for (EntityPlayer player : world.playerEntities) {
                //给附近300格的玩家发送弹孔消息
                if (player.getDistanceSq(pos) < 90000) {

                }
            }
            if (penetratingPower <= 0F || (bulletType.hitExplosion)) {
                return -1f;
            }
        }
        return penetratingPower;
    }

    public static void onDetonate(World world, EntityBullet entityBullet, Vec3d detonatePos) {
        BulletType bulletType = entityBullet.getBulletType();
        if (bulletType.explosionRadius > 0) {
            new ZeroExplosion(world, entityBullet, entityBullet.getPlayer(), bulletType,
                    detonatePos.x, detonatePos.y, detonatePos.z, bulletType.explosionRadius, bulletType.burnRadius > 0, bulletType.canSmoke, bulletType.explosionBreaksBlocks);

        }
        if (bulletType.burnRadius > 0) {
            for (float i = -bulletType.burnRadius; i < bulletType.burnRadius; i++) {
                for (float k = -bulletType.burnRadius; k < bulletType.burnRadius; k++) {
                    for (int j = -1; j < 1; j++) {
                        if (world.getBlockState(new BlockPos((int) (detonatePos.x + i), (int) (detonatePos.y + j), (int) (detonatePos.z + k))).getMaterial() == Material.AIR) {
                            world.setBlockState(new BlockPos((int) (detonatePos.x + i), (int) (detonatePos.y + j), (int) (detonatePos.z + k)), Blocks.FIRE.getDefaultState(), 2);
                        }
                    }
                }
            }
        }

    }

    public static float getBlockPenetrationDecrease(IBlockState blockstate, BlockPos pos, World world) {
        float hardness = blockstate.getBlockHardness(world, pos) * 2;
        if (hardness < 0) {
            return 1000;
        } else {
            return Math.max(hardness, 1);
        }
    }

}
