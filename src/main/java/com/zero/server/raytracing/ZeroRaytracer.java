package com.zero.server.raytracing;

import com.zero.server.data.PlayerData;
import com.zero.server.data.PlayerHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZeroRaytracer {

    public static List<BulletHit> Raytrace(World world, Entity playerToIgnore, boolean canHitSelf, Entity entityToIgnore, Vec3d origin, Vec3d motion, int pingOfShooter, float gunPenetration) {
        List<BulletHit> hits = new ArrayList<>();

        for (int i = 0; i < world.loadedEntityList.size(); i++) {
            Entity obj = world.loadedEntityList.get(i);
            boolean shouldDoNormalHitDetect = true;

            if (obj instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) obj;
                PlayerData data = PlayerHandler.getPlayerData(player.getUniqueID());
                shouldDoNormalHitDetect = false;
                if (data != null) {
                    if (player.isDead) {
                        continue;
                    }
                    if (player == playerToIgnore && !canHitSelf) {
                        continue;
                    }
                    int snapshotToTry = pingOfShooter / 50;
                    if (snapshotToTry >= data.snapshots.length) {
                        snapshotToTry = data.snapshots.length - 1;
                    }
                    PlayerSnapshot snapshot = data.snapshots[snapshotToTry];
                    if (snapshot == null) {
                        snapshot = data.snapshots[0];
                    }
                    if (snapshot == null) {
                        shouldDoNormalHitDetect = true;
                    } else {
                        List<BulletHit> playerHits = snapshot.raytrace(origin, motion);
                        hits.addAll(playerHits);
                    }
                }
            }
            if (shouldDoNormalHitDetect) {
                Entity entity = obj;
                if (entity != entityToIgnore && entity != playerToIgnore && !entity.isDead && (entity instanceof EntityLivingBase)) {
                    RayTraceResult mop = entity.getEntityBoundingBox().calculateIntercept(origin, new Vec3d(origin.x + motion.x, origin.y + motion.y, origin.z + motion.z));
                    if (mop != null) {
                        Entity[] parts = entity.getParts();
                        boolean hit = true;
                        // If parts exist, the intercepted part is calculated and used instead of the whole entity.
                        // If no part is intercepted, the entity itself is not hit
                        if (parts != null) {
                            hit = false;
                            for (Entity part : parts) {
                                RayTraceResult result = part.getEntityBoundingBox().calculateIntercept(origin, new Vec3d(origin.x + motion.x, origin.y + motion.y, origin.z + motion.z));
                                if (result != null) {
                                    mop = result;
                                    entity = part;
                                    hit = true;
                                    break;
                                }
                            }
                        }
                        if (hit) {
                            Vec3d hitPoint = new Vec3d(mop.hitVec.x - origin.x, mop.hitVec.y - origin.y, mop.hitVec.z - origin.z);
                            float hitLambda = 1F;
                            if (motion.x != 0F) {
                                hitLambda = (float) (hitPoint.x / motion.x);
                            } else if (motion.y != 0F) {
                                hitLambda = (float) (hitPoint.y / motion.y);
                            } else if (motion.z != 0F) {
                                hitLambda = (float) (hitPoint.z / motion.z);
                            }
                            if (hitLambda < 0) {
                                hitLambda = -hitLambda;
                            }
                            hits.add(new EntityHit(entity, hitLambda));
                        }
                    }
                }
            }
        }
        Vec3d mot = motion;
        mot = mot.normalize();
        mot = mot.scale(0.5d);
        hits = raytraceBlock(world, origin, new Vec3d(0, 0, 0), motion, mot, hits, gunPenetration, null);

        if (!hits.isEmpty()) {
            Collections.sort(hits);
        }

        return hits;
    }

    private static List<BulletHit> raytraceBlock(World world, Vec3d posVec, Vec3d previousHit, Vec3d motion, Vec3d normalized_motion, List<BulletHit> hits, float penetration, BlockPos oldPos) {
        Vec3d nextPosVec = new Vec3d(posVec.x + motion.x, posVec.y + motion.y, posVec.z + motion.z);

        RayTraceResult hit = world.rayTraceBlocks(posVec, nextPosVec, false, true, true);

        if (hit != null) {
            Vec3d hitVec = hit.hitVec.subtract(posVec);
            hitVec = hitVec.add(previousHit);

            BlockPos pos = hit.getBlockPos();
            IBlockState blockState = world.getBlockState(hit.getBlockPos());

            if (!pos.equals(oldPos)) {
                float lambda = 1;
                if (motion.x != 0) {
                    lambda = (float) (hitVec.x / motion.x);
                } else if (motion.y != 0) {
                    lambda = (float) (hitVec.y / motion.y);
                } else if (motion.z != 0) {
                    lambda = (float) (hitVec.z / motion.z);
                }

                if (lambda < 0) {
                    lambda = -lambda;
                }

                hits.add(new BlockHit(hit, lambda, blockState));
                penetration -= ZeroRaytracer.getBlockPenetrationDecrease(blockState, pos, world);
            }
            if (penetration > 0) {
                hits = raytraceBlock(world, hit.hitVec.add(normalized_motion), hitVec.add(normalized_motion), motion, normalized_motion, hits, penetration, pos);
            }
        }
        return hits;
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
