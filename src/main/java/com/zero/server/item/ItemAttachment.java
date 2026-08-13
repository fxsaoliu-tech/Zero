package com.zero.server.item;

import com.zero.server.type.AttachmentType;
import com.zero.server.type.tab.TabUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemAttachment extends Item {
    private AttachmentType type;

    public ItemAttachment(AttachmentType type) {
        this.type = type;
        this.setTranslationKey(type.id);//注册本地化
        this.setRegistryName(type.id);//注册名
        this.setMaxStackSize(1);//设置物品叠
        type.item = this;
        TabUtil.setTabs(type);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        AttachmentType att = this.type;

        tooltip.add(getTranslateKey("tooltip.zero.attachment.info")); // 标题
        // 📦 配件类型
        String types = getTranslateKey("tooltip.zero.attachment.type") + ": ";
        tooltip.add(types + getTranslateKey("tooltip.zero.attachment." + att.enumAttachmentType.getKey()));
        switch (att.enumAttachmentType) {
            case SCOPE:
                tooltip.add(getTranslateKey("tooltip.zero.attachment.scope.aim") + " " + att.getAimSpeed() + "s");
                tooltip.add(getTranslateKey("tooltip.zero.attachment.scope.fov") + " " + att.getFOVFactor() + "x");
                tooltip.add(getTranslateKey("tooltip.zero.attachment.scope.view") + " " + att.getViewFov());
                break;
            case MUZZLE:
                if (att.spreadMultiplier != 0) {
                    tooltip.add(getTranslateKey("tooltip.zero.attachment.spread") + " " + getColorNum(att.spreadMultiplier * 100));
                }
            case STOCK:
                if (att.recoilVerticalMultiplier != 0) {
                    tooltip.add(getTranslateKey("tooltip.zero.attachment.vertical") + " " + getColorNum(att.recoilVerticalMultiplier * 100));
                }
                if (att.recoilHorizontalMultiplier != 0) {
                    tooltip.add(getTranslateKey("tooltip.zero.attachment.horizontal") + " " + getColorNum(att.recoilHorizontalMultiplier * 100));
                }
                break;
            case LASER:
                if (att.color != null) {
                    tooltip.add(getTranslateKey("tooltip.zero.attachment.color") + " " + att.color[0] + " " + att.color[1] + " " + att.color[2]);
                }
                break;
            case EXTENDED_MAG:
                tooltip.add(getTranslateKey("tooltip.zero.attachment.level") + " " + att.level);
                break;
        }
    }

    private String getTranslateKey(String key) {
        return I18n.format(key);
    }

    private String getColorNum(float num) {
        String color = "";
        if (num < 0) {
            color = "§c";
        } else if (num > 0) {
            color = "§a";
        }
        return color + num + "%";
    }

    public AttachmentType getType() {
        return type;
    }
}
