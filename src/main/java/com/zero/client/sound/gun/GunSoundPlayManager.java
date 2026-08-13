package com.zero.client.sound.gun;

import com.zero.Zero;
import com.zero.client.sound.GunSound;
import com.zero.client.util.ZeroResources;
import com.zero.server.file.FileList;
import com.zero.server.file.FileType;
import com.zero.server.item.ItemGun;
import com.zero.server.type.GunType;
import com.zero.server.type.InfoType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GunSoundPlayManager {
    private final static ResourceLocation dry_fire = new ResourceLocation(Zero.MOD_ID, "dry_fire");
    private final static ResourceLocation fire_select = new ResourceLocation(Zero.MOD_ID, "fire_select");
    private final static ResourceLocation kill_cue = new ResourceLocation(Zero.MOD_ID, "kill_cue");
    //当前播放的音效
    private static GunSound currentSound = null;

    //播放抬起音效
    public static void playDrawSound(Entity entity, ItemGun itemGun, float volume, float pitch, int distance) {
        GunType gunType = itemGun.getType();
        if (gunType.drawSound.isEmpty()) {
            return;
        }
        ResourceLocation resource = ZeroResources.getSoundResource(gunType.id + "/" + gunType.drawSound);
        if (resource == null) {
            return;
        }
        if (currentSound != null) {
            currentSound.stop();
        }
        currentSound = new GunSound(entity, distance, resource, volume, pitch);
        FMLClientHandler.instance().getClient().getSoundHandler().playSound(currentSound);
    }

    //播放收起动画
    public static void playPutAwaySound(Entity entity, ItemGun itemGun, float volume, float pitch, int distance) {
        GunType gunType = itemGun.getType();
        if (gunType.putAwaySound.isEmpty()) {
            return;
        }
        ResourceLocation resource = ZeroResources.getSoundResource(gunType.id + "/" + gunType.putAwaySound);
        if (resource == null) {
            return;
        }
        if (currentSound != null) {
            currentSound.stop();
        }
        currentSound = new GunSound(entity, distance, resource, volume, pitch);
        FMLClientHandler.instance().getClient().getSoundHandler().playSound(currentSound);
    }

    //播放检视音效
    public static void playInspectSound(Entity entity, ItemGun itemGun, ItemStack itemStack, float volume, float pitch, int distance) {
        GunType gunType = itemGun.getType();
        String ammo = itemGun.getCurrentAmmo(itemStack) > 0 ? gunType.inspectSound : gunType.inspectEmptySound;
        if (ammo.isEmpty()) {
            return;
        }
        ResourceLocation resource = ZeroResources.getSoundResource(gunType.id + "/" + ammo);
        if (resource == null) {
            return;
        }
        if (currentSound != null) {
            currentSound.stop();
        }
        currentSound = new GunSound(entity, distance, resource, volume, pitch);
        FMLClientHandler.instance().getClient().getSoundHandler().playSound(currentSound);
    }


    //播放检视音效
    public static void playReloadSound(Entity entity, ItemGun itemGun, ItemStack itemStack, float volume, float pitch, int distance) {
        GunType gunType = itemGun.getType();
        String ammo = itemGun.getCurrentAmmo(itemStack) > 0 ? gunType.reloadTacticalSound : gunType.reloadEmptySound;
        if (ammo.isEmpty()) {
            return;
        }
        ResourceLocation resource = ZeroResources.getSoundResource(gunType.id + "/" + ammo);
        if (resource == null) {
            return;
        }
        if (currentSound != null) {
            currentSound.stop();
        }
        currentSound = new GunSound(entity, distance, resource, volume, pitch);
        FMLClientHandler.instance().getClient().getSoundHandler().playSound(currentSound);
    }

    //播放抬起音效
    public static void playBoltSound(Entity entity, ItemGun itemGun, float volume, float pitch, int distance) {
        GunType gunType = itemGun.getType();
        if (gunType.boltSound.isEmpty()) {
            return;
        }
        ResourceLocation resource = ZeroResources.getSoundResource(gunType.id + "/" + gunType.boltSound);
        if (resource == null) {
            return;
        }
        if (currentSound != null) {
            currentSound.stop();
        }
        currentSound = new GunSound(entity, distance, resource, volume, pitch);
        FMLClientHandler.instance().getClient().getSoundHandler().playSound(currentSound);
    }

    public static void playFireSelectSound(Entity entity, GunType gunType) {
        GunSound sound = new GunSound(entity, 16, fire_select, 1, 1);

        if (!gunType.fireSelectSound.isEmpty()) {
            ResourceLocation resource = ZeroResources.getSoundResource(gunType.id + "/" + gunType.fireSelectSound);
            if (resource != null) {
                sound = new GunSound(entity, 16, resource, 1, 1);
            }
        }

        FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
    }


    public static void playDryFireSound(Entity entity, GunType gunType) {
        GunSound sound = new GunSound(entity, 16, dry_fire, 1, 1);

        if (!gunType.dryFireSound.isEmpty()) {
            ResourceLocation resource = ZeroResources.getSoundResource(gunType.id + "/" + gunType.dryFireSound);
            if (resource != null) {
                sound = new GunSound(entity, 16, resource, 1, 1);
            }
        }

        FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
    }

    //播放射击音效
    public static void playShootSound(Entity entity, ItemGun itemGun, boolean silenced) {
        GunType gunType = itemGun.getType();
        if (gunType.shootSound.isEmpty()) {
            return;
        }
        ResourceLocation resourceLocation = ZeroResources.getSoundResource(gunType.id + "/" + gunType.shootSound);
        if (resourceLocation == null) {
            return;
        }
        float volume = silenced ? 5 : 10;
        float pitch = silenced ? 2F : 1F;
        pitch = pitch * (0.97F + entity.world.rand.nextFloat() * 0.06F);
        pitch = (float) Math.random() + pitch;
        float distance = silenced ? 32 : 64;
        FMLClientHandler.instance().getClient().getSoundHandler().playSound(new GunSound(entity, distance, resourceLocation, volume, pitch));
    }

    public static void playKillSound(Entity entity, InfoType gunType) {
        GunSound sound = new GunSound(entity, 16, kill_cue, 1, 1);
        if (!gunType.killSound.isEmpty()) {
            ResourceLocation resource = ZeroResources.getSoundResource(gunType.id + "/" + gunType.killSound);
            if (resource != null) {
                sound = new GunSound(entity, 16, resource, 1, 1);
            }
        }
        FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);
    }
}
