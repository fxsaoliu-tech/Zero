package com.zero.api.client.machine.state.gun.move;

import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.api.client.machine.state.AnimationState;
import com.zero.api.client.machine.EnumState;
import com.zero.client.animation.AnimationController;
import com.zero.client.animation.AnimationPlayType;
import com.zero.client.animation.AnimationTransition;
import com.zero.client.animation.data.AnimationStateText;

import java.util.Map;

public class SprintSate implements AnimationState {
    private int mode = -1;
    private final GunStateMachine stateMachine;

    public SprintSate(GunStateMachine machine) {
        this.stateMachine = machine;
    }

    @Override
    public void initialization(AnimationController controller, int track) {
        mode = -1;
        controller.runAnimation(track, AnimationStateText.SPRINT_IN, AnimationPlayType.PLAY_ONCE_HOLD, 0.2f);
    }

    @Override
    public void onUpdate(AnimationController controller, int track) {
        AnimationTransition transition = controller.getAnimation(track);
        if (transition != null) {
            if (transition.isHoldLastFrame()) {
                controller.runAnimation(track, AnimationStateText.SPRINT_LOOP, AnimationPlayType.LOOP, 0.2f);
                stateMachine.anchorWalkDist();
                mode = 0;
            }
            if (mode != -1) {
                if (stateMachine.isOnGround()) {
                    if (mode != 1) {
                        mode = 1;
                        controller.runAnimation(track, AnimationStateText.SPRINT_LOCK, AnimationPlayType.LOOP, 0.2f);
                    }
                } else {
                    if (mode != 0) {
                        mode = 0;
                        controller.runAnimation(track, AnimationStateText.SPRINT_LOOP, AnimationPlayType.LOOP, 0.2f);
                    }
                    transition.setProgress(stateMachine.getMoveProgress() % 2.0F / 2.0F);
                }
            }
        }
    }

    @Override
    public void exit(AnimationController controller, int track) {
        controller.runAnimation(track, AnimationStateText.SPRINT_OUT, AnimationPlayType.PLAY_ONCE_HOLD, 0.3f);
    }

    @Override
    public AnimationState transition(AnimationController controller, int track, EnumState enumState, Map<EnumState, AnimationState> map) {
        if (enumState == EnumState.BREATHE) {
            return map.get(EnumState.BREATHE);
        } else if (enumState == EnumState.WALK) {
            AnimationTransition c = controller.getAnimation(1);
            if (c != null && c.isFinished()) {
                return map.get(EnumState.WALK);
            }
        }
        return null;
    }
}
