package com.zero.api.client.machine.block.gun;

import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.api.client.machine.block.StateBlock;
import com.zero.api.client.machine.state.AnimationState;
import com.zero.api.client.machine.EnumState;
import com.zero.client.ClientProxy;
import com.zero.client.animation.AnimationController;
import com.zero.client.animation.AnimationTransition;
import com.zero.client.animation.json.BonePose;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Collections;
import java.util.Map;

public class FireModeStateBlock implements StateBlock {

    public FireModeStateBlock() {
    }

    @Override
    public void onUpdate(AnimationController controller, EntityPlayer player) {
    }

    @Override
    public void onTick(AnimationController controller, EntityPlayer player) {
    }

    @Override
    public void triggerAnimation(AnimationController controller, EnumState state, Map<EnumState, AnimationState> map) {
    }


    @Override
    public Map<String, BonePose> sampleBonePose(AnimationController controller) {
        AnimationTransition transition = controller.getAnimation(getTrack());
        if (transition != null) {
            return transition.sampleForRender();
        }
        return Collections.emptyMap();
    }

    @Override
    public int getTrack() {
        return 3;
    }
}
