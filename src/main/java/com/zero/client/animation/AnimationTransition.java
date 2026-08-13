package com.zero.client.animation;

import com.zero.client.animation.json.Animation;
import com.zero.client.animation.json.BonePose;
import com.zero.client.util.math.MathUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnimationTransition {
    private final AnimationPlayer current;  // 当前动画（独占）
    private AnimationPlayer next;     // 过渡目标动画（独占副本）

    private long lastUpdateNs = -1L;            // 系统纳秒时间（用于 delta 计算）
    private float transitionDuration = 0.2f;    // 秒
    private float transitionTime = 0f;          // 秒（已过时间）
    private boolean isTransitioning = false;

    private AnimationTransition toRun;     // 原始 target runner 引用（仅保留作信息）

    private final Map<String, BonePose> poseCache = new HashMap<>();

    public AnimationTransition(Animation animation) {
        this.current = new AnimationPlayer(animation);
    }

    /**
     * tick: 推进当前播放器与 next（若存在）的时间。
     * - 当未过渡时，推进 current。
     * - 当过渡时，推进 current 与 next（保持两边时间同步），并推进 transitionTime。
     */
    public void tick() {
        if (!isTransitioning) {
            // 普通播放，推进 current
            current.tick();
            return;
        }
        long now = System.nanoTime();
        float deltaSec = lastUpdateNs < 0 ? 0f : (now - lastUpdateNs) / 1e9f;
        lastUpdateNs = now;

        transitionTime += deltaSec;
        if (transitionTime >= transitionDuration) {
            isTransitioning = false;
            transitionTime = 0f;
        }
    }

    public void transitionTo(AnimationTransition target, float durationSec) {
        if (target == null) return;
        // 1) 冻结当前可见姿态为 snapshot（使用 sampleForRender 获取真实混合结果）
        Map<String, BonePose> snapshotPose = sampleForRender(); // 若当前没有过渡则相当于 current.sampleForRender
        // snapshotPlayer 已经 pause()，它是独占的 current
        this.poseCache.putAll(snapshotPose);
        this.next = target.current;
        this.toRun = target;
        this.transitionDuration = Math.max(0.0001f, durationSec);
        this.transitionTime = 0f;
        this.isTransitioning = true;
        this.lastUpdateNs = System.nanoTime();
    }


    private float getCurrentAlpha() {
        if (!isTransitioning || transitionDuration <= 0f) return 1f;
        long now = System.nanoTime();
        float elapsed = transitionTime + (now - lastUpdateNs) / 1e9f;
        float raw = Math.min(1f, elapsed / transitionDuration);
        return easeOutCubic(raw);
    }

    /**
     * 获取当前渲染用姿势（对当前与 next 做混合）
     */
    public Map<String, BonePose> sampleForRender() {
        if (!isTransitioning || next == null) {
            return current.sampleForRender();
        }
        Map<String, BonePose> snapshotPose = new HashMap<>();
        float alpha = getCurrentAlpha();
        Map<String, BonePose> currPose = poseCache;
        Map<String, BonePose> nextPoseMap = next.sampleForRender();

        // 合并所有骨骼
        Set<String> allBones = new HashSet<>();
        allBones.addAll(currPose.keySet());
        allBones.addAll(nextPoseMap.keySet());

        for (String bone : allBones) {
            BonePose a = currPose.get(bone);
            BonePose b = nextPoseMap.get(bone);
            BonePose blended;

            if (a != null && b != null) {
                // 两个动画都有此骨骼 → 正常平滑过渡
                blended = new BonePose();
                if (a.position != null && b.position != null) {
                    blended.position = MathUtil.lerp(a.position, b.position, alpha);
                }
                if (a.scale != null && b.scale != null) {
                    blended.scale = MathUtil.lerp(a.scale, b.scale, alpha);
                }
                if (a.rotation != null && b.rotation != null) {
                    blended.rotation = MathUtil.slerp(a.rotation, b.rotation, alpha);
                }
            } else if (a != null) {
                // 只有当前动画有此骨骼 → 直接使用当前值（不混合）
                blended = new BonePose();
            } else { // b != null
                // 只有下一动画有此骨骼 → 直接使用下一值
                blended = b;
            }
            snapshotPose.put(bone, blended);
        }
        return snapshotPose;
    }

    public AnimationTransition getToRun() {
        return toRun;
    }

    private float easeOutCubic(double x) {
        return (float) (1 - Math.pow(1 - x, 4));
    }

    public boolean isTransitioning() {
        return isTransitioning;
    }

    public boolean isFinished() {
        if (next != null) {
            return next.isFinished();
        }
        return current.isFinished();
    }

    public boolean isPaused() {
        return current.isPaused();
    }

    public float getTime() {
        return current.getTime();
    }

    public float getProgress() {
        return current.getProgress();
    }

    public void setProgress(float progress) {
        current.setProgress(progress);
        if (next != null) {
            next.setProgress(progress);
        }
    }

    public boolean isHoldLastFrame() {
        if (next != null) {
            return next.isHoldLastFrame();
        }
        return current.isHoldLastFrame();
    }

    public void pause() {
        current.pause();
        if (next != null) {
            next.pause();
        }
    }

    public void resume() {
        current.resume();
        if (next != null) {
            next.resume();
        }
    }

    public AnimationPlayer getCurrent() {
        return current;
    }

    public void setPlayType(AnimationPlayType type) {
        current.setPlayType(type);
    }

    public void setTimeScale(float timeScale) {
        current.setTimeScale(timeScale);
    }
}