package com.zero.client.animation;

import com.zero.Zero;
import com.zero.client.animation.json.Animation;
import com.zero.client.animation.json.AnimationChannel;
import com.zero.client.animation.json.BonePose;
import com.zero.client.animation.json.Keyframe;
import com.zero.client.animation.json.InterpolationType;
import com.zero.client.sound.GunSound;
import com.zero.client.sound.gun.GunSoundPlayManager;
import com.zero.client.util.ZeroResources;
import com.zero.client.util.math.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.*;

/**
 * AnimationPlayer - 基于系统时间的动画播放器
 * 逻辑动画时间使用系统时间差推进，无动画过渡功能，只负责单个动画的播放与采样
 */
public class AnimationPlayer {
    // ==================== 常量定义 ====================
    private static final float EPS = 1e-5f;
    // ==================== 核心数据 ====================
    private final Animation animation;
    // 动画时间状态
    private float time = 0f;
    private float prevTime = 0f;
    private float timeScale = 1f; // 时间压缩倍率
    private AnimationPlayType type;//播放模式
    private boolean finished = false;//是否播放完成
    private boolean holdLastFrame = false;//是否锁定最后一针
    private boolean paused = false; // 暂停状态
    // 姿势数据
    private final Map<String, BonePose> currentPose = new HashMap<>();
    private final Set<Integer> triggeredEventKeys = new HashSet<>();
    // 系统时间跟踪
    private long startTimeNs = -1L; // 毫秒（单调时间来源于 System.nanoTime）
    private float progress = 0;

    public AnimationPlayer(Animation animation) {
        this.animation = animation;
        this.type = animation.getType();
        initializePose();
    }

    private void initializePose() {
        // 初始化姿势存储
        if (animation != null) {
            for (String bone : animation.getChannels().keySet()) {
                currentPose.put(bone, new BonePose());
            }
        }
        // 采样初始帧并设置prevPose
        sampleAnimation(0f);
        triggerEvents(prevTime, time);
    }

    public void tick() {
        if (paused || finished || holdLastFrame) return;
        if (animation == null || animation.getLength() <= 0f) return;

        long now = System.nanoTime();

        if (startTimeNs < 0) {
            startTimeNs = now;
        }
        prevTime = time;

        float elapsed = (now - startTimeNs) / 1_000_000_000f * timeScale;

        progress = elapsed / animation.getLength();

        if (progress >= 1f) {
            switch (type) {
                case LOOP:
                    progress %= 1f;
                    startTimeNs = now - (long) (progress * animation.getLength() / timeScale * 1_000_000_000L);
                    triggeredEventKeys.clear();
                    break;
                case PLAY_ONCE_HOLD:
                    progress = 1f;
                    holdLastFrame = true;
                    break;
                case PLAY_ONCE_STOP:
                    progress = 1f;
                    finished = true;
                    break;
            }
        }
        time = progress * animation.getLength();
        sampleAnimation(time);
        triggerEvents(prevTime, time);
    }


    public boolean isFinished() {
        return finished;
    }

    public boolean isPaused() {
        return paused;
    }

    public float getTime() {
        return time;
    }

    public boolean isHoldLastFrame() {
        return holdLastFrame;
    }

    public float getProgress() {
        return progress;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        if (paused) {
            paused = false;
            long now = System.nanoTime();
            // 根据当前 progress 重新计算 startTimeNs
            startTimeNs = now - (long) (progress * animation.getLength() / timeScale * 1_000_000_000L);
        }
    }

    //设置播放属性
    public void setPlayType(AnimationPlayType type) {
        this.type = type;
    }

    public void setTimeScale(float timeScale) {
        this.timeScale = Math.max(0.1f, timeScale);
    }

    public void setProgress(float progress) {
        progress = Math.max(0f, Math.min(1f, progress));
        this.progress = progress;
        prevTime = time;
        time = progress * animation.getLength();
        long now = System.nanoTime();
        startTimeNs = now - (long) (progress * animation.getLength() / timeScale * 1_000_000_000L);
    }

    // ==================== 姿势采样与渲染 ====================
    public Map<String, BonePose> sampleForRender() {
        return currentPose;
    }

    private void sampleAnimation(float t) {
        if (animation == null) {
            return;
        }
        for (Map.Entry<String, AnimationChannel> entry : animation.getChannels().entrySet()) {
            String boneName = entry.getKey();
            AnimationChannel channel = entry.getValue();

            BonePose pose = currentPose.computeIfAbsent(boneName, k -> new BonePose());

            float[] pos = sampleVec3f(channel.position, t);
            float[] rot = sampleVec3f(channel.rotation, t);
            float[] scale = sampleVec3f(channel.scale, t);
            pose.position = pos;
            pose.rotation = rot;
            pose.scale = scale;
        }
    }

