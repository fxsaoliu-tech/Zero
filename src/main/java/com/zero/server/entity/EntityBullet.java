package com.zero.server.entity;

import com.zero.Zero;
import com.zero.client.ClientProxy;
import com.zero.network.PacketBase;
import com.zero.server.damage_source.EntityDamageSourceZero;
import com.zero.server.raytracing.BulletHit;
import com.zero.server.raytracing.ZeroRaytracer;
import com.zero.server.type.BulletType;
import com.zero.server.type.InfoType;
import com.zero.server.util.ShotHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;

public class EntityBullet extends Entity implements IEntityAdditionalSpawnData {
    private final static int BULLET_LIFE = 600; //子弹生命 30秒自杀
    private EntityPlayer owner;//发射者
    public int tickAir;//子弹存活时间
    private BulletType bulletType;//子弹属性
    private InfoType firedFrom;//用来判断是什么武器发起的 (死亡消息的调用)
    private float damage;//子弹伤害
    private int pingOfShooter = 0;//(如果是玩家获取玩家是在多少延迟射击的)
    private float penetratingPower;//子弹的穿透力

    public EntityBullet(World worldIn) {
        super(worldIn);
        tickAir = 0;
        setSize(0.5f, 0.5f);
    }

    public EntityBullet(World world, EntityPlayer shooter, float gunDamage, float speed, float spread, BulletType bulletType, InfoType shotFrom) {
        this(world);
        owner = shooter;
        pingOfShooter = ((EntityPlayerMP) shooter).ping;
        this.bulletType = bulletType;
        firedFrom = shotFrom;
        damage = gunDamage;
        penetratingPower = bulletType.shootPenetrate;
        this.setLocationAndAngles(shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1f, shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
        this.setPosition(shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.1f, shooter.posZ);
        this.setBulletArrow(shooter.rotationYaw, shooter.rotationPitch, speed, spread);
    }


    public void setBulletArrow(float yaw, float pitch, double velocity, double Spread) {
        double x = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        double y = -MathHelper.sin(pitch * 0.017453292F);
        double z = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        double sqrt = MathHelper.sqrt(x * x + y * y + z * z);
        x = x / sqrt;
        y = y / sqrt;
        z = z / sqrt;
        //精准度散布
        x += this.rand.nextGaussian() * 0.007499999832361937D * Spread;
        y += this.rand.nextGaussian() * 0.007499999832361937D * Spread;
        z += this.rand.nextGaussian() * 0.007499999832361937D * Spread;
        //重新归一化，避免速度影响方向
        sqrt = MathHelper.sqrt(x * x + y * y + z * z);
        x = x / sqrt;
        y = y / sqrt;
        z = z / sqrt;
        float xzSqrt = MathHelper.sqrt(x * x + z * z);
        this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(y, xzSqrt) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        x = x * velocity;
        y = y * velocity;
        z = z * velocity;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
    }


    //在注册的时候启动同步 造成了z的错误速度 已关闭同步
    @Override
    public void setVelocity(double x, double y, double z) {
        motionX = x;
        motionY = y;
        motionZ = z;
        if (prevRotationPitch == 0.0F && prevRotationYaw == 0.0F) {
            float xzSqrt = MathHelper.sqrt(x * x + z * z);
            prevRotationYaw = rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
            prevRotationPitch = rotationPitch = (float) (MathHelper.atan2(y, xzSqrt) * (180D / Math.PI));
            setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (tickAir > BULLET_LIFE) {
            setDead();
            return;
        }
        if (world.isRemote) {
            this.clientOnUpdate();
        }

        tickAir++;

        float drag = 0.99F;//阻力
        float gravity = 0.02F;//重力
        if (isInWater()) {
            if (world.isRemote) {
                for (int i = 0; i < 4; i++) {
                    float bubbleMotion = 0.25F;
                    world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * bubbleMotion, posY - motionY * bubbleMotion, posZ - motionZ * bubbleMotion, motionX, motionY, motionZ);
                }
            }
            drag = 0.8F;
        }
        motionX *= drag;
        motionY *= drag;
        motionZ *= drag;
        motionY -= gravity * this.getBulletType().fallSpeed;

        float motionXZ = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float) ((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
        rotationPitch = (float) ((Math.atan2(motionY, motionXZ) * 180D) / 3.1415927410125732D);
        for (; rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) {
        }
        for (; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) {
        }
        for (; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) {
        }
        for (; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) {
        }
        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;

        Vec3d origin = new Vec3d(posX, posY, posZ);
        Vec3d motion = new Vec3d(motionX, motionY, motionZ);

        if (!world.isRemote) {
            Entity entity = owner;
            int ping = 0;
            if (entity instanceof EntityPlayerMP) {
                EntityPlayerMP playerMP = (EntityPlayerMP) entity;
                ping = playerMP.ping;
            }
            List<BulletHit> hits = ZeroRaytracer.Raytrace(world, entity, tickAir > 20, this, origin, motion, ping, 0);
            if (!hits.isEmpty()) {
                for (BulletHit bulletHit : hits) {
                    Vec3d hitPos = new Vec3d(origin.x + motion.x * bulletHit.hitLambda, origin.y + motion.y * bulletHit.hitLambda, origin.z + motion.z * bulletHit.hitLambda);

                    penetratingPower = ShotHandler.onHit(world, hitPos, motion, this, bulletHit, penetratingPower);
                    if (penetratingPower <= 0f) {
                        ShotHandler.onDetonate(world, this, hitPos);
                        setDead();
                        break;
                    }
                }
            }
        }
        this.setPosition(posX + motionX, posY + motionY, posZ + motionZ);
    }

    @SideOnly(Side.CLIENT)
    private void clientOnUpdate() {
        if (bulletType.trailParticles && tickAir > 1) {
            this.spawnParticles();
        }
    }

    @SideOnly(Side.CLIENT)
    private void spawnParticles() {
        double dX = (posX - prevPosX) / 10;
        double dY = (posY - prevPosY) / 10;
        double dZ = (posZ - prevPosZ) / 10;

        float spread = 0.1F;
        for (int i = 0; i < 10; i++) {
            ClientProxy.getParticle(bulletType.trailParticleType, world,
                    prevPosX + dX * i + rand.nextGaussian() * spread,
                    prevPosY + dY * i + rand.nextGaussian() * spread,
                    prevPosZ + dZ * i + rand.nextGaussian() * spread);
        }
    }

    public DamageSource getDamageSource(boolean headshot) {
        return new EntityDamageSourceZero(firedFrom.id, owner, owner, firedFrom, headshot).setProjectile();
    }


    public EntityPlayerMP getPlayer() {
        if (owner instanceof EntityPlayerMP) {
            return (EntityPlayerMP) owner;
        }
        return null;
    }

    public BulletType getBulletType() {
        return bulletType;
    }

    public float getDamage() {
        return damage;
    }

    @Override
    public boolean isBurning() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void entityInit() {

    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        bulletType = BulletType.getBulletType(tag.getString("bulletType"));
        firedFrom = InfoType.getInfoType(tag.getString("infoType"));
        damage = tag.getFloat("gunDamage");
        penetratingPower = tag.getFloat("penetratingPower");
        UUID uuid = tag.getUniqueId("owner");
        owner = world.getPlayerEntityByUUID(uuid);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        tag.setString("bulletType", bulletType.id);
        tag.setFloat("gunDamage", damage);
        if (owner != null) {
            tag.setUniqueId("owner", owner.getUniqueID());
        } else {
            setDead();
        }
        tag.setString("infoType", firedFrom.id);
        tag.setFloat("penetratingPower", penetratingPower);
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        data.writeDouble(motionX);
        data.writeDouble(motionY);
        data.writeDouble(motionZ);
        PacketBase.writeUTF(data, bulletType.id);
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        try {
            motionX = data.readDouble();
            motionY = data.readDouble();
            motionZ = data.readDouble();
            bulletType = BulletType.getBulletType(PacketBase.readUTF(data));
        } catch (Exception e) {
            Zero.logger.error("Failed to read bullet owner from server.");
            super.setDead();
            Zero.logger.throwing(e);
        }
    }
}
