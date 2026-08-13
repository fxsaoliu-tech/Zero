package com.zero.server.data;

import com.zero.server.util.ZeroTimer;

public class GunData {
    //重载记时
    private ZeroTimer reload = new ZeroTimer();
    //射击记时
    private ZeroTimer fire = new ZeroTimer();
    //扳机
    private ZeroTimer trigger = new ZeroTimer();

    public GunData() {

    }

    public void onUpdate() {
        if (reload.isRunning()) {
            if (reload.isFinished()) {
                reload.reset();
            }
        }
        if (fire.isRunning()) {
            if (fire.isFinished()) {
                fire.reset();
            }
        }
        if (trigger.isRunning()) {
            if (trigger.isFinished()) {
                trigger.reset();
            }
        }
    }

    public void beginReload(long reloadDuration) {
        if (!reload.isRunning()) {
            reload.start(reloadDuration - 50);
        }
    }

    public void setShootInterval(long duration) {
        if (!fire.isRunning()) {
            fire.start(duration);
        }
    }

    public void setTrigger() {
        if (!trigger.isRunning()) {
            trigger.start((long) (0.25 * 1000L));
        }
    }

    public boolean isCanTrigger() {
        return !trigger.isRunning();
    }

    public boolean isCanShoot() {
        return !fire.isRunning();
    }

    public boolean canReload() {
        return !reload.isRunning();
    }

}
