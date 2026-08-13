package com.zero.server.type.tab;

import com.zero.Zero;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;

public class CreativeTabZero extends CreativeTabs {
    private EnumTabType type;
    private int icon;

    public CreativeTabZero(EnumTabType type) {
        super("tabZero." + type.getName());
        this.type = type;
    }


    @Override
    public ItemStack getIcon() {
        return this.createIcon();
    }

    @Override
    public ItemStack createIcon() {
        List<Item> list = TabUtil.getTabItems(type);
        icon = Zero.tick / 20;
        if (list != null && !list.isEmpty()) {
            return new ItemStack(list.get(icon % list.size()));
        }
        return new ItemStack(Blocks.WOOL, 1, 1);
    }
}
