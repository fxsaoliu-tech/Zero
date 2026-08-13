package com.zero.client.animation;

import com.zero.client.animation.data.AnimationData;
import com.zero.client.animation.json.Animation;

import javax.annotation.Nullable;
import java.util.*;

/**
 * 多轨道动画控制器
 * - 支持平滑过渡、链式动画、动画队列
 * - 自动从 AnimationData 获取动画
 */
public class AnimationController {
    /**
     * 动画数据
     */
    private final AnimationData animationData;
    /**
     * 当前轨道上的动画
     */
    private final List<AnimationTransition> currentRunners = new ArrayList<>();
    /**
     * 每条轨道的动画队列
     */
    private final List<Queue<AnimationPlan>> animationQueue = new ArrayList<>();

    public AnimationController(AnimationData animationData) {
        this.animationData = animationData;
    }

    public int findAvailableTrackLinear(int startTrack, int endTrack) {
        for (int i = startTrack; i < endTrack; i++) {
            AnimationTransition transition = this.getAnimation(i);
            if (transition == null) {
                return i;
            } else if (transition.isFinished() || transition.isHoldLastFrame()) {
                return i;
            }
        }
        return -1;
    }


    @Nullable
    public AnimationTransition getAnimation(int track) {
        if (track >= currentRunners.size()) {
            return null;
        }
        return currentRunners.get(track);
    }

    public void removeAnimation(int track) {
        if (track < currentRunners.size()) {
            currentRunners.set(track, null);
        }
        if (track < animationQueue.size()) {
            animationQueue.set(track, null);
        }
    }

    public void queueAnimation(int track, Queue<AnimationPlan> queue) {
        // 确保数组长度正确
        for (int i = animationQueue.size(); i <= track; i++) {
            animationQueue.add(null);
        }
        animationQueue.set(track, queue);
        if (queue != null) {
            AnimationPlan plan = null;
            while (plan == null && !queue.isEmpty()) {
                plan = queue.poll();
            }
            if (plan != null) {
                run(track, plan.name, plan.type, plan.transitionTimeS);
            }
        }
    }

    public void runAnimation(int track, String animationName, AnimationPlayType type, float transitionTimeS) {
        // 运行单个动画的时候视为执行一个只有一个动画的动画队列，因此需要清理旧的队列。
        if (track < animationQueue.size()) {
            animationQueue.set(track, null);
        }
        run(track, animationName, type, transitionTimeS);
    }

    synchronized private void run(int track, String animationName, AnimationPlayType playType, float transitionTimeS) {
        Animation animation = animationData.getAnimation(animationName);
        if (animation == null) {
            return;
        }
        // 确保数组长度正确
        for (int i = currentRunners.size(); i <= track; i++) {
            currentRunners.add(null);
        }
        AnimationTransition runner = new AnimationTransition(animation);
        runner.setPlayType(playType);
        runner.setProgress(0);

        AnimationTransition oldRunner = currentRunners.get(track);
        if (transitionTimeS > 0) {
            if (oldRunner != null) {
                oldRunner.transitionTo(runner, transitionTimeS);
            } else {
                currentRunners.set(track, runner);
            }
        } else {
            currentRunners.set(track, runner);
        }
    }

    /**
     * 每帧
     */
    public synchronized void update() {
        for (int track = 0; track < currentRunners.size(); track++) {
            updateTrack(track);
        }
    }

    private void updateTrack(int track) {
        if (track >= currentRunners.size()) return;

        AnimationTransition player = currentRunners.get(track);
        if (player == null) return;

        // 推进当前动画
        if (!player.isFinished() || player.isHoldLastFrame() || !player.isPaused() || player.isTransitioning()) {
            player.tick();
        }
        // 检查链式动画
        AnimationTransition next = player.getToRun();
        if (next != null) {
            next.tick();
            if (!player.isTransitioning()) {
                currentRunners.set(track, next);
                player = next;
            }
        }
        // 如果动画结束，检查队列是否有下一个动画，有则播放
        if ((player.isHoldLastFrame() || !player.isFinished()) && !player.isTransitioning()) {
            if (track < animationQueue.size()) {
                Queue<AnimationPlan> queue = animationQueue.get(track);
                if (queue != null) {
                    AnimationPlan plan = null;
                    while (plan == null && !queue.isEmpty()) {
                        plan = queue.poll();
                    }
                    if (plan != null) {
                        run(track, plan.name, plan.type, plan.transitionTimeS);
                    }
                }
            }
        }
    }
}