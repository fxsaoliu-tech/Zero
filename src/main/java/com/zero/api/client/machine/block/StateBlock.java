package com.zero.api.client.machine.block;

import com.zero.api.client.machine.state.AnimationState;
import com.zero.api.client.machine.EnumState;
import com.zero.client.animation.AnimationController;
import com.zero.client.animation.json.BonePose;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Map;

public interface StateBlock {

    void onUpdate(AnimationController controller, EntityPlayer player);

    void onTick(AnimationController controller, EntityPlayer player);

    void triggerAnimation(AnimationController controller, EnumState state, Map<EnumState, AnimationState> map);

    Map<String, BonePose> sampleBonePose(AnimationController controller);

    int getTrack();

}