    private float[] sampleVec3f(List<Keyframe> keyframes, float t) {
        if (keyframes == null || keyframes.isEmpty()) {
            return null;
        }
        int n = keyframes.size();
        if (n == 1) {
            return keyframes.get(0).sample(t);
        }
        int idx = findKeyframeIndex(keyframes, t);
        return interpolateKeyframes(keyframes, idx, t);
    }

    private int findKeyframeIndex(List<Keyframe> keyframes, float t) {
        int low = 0;
        int high = keyframes.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            float midTime = keyframes.get(mid).time;
            if (midTime < t) {
                low = mid + 1;
            } else if (midTime > t) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return Math.max(0, low - 1);
    }

    private float[] interpolateKeyframes(List<Keyframe> keyframes, int idx, float t) {
        int n = keyframes.size();

        Keyframe prev = keyframes.get(idx);
        Keyframe next = keyframes.get(Math.min(idx + 1, n - 1));

        float dt = next.time - prev.time;

        if (dt <= EPS) {
            return prev.sample(t);
        }

        float factor = (t - prev.time) / dt;

        if (factor <= 0f) {
            return prev.sample(t);
        }

        if (factor >= 1f) {
            return next.sample(t);
        }

        InterpolationType interp = prev.interpolation != null ? prev.interpolation : next.interpolation;
        if (interp == null) {
            interp = InterpolationType.LINEAR;
        }

        float[] prevValue = prev.sample(t);
        float[] nextValue = next.sample(t);

        switch (interp) {
            case LINEAR:
                return MathUtil.lerp(prevValue, nextValue, factor);
            case CATMULL_ROM: {
                int pidx = Math.max(idx - 1, 0);
                int nidx = Math.min(idx + 2, n - 1);
                float[] p0 = keyframes.get(pidx).sample(t);
                float[] p1 = prevValue;
                float[] p2 = nextValue;
                float[] p3 = keyframes.get(nidx).sample(t);
                return MathUtil.catmullRom(p0, p1, p2, p3, factor);
            }
            case SPHERICAL_LINEAR:
                return MathUtil.slerp(prevValue, nextValue, factor);
            case SPHERICAL_SQUAD: {
                float[] prevPre = prev.pre != null ? prev.pre : prevValue;
                float[] nextPost = next.post != null ? next.post : nextValue;
                return MathUtil.squad(prevPre, prevValue, nextValue, nextPost, factor);
            }
        }

        return null;
    }

    private void triggerEvents(float start, float end) {
        if (animation == null) return;
        for (Map.Entry<Float, String> entry : animation.getSoundEffects().entrySet()) {
            checkAndTrigger(entry.getKey(), entry.getValue(), start, end);
        }
    }

    private void checkAndTrigger(float eventTime, String id, float start, float end) {
        int key = eventKey(eventTime);
        if (triggeredEventKeys.contains(key)) return;

        boolean hit = checkEventHit(eventTime, start, end);

        if (hit) {
            playSound(id);
            triggeredEventKeys.add(key);
        }
    }

    private boolean checkEventHit(float eventTime, float start, float end) {
        if (animation != null && animation.getType().equals(AnimationPlayType.LOOP) && end < start) {
            return eventTime + EPS >= start || eventTime <= end + EPS;
        } else {
            return eventTime + EPS >= start && eventTime <= end + EPS;
        }
    }

    private int eventKey(float t) {
        return Math.round(t * (1f / EPS));
    }

    public Animation getAnimation() {
        return animation;
    }

    public String getName() {
        return animation != null ? animation.getName() : "<unnamed>";
    }

    // ==================== 事件回调（可重写） ====================
    private void playSound(String soundName) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (soundName.isEmpty()) {
            return;
        }
        String[] split = soundName.split(":");
        if (split.length != 2) {
            return;
        }
        String name = split[0].replace("tacz", "zero");
        if (name.equalsIgnoreCase("zero")) {
            ResourceLocation resourceLocation = ZeroResources.getSoundResource(split[1]);
            if (resourceLocation != null) {
                FMLClientHandler.instance().getClient().getSoundHandler().playSound(new GunSound(player, 16, resourceLocation, 10F, 1F));
            }
        } else {
            ResourceLocation location = new ResourceLocation(name, split[1]);
            SoundEvent soundEvent = new SoundEvent(location);
            PositionedSoundRecord soundRecord = new PositionedSoundRecord(soundEvent, SoundCategory.PLAYERS, 1f, 1f, player.getPosition());
            FMLClientHandler.instance().getClient().getSoundHandler().playSound(soundRecord);
        }
    }
}