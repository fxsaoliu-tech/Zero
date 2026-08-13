package com.zero.api.client.machine.block.gun;

import com.zero.api.client.machine.block.StateBlock;
import com.zero.api.client.machine.state.AnimationState;
import com.zero.api.client.machine.state.gun.main.DrawState;
import com.zero.api.client.machine.EnumState;
import com.zero.client.animation.AnimationController;
import com.zero.client.animation.AnimationTransition;
import com.zero.client.animation.json.BonePose;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Collections;
import java.util.Map;

public class MainStateBlock implements StateBlock {
    private AnimationState animationState;

    public MainStateBlock(AnimationController controller) {
        animationState = new DrawState();
        animationState.initialization(controller, getTrack());
    }

    @Override
    public void onUpdate(AnimationController controller, EntityPlayer player) {
        animationState.onUpdate(controller, getTrack());
    }

    @Override
    public void onTick(AnimationController controller, EntityPlayer player) {

    }

    @Override
    public void triggerAnimation(AnimationController controller, EnumState state, Map<EnumState, AnimationState> map) {
        int track = getTrack();
        // 1. 从 map 获取目标状态（复用实例）
        AnimationState next = animationState.transition(controller, getTrack(), state, map);
        if (next == null) return;
        // 3. 退出旧状态
        if (animationState != null) {
            animationState.exit(controller, track);
        }
        // 4. 切换状态
        animationState = next;
        // 5. 初始化新状态
        animationState.initialization(controller, track);
    }

    @Override
    public Map<String, BonePose> sampleBonePose(AnimationController controller) {
        AnimationTransition transition = controller.getAnimation(getTrack());
        if (transition != null && !transition.isFinished()) {
            return transition.sampleForRender();
        }
        return Collections.emptyMap();
    }

    @Override
    public int getTrack() {
        return 1;
    }
}
