package com.zero.server.event;

import com.zero.Zero;
import com.zero.network.hit.PacketKillMessage;
import com.zero.server.damage_source.EntityDamageSourceZero;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityDeathEvent {

    public EntityDeathEvent() {

    }

    @SubscribeEvent
    public void entityDeathEvent(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        DamageSource source = event.getSource();
        if (source instanceof EntityDamageSourceZero) {
            EntityDamageSourceZero damageSource = (EntityDamageSourceZero) source;

            if (damageSource.getCausedPlayer() != null && event.getEntityLiving() instanceof EntityPlayer) {
                EntityPlayer BeKilled = (EntityPlayer) event.getEntity();
                EntityPlayerMP player = (EntityPlayerMP) damageSource.getCausedPlayer();
                Zero.getPacketHandler().sendToPlayer(new PacketKillMessage(damageSource.isHeadshot(),
                        damageSource.getWeapon(), BeKilled.getName(), player.getName()), player);
            }else {
                EntityLivingBase BeKilled =  event.getEntityLiving();
                EntityPlayerMP player = (EntityPlayerMP) damageSource.getCausedPlayer();
                Zero.getPacketHandler().sendToPlayer(new PacketKillMessage(damageSource.isHeadshot(),
                        damageSource.getWeapon(), BeKilled.getName(), player.getName()), player);
            }

        }
    }

}
