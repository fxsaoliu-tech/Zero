package com.zero.server.type;

import com.zero.Zero;
import com.zero.client.model.BedrockAmmoModel;
import com.zero.server.file.FileList;
import com.zero.server.item.ItemBullets;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulletType extends ShootsType {
    public int shootPenetrate = 0;//能穿透几个实体

    public List<PotionEffect> hitEffects = new ArrayList<>();//击中目标可附带药水效果

    private static final Map<String, BulletType> TYPE = new HashMap<>();

    public BedrockAmmoModel ammoModel;

    public BulletType() {
    }

    @Override
    public BulletType loadContent(FileList file) {
        super.loadContent(file);
        TYPE.put(this.id, this);
        return this;
    }

    @Override
    protected void read(String[] content, FileList file) {
        super.read(content, file);
        shootPenetrate = Read(content, "shootPenetrate", shootPenetrate, file);

        if (content[0].equalsIgnoreCase("PotionEffect")) {
            hitEffects.add(getPotionEffect(content));
        }
    }

    @Override
    protected void readClient(String[] content, FileList file) {
        super.readClient(content, file);

        String model = Read(content, "model", "", file);
        if (!model.isEmpty()) {
            this.ammoModel = new BedrockAmmoModel(Zero.server.loadModel(file, "ammo/" + model));
        }
    }

    public static BulletType getBulletType(String id) {
        if (TYPE.containsKey(id)) {
            return TYPE.get(id);
        }
        return null;
    }

    public static BulletType getBulletType(ItemStack stack) {
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof ItemBullets) {
                return getBulletType(((ItemBullets) stack.getItem()).getType().id);
            }
        }
        return null;
    }


}

