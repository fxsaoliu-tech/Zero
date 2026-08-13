package com.zero.client.render;

import com.zero.server.type.InfoType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface RenderCustomItem {

    void render(CustomItemRenderType type, ItemStack stack, InfoType infoType, EntityLivingBase base);

}
