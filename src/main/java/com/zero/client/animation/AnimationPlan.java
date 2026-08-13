package com.zero.client.animation;

public class AnimationPlan {
    public final String name;
    public final AnimationPlayType type;
    public final float transitionTimeS;

    public AnimationPlan(String name,AnimationPlayType type, float transitionTimeS) {
        this.name = name;
        this.type = type;
        this.transitionTimeS = transitionTimeS;
    }
}
