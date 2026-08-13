package com.zero.api.client.machine.state.gun.main;

import com.zero.api.client.ZeroClientPlayer;
import com.zero.api.client.machine.animation.gun.GunAnimation;
import com.zero.api.client.machine.state.AnimationState;
import com.zero.api.client.machine.EnumState;
import com.zero.client.animation.AnimationController;
import com.zero.client.animation.AnimationTransition;

import java.util.Map;

public class InspectState implements AnimationState {
    private GunAnimation gunAnimation;

    public InspectState(GunAnimation gunAnimation) {
        this.gunAnimation = gunAnimation;
    }

    @Override
    public void initialization(AnimationController controller, int track) {
        gunAnimation.inspect = true;
    }

    @Override
    public void onUpdate(AnimationController controller, int track) {
        AnimationTransition transition = controller.getAnimation(track);
        if (transition != null) {
            if (transition.isFinished()) {
                ZeroClientPlayer.getPlayer().getItemZero().triggerAnimation(EnumState.INSPECT_RETREAT);
            }
        }
    }

    @Override
    public void exit(AnimationController controller, int track) {
        gunAnimation.inspect = false;
    }

    @Override
    public AnimationState transition(AnimationController controller, int track, EnumState enumState, Map<EnumState, AnimationState> map) {
        if (enumState == EnumState.INSPECT_RETREAT) {
            AnimationTransition transition = controller.getAnimation(track);
            if (transition != null) {
                transition.setProgress(1);
            }
            return map.get(EnumState.IDLE);
        }
        return map.get(EnumState.IDLE).transition(controller, track, enumState, map);
    }
}
