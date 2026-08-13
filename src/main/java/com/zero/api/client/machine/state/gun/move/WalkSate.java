package com.zero.api.client.machine.state.gun.move;

import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.api.client.machine.state.AnimationState;
import com.zero.api.client.machine.EnumState;
import com.zero.client.animation.AnimationController;
import com.zero.client.animation.AnimationPlayType;
import com.zero.client.animation.AnimationTransition;
import com.zero.client.animation.data.AnimationStateText;

import java.util.Map;

public class WalkSate implements AnimationState {
    private int mode = -1;
    private final GunStateMachine stateMachine;

    public WalkSate(GunStateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    public void initialization(AnimationController controller, int track) {
        mode = -1;
    }

    @Override
    public void onUpdate(AnimationController controller, int track) {
        if (stateMachine.isOnGround()) {
            if (mode != 0) {
                mode = 0;
                controller.runAnimation(track, AnimationStateText.IDLE_BREATHE, AnimationPlayType.LOOP, 0.6f);
            }
        } else if (stateMachine.isInputUp()) {
            if (mode != 2) {
                mode = 2;
                controller.runAnimation(track, AnimationStateText.WALK_FORWARD, AnimationPlayType.LOOP, 0.4f);
                stateMachine.anchorWalkDist();
            }
        } else if (stateMachine.isInputDown()) {
            if (mode != 3) {
                mode = 3;
                controller.runAnimation(track, AnimationStateText.WALK_BACKWARD, AnimationPlayType.LOOP, 0.4f);
                stateMachine.anchorWalkDist();
            }
        } else if (stateMachine.isInputLeft() || stateMachine.isInputRight()) {
            if (mode != 4) {
                mode = 4;
                controller.runAnimation(track, AnimationStateText.WALK_SIDE, AnimationPlayType.LOOP, 0.4f);
                stateMachine.anchorWalkDist();
            }
        }
        if (mode >= 1 && mode <= 4) {
            AnimationTransition transition = controller.getAnimation(track);
            if (transition != null) {
               transition.setProgress((stateMachine.getMoveProgress() % 2.0f) / 2.0f);
            }
        }
    }

    @Override
    public void exit(AnimationController controller, int track) {
        controller.runAnimation(track, AnimationStateText.IDLE_BREATHE, AnimationPlayType.LOOP, 0.4f);
    }

    @Override
    public AnimationState transition(AnimationController controller, int track, EnumState enumState, Map<EnumState, AnimationState> map) {
        if (enumState == EnumState.BREATHE) {
            return map.get(enumState);
        }else if (enumState == EnumState.SPRINT) {
            AnimationTransition transition = controller.getAnimation(1);
            if (transition != null && transition.isFinished()) {
                return map.get(enumState);
            }
        }
        return null;
    }
}
