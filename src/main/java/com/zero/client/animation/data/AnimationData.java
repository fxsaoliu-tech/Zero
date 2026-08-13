package com.zero.client.animation.data;

import com.zero.Zero;
import com.zero.client.animation.json.Animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AnimationData 存储物品的基础动画数据。
 * animations：物品提供的动画
 * replace：默认动画，用于补全缺失动画
 */
public class AnimationData {
    // 存储的动画数据
    protected final Map<String, Animation> animations = new HashMap<>();

    /**
     * 构造函数
     */
    public AnimationData(Map<String, Animation> animations, Map<String, Animation> replace) {
        this.animations.putAll(animations);

        this.replaceDefaultAnimations(replace, AnimationStateText.STATIC_IDLE);
        this.replaceDefaultAnimations(replace, AnimationStateText.IDLE_BREATHE);

        this.replaceDefaultAnimations(replace, AnimationStateText.DRAW);
        this.replaceDefaultAnimations(replace, AnimationStateText.PUT_AWAY);

        this.replaceDefaultAnimations(replace, AnimationStateText.WALK_FORWARD);
        this.replaceDefaultAnimations(replace, AnimationStateText.WALK_BACKWARD);
        this.replaceDefaultAnimations(replace, AnimationStateText.WALK_SIDE);

        this.replaceDefaultAnimations(replace, AnimationStateText.SPRINT_IN);
        this.replaceDefaultAnimations(replace, AnimationStateText.SPRINT_LOOP);
        this.replaceDefaultAnimations(replace, AnimationStateText.SPRINT_LOCK);
        this.replaceDefaultAnimations(replace, AnimationStateText.SPRINT_OUT);

        this.replaceDefaultAnimations(replace, AnimationStateText.INSPECT);
    }

    //替换默认配置
    protected void replaceDefaultAnimations(Map<String, Animation> replace, String key) {
        if (!animations.containsKey(key)) {
            if (replace.containsKey(key)) {
                animations.put(key, replace.get(key));
            }
        }
    }

    public List<String> getAnimationSounds() {
        List<String> sounds = new ArrayList<>();
        for (Animation animation : animations.values()) {
            for (String sound : animation.getSoundEffects().values()) {
                sounds.add(sound.replace("tacz", Zero.MOD_ID));
            }
        }
        return sounds;
    }

    public Animation getAnimation(String key) {
        if (animations.containsKey(key)) {
            return animations.get(key);
        }
        return null;
    }
}