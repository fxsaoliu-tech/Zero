package com.zero.client.input.type;

import com.zero.server.item.ItemGun;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.settings.IKeyConflictContext;

public enum KeyConflictContextGun implements IKeyConflictContext {
    UNIVERSAL {
        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return true;
        }
    },
    GUI {
        @Override
        public boolean isActive() {
            return Minecraft.getMinecraft().currentScreen != null;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }
    },
    IN_GAME {
        @Override
        public boolean isActive() {
            ItemStack stack = Minecraft.getMinecraft().player.inventory.getCurrentItem();
            if (!stack.isEmpty() && !GUI.isActive()) {
                if (stack.getItem() instanceof ItemGun) {
                    return stack.getItem() instanceof ItemGun;
                }
            }
            return false;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }
    }
}
