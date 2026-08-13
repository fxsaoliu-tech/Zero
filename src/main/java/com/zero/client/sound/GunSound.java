package com.zero.client.sound;

import com.zero.Zero;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nullable;

public class GunSound extends MovingSound {
    private static final SoundEvent PLACEHOLDER_EVENT = new SoundEvent(new ResourceLocation(Zero.MOD_ID, "sounds"));

    private final Entity entity;
    private final ResourceLocation registryName;

    public GunSound(Entity entity, float soundDistance, ResourceLocation registryName, float volume, float pitch) {
        super(PLACEHOLDER_EVENT, SoundCategory.PLAYERS);
        this.entity = entity;
        this.registryName = registryName;
        this.repeat = false;
        this.repeatDelay = 0;
        this.attenuationType = AttenuationType.NONE;
        this.volume = volume;
        this.pitch = pitch;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null && soundDistance > 0) {
            float scaledVolume = this.volume * (1.0F - Math.min(1.0F, (float) Math.sqrt(player.getDistanceSq(entity.posX, entity.posY, entity.posZ)) / soundDistance));
            this.volume = scaledVolume * scaledVolume;
        }
        updatePosition();
    }

    @Override
    public void update() {
        if (entity == null || entity.isDead) {
            this.donePlaying = true;
            return;
        }
        updatePosition();
    }

    @Override
    public ResourceLocation getSoundLocation() {
        return registryName;
    }
    @Override
    public SoundEventAccessor createAccessor(SoundHandler handler) {
        SoundEventAccessor accessor = new SoundEventAccessor(registryName, "zero.sound.gun");
        Sound resolvedSound = new Sound(registryName.toString(), 1.0F, 1.0F, 1, Sound.Type.FILE, false);
        accessor.addSound(resolvedSound);
        this.sound = resolvedSound;
        return accessor;
    }

    public void stop() {
        this.donePlaying = true;
    }

    private void updatePosition() {
        this.xPosF = (float) entity.posX;
        this.yPosF = (float) entity.posY;
        this.zPosF = (float) entity.posZ;
    }
}
