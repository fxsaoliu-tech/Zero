package com.zero.api.client.machine.state.gun.move;

import com.zero.api.client.machine.state.AnimationState;
import com.zero.api.client.machine.EnumState;
import com.zero.client.animation.AnimationController;
import com.zero.client.animation.AnimationPlayType;
import com.zero.client.animation.AnimationTransition;
import com.zero.client.animation.data.AnimationStateText;

import java.util.Map;

public class BreatheSate implements AnimationState {
    @Override
    public void initialization(AnimationController controller, int track) {

    }

    @Override
    public void onUpdate(AnimationController controller, int track) {
        AnimationTransition transition = controller.getAnimation(track);
        if (transition != null) {
            if (transition.isFinished() || transition.isHoldLastFrame()) {
                controller.runAnimation(track, AnimationStateText.IDLE_BREATHE, AnimationPlayType.LOOP, 0);
            }
        } else {
            controller.runAnimation(track, AnimationStateText.IDLE_BREATHE, AnimationPlayType.LOOP, 0);
        }
    }

    @Override
    public void exit(AnimationController controller, int track) {

    }

    @Override
    public AnimationState transition(AnimationController controller, int track, EnumState enumState, Map<EnumState, AnimationState> map) {
        if (enumState == EnumState.SPRINT) {
            AnimationTransition transition = controller.getAnimation(1);
            if (transition != null && transition.isFinished()) {
                return map.get(enumState);
            }else {
                return map.get(EnumState.WALK);
            }
        }else if (enumState == EnumState.WALK) {
            return map.get(EnumState.WALK);
        }
        return null;
    }
}
