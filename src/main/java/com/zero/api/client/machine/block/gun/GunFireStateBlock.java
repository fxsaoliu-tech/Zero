package com.zero.api.client.machine.block.gun;

import com.zero.api.client.machine.block.StateBlock;
import com.zero.api.client.machine.state.AnimationState;
import com.zero.api.client.machine.state.gun.fire.GunFireState;
import com.zero.api.client.machine.EnumState;
import com.zero.client.animation.AnimationController;
import com.zero.client.animation.AnimationTransition;
import com.zero.client.animation.BoneMaskManager;
import com.zero.client.animation.json.BonePose;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;

public class GunFireStateBlock implements StateBlock {
    private final GunFireState fireState = new GunFireState();
    private final BoneMaskManager boneMaskManager = new BoneMaskManager();

    public GunFireStateBlock() {
    }

    @Override
    public void onUpdate(AnimationController controller, EntityPlayer player) {

    }

    @Override
    public void onTick(AnimationController controller, EntityPlayer player) {

    }

    @Override
    public void triggerAnimation(AnimationController controller, EnumState state, Map<EnumState, AnimationState> map) {
        if (state == EnumState.SHOOT) {
            int track = controller.findAvailableTrackLinear(4, 20);
            if (track == -1) {
                return;
            }
            fireState.transition(controller, track, state, map);
        }
    }

    @Override
    public Map<String, BonePose> sampleBonePose(AnimationController controller) {
        Map<String, BonePose> result = new HashMap<>();
        for (int i = getTrack(); i < 20; i++) {
            AnimationTransition transition = controller.getAnimation(i);
            if (transition != null) {
                if (!transition.isFinished()) {
                    Map<String, BonePose> pose = transition.sampleForRender();
                    result = boneMaskManager.blend(result, pose);
                } else {
                    controller.removeAnimation(i);
                }
            }
        }
        return result;
    }

    @Override
    public int getTrack() {
        return 4;
    }
}
