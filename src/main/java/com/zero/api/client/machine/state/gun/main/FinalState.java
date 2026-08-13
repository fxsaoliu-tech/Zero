package com.zero.api.client.machine.state.gun.main;

import com.zero.api.client.machine.state.AnimationState;
import com.zero.api.client.machine.EnumState;
import com.zero.client.animation.AnimationController;

import java.util.Map;

public class FinalState implements AnimationState {
    @Override
    public void initialization(AnimationController controller, int track) {

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
