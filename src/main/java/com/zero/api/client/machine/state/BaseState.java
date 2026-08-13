package com.zero.api.client.machine.state;

import com.zero.api.client.machine.EnumState;
import com.zero.client.animation.AnimationController;
import com.zero.client.animation.AnimationPlayType;
import com.zero.client.animation.data.AnimationStateText;

import java.util.Map;

public class BaseState implements AnimationState {

    @Override
    public void initialization(AnimationController controller, int track) {
        controller.runAnimation(track, AnimationStateText.STATIC_IDLE, AnimationPlayType.LOOP, 0);
    }

    @Override
    public void onUpdate(AnimationController controller, int track) {

    }

    @Override
    public void exit(AnimationController controller, int track) {

    }

    @Override
    public AnimationState transition(AnimationController controller, int track, EnumState enumState, Map<EnumState, AnimationState> map) {
        return null;
    }
}
