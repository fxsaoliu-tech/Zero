package com.zero.server.damage_source;

import com.zero.server.data.PlayerHandler;
import com.zero.server.type.InfoType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;

public class EntityDamageSourceZero extends EntityDamageSource {
    private InfoType weapon;
    private EntityPlayer shooter;
    private boolean headshot;

    public EntityDamageSourceZero(String damageTypeIn, Entity entity, EntityPlayer player, InfoType infoType) {
        this(damageTypeIn, entity, player, infoType, false);
    }

    public EntityDamageSourceZero(String damageTypeIn, Entity entity, EntityPlayer player, InfoType wep, boolean headshot) {
        super(damageTypeIn, player);
        this.weapon = wep;
        this.shooter = player;
        this.headshot = headshot;
    }

    @Override
    public ITextComponent getDeathMessage(EntityLivingBase living) {
        shooter.sendMessage(new TextComponentString("击杀: " + living.getName()));
        if (!(living instanceof EntityPlayer) || shooter == null || PlayerHandler.getPlayerData(shooter.getUniqueID()) == null) {
            if (shooter == null) {
                return new TextComponentString(living.getName() + " was shot");
            } else {
                return new TextComponentString(living.getName() + " was shot by " + shooter.getName());
            }
        }
        return new TextComponentString("#zero");
    }

    /**
     * @return The weapon (InfoType) used to cause this damage
     */
    public InfoType getWeapon() {
        return weapon;
    }

    /**
     * @return The Player responsible for this damage
     */
    public EntityPlayer getCausedPlayer() {
        return shooter;
    }

    /**
     * @return True if this is a headshot, false if not
     */
    public boolean isHeadshot() {
        return headshot;
    }

    @Override
    @Nullable
    public Vec3d getDamageLocation() {
        if (damageSourceEntity == null) {
            return new Vec3d(0d, 0d, 0d);
        }
        return super.getDamageLocation();
    }
}
