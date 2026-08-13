package com.zero.server.entity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityItemZero extends EntityItem {

    public EntityItemZero(EntityItem entity) {
        super(entity.world, entity.posX, entity.posY, entity.posZ, entity.getItem());
        this.motionX = entity.motionX;
        this.motionY = entity.motionY;
        this.motionZ = entity.motionZ;
        this.setPickupDelay(40);
    }

    public EntityItemZero(World world, double posX, double posY, double posZ, ItemStack stack) {
        super(world, posX, posY, posZ, stack);
    }

    public EntityItemZero(World world) {
        super(world);
    }

    public EntityItemZero(World w, double x, double y, double z) {
        super(w, x, y, z);
    }


}
