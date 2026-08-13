package com.zero.api.client.machine.state.gun.main;

import com.zero.api.client.ZeroClientPlayer;
import com.zero.api.client.machine.animation.gun.GunStateMachine;
import com.zero.api.client.machine.state.AnimationState;
import com.zero.api.client.machine.EnumState;
import com.zero.client.animation.AnimationController;
import com.zero.client.animation.AnimationPlayType;
import com.zero.client.animation.data.AnimationStateText;

import java.util.Map;

public class IdleState implements AnimationState {
    private GunStateMachine stateMachine;

    public IdleState(GunStateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

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
        if (enumState == EnumState.PUT_AWAY) {
            float f = ZeroClientPlayer.getPlayer().getItemZero().holder.getProgress();
            controller.runAnimation(track, AnimationStateText.PUT_AWAY, AnimationPlayType.PLAY_ONCE_HOLD, f * 0.75f);
            controller.getAnimation(track).setProgress(1 - f);
            return map.get(EnumState.FINAL);
        } else if (enumState == EnumState.RELOAD) {
            this.stateMachine.getAnimation().playReloadAnimation(track, controller);
            return map.get(EnumState.IDLE);
        } else if (enumState == EnumState.INSPECT) {
            this.stateMachine.getAnimation().playInspectAnimation(track, controller);
            return map.get(EnumState.INSPECT);
        } else if (enumState == EnumState.BOLT) {
            this.stateMachine.getAnimation().playBoltAnimation(track, controller);
            return map.get(EnumState.IDLE);
        } else if (enumState == EnumState.FIRE_MODE) {
            this.stateMachine.getAnimation().playTiggerAnimation(track, controller);
            return map.get(EnumState.IDLE);
        }
        return null;
    }


}
