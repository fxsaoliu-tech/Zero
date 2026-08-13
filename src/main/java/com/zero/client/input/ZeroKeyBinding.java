package com.zero.client.input;

import com.zero.Zero;
import com.zero.api.client.ZeroClientPlayer;
import com.zero.api.client.machine.ItemZeroStateMachine;
import com.zero.api.client.machine.animation.gun.GunAnimation;
import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.api.client.machine.EnumState;
import com.zero.client.input.type.KeyConflictContextGun;
import com.zero.client.sound.gun.GunSoundPlayManager;
import com.zero.client.util.ZeroResources;
import com.zero.network.gun.PacketGunFireSelect;
import com.zero.server.item.ItemGun;
import com.zero.server.item.ItemZero;
import com.zero.server.type.GunType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class ZeroKeyBinding {
    public static final KeyBinding KEY_R = getKey("key.zero.gun.reload", KeyConflictContextGun.IN_GAME, Keyboard.KEY_R);
    public static final KeyBinding KEY_H = getKey("key.zero.gun.inspect", KeyConflictContextGun.IN_GAME, Keyboard.KEY_H);
    public static final KeyBinding KEY_Z = getKey("key.zero.gun.openz", KeyConflictContextGun.IN_GAME, Keyboard.KEY_Z);
    public static final KeyBinding KEY_B = getKey("key.zero.gun.bfire", KeyConflictContextGun.IN_GAME, Keyboard.KEY_B);

    private Minecraft mc;

    public ZeroKeyBinding() {
        ClientRegistry.registerKeyBinding(KEY_R);
        ClientRegistry.registerKeyBinding(KEY_H);
        ClientRegistry.registerKeyBinding(KEY_Z);
        ClientRegistry.registerKeyBinding(KEY_B);
        mc = Minecraft.getMinecraft();
        Zero.eventRegister(this);
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event) {
        EntityPlayerSP player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getHeldItemMainhand();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemZero) {
            if (KEY_H.isPressed()) {
                ItemZeroStateMachine animation = ZeroClientPlayer.getStateMachine();
                if (animation != null && animation.canInspect()) {
                    animation.triggerAnimation(EnumState.INSPECT);
                }
            } else if (stack.getItem() instanceof ItemGun) {
                ItemGun gun = (ItemGun) stack.getItem();
                GunStateMachine gunStateMachine = (GunStateMachine) ZeroClientPlayer.getStateMachine();
                if (gunStateMachine != null) {
                    GunAnimation gunAnimation = gunStateMachine.getAnimation();
                    if (KEY_B.isPressed()) {
                        GunType gunType = gun.getType();
                        if (gunType.fireType != null && gunType.fireType.length > 1 && gunAnimation.canTrigger()) {
                            Zero.getPacketHandler().sendToServer(new PacketGunFireSelect());
                            gunStateMachine.triggerAnimation(EnumState.FIRE_MODE);
                        }
                    } else if (KEY_Z.isPressed()) {
                        player.openGui(Zero.zero, 0, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
                    } else if (KEY_R.isPressed()) {
                        if (ItemGun.isAmmoSufficient(stack)) {
                            if (ItemGun.canReload(stack, player.inventory)) {
                                if (gunAnimation.canReload()) {
                                    gunAnimation.beginReload();
                                    gunStateMachine.triggerAnimation(EnumState.RELOAD);
                                }
                            } else {
                                player.sendMessage(new TextComponentTranslation("zero.message.no_ammo"));
                            }
                        }
                    }
                }
            }
        }
    }

    private static KeyBinding getKey(String description, IKeyConflictContext context, int keyboard) {
        return new KeyBinding(description, context, keyboard, "key.category.Zero");
    }

}
