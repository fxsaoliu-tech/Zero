package com.zero.server.item;

import com.zero.server.type.BulletType;
import com.zero.server.type.tab.EnumTabType;
import com.zero.server.type.tab.TabUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBullets extends Item {
    private BulletType type;

    public ItemBullets(BulletType type) {
        this.type = type;
        this.setTranslationKey(type.id);//注册本地化
        this.setRegistryName(type.id);//注册名
        this.setMaxStackSize(type.maxStackSize);//子弹数量
        type.item = this;
        type.enumTabType = EnumTabType.BULLET;
        TabUtil.setTabs(type);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> lore, ITooltipFlag flagIn) {
        lore.add(I18n.format("tooltip.zero.ammo.info"));
        lore.add(I18n.format("tooltip.zero.ammo.info.fallSpeed") + " " + type.fallSpeed);
        lore.add(I18n.format("tooltip.zero.ammo.info.shootPenetrate") + " " + type.shootPenetrate);
    }

    public BulletType getType() {
        return type;
    }
}
