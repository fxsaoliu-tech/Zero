package com.zero.api.client.machine.state;

import com.zero.client.animation.AnimationController;
import com.zero.api.client.machine.EnumState;

import java.util.Map;

/**
 * 动画状态接口，用于控制动画的生命周期和状态切换。
 */
public interface AnimationState {

    /**
     * 初始化状态，进入状态前执行一次。
     *
     * @param controller 动画控制器
     */
    void initialization(AnimationController controller,int track);

    /**
     * 每帧更新状态逻辑。
     *
     * @param controller 动画控制器
     */
    void onUpdate(AnimationController controller,int track);

    /**
     * 退出状态时执行，用于清理或停止动画。
     *
     * @param controller 动画控制器
     */
    void exit(AnimationController controller,int track);

    /**
     * 状态切换逻辑。
     *
     * @param controller   动画控制器
     * @param enumState 当前道具状态
     * @return 下一个状态对象，如果不切换则返回 null
     */
    AnimationState transition(AnimationController controller, int track, EnumState enumState, Map<EnumState, AnimationState> map);
}