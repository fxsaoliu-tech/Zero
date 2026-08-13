package com.zero.server.type.tab;

import com.zero.server.type.InfoType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TabUtil {
    private static final Map<EnumTabType, CreativeTabs> tabs = new HashMap<>();
    private static final Map<EnumTabType, List<Item>> tabItems = new HashMap<>();


    public static void registerCreativeTabs() {
        for (EnumTabType type : EnumTabType.values()) {
            if (!tabs.containsKey(type)) {
                tabs.put(type, new CreativeTabZero(type));
                tabItems.put(type, new ArrayList<>());
            }
        }
    }

    public static void registerCreativeTabsItem() {
        for (InfoType infoType : InfoType.getInfoType()) {
            if (infoType.enumTabType == null) {
                continue;
            }
            if (tabItems.containsKey(infoType.enumTabType)) {
                tabItems.get(infoType.enumTabType).add(infoType.item);
            }
        }
    }

    public static List<Item> getTabItems(EnumTabType infoType) {
        if (tabItems.containsKey(infoType)) {
            return tabItems.get(infoType);
        }
        return null;
    }

    public static void setTabs(InfoType infoType) {
        if (infoType.enumTabType == null) {
            return;
        }
        if (tabs.containsKey(infoType.enumTabType)) {
            infoType.item.setCreativeTab(tabs.get(infoType.enumTabType));
        }
    }
}
