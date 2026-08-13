package com.zero.client.util;

import com.zero.server.item.ItemGun;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PapiManager {


    public static String getText(String text, ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return text;
        }
        EntityPlayer player = Minecraft.getMinecraft().player;
        text = text.replaceAll("%player%", player.getName());

        Item item = itemStack.getItem();
        if (item instanceof ItemGun) {
            text = text.replaceAll("%ammo%", String.valueOf(((ItemGun) item).getCurrentAmmo(itemStack)));
        }
        text = text.replace("@", " ");
        return text;
    }

}
