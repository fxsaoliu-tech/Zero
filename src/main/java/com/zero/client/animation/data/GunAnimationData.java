package com.zero.client.animation.data;

import com.zero.client.animation.json.Animation;

import java.util.Map;

public class GunAnimationData extends AnimationData {

    public GunAnimationData(Map<String, Animation> animations, Map<String, Animation> replace) {
        super(animations, replace);

        this.replaceDefaultAnimations(replace,AnimationStateText.INSPECT_EMPTY);
        this.replaceDefaultAnimations(replace,AnimationStateText.RELOAD_EMPTY);
        this.replaceDefaultAnimations(replace,AnimationStateText.RELOAD_TACTICAL);
        this.replaceDefaultAnimations(replace,AnimationStateText.SHOOT);
    }
}
