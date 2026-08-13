package com.zero.api.client.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RenderItemInHandBobEvent extends Event {

    public RenderItemInHandBobEvent() {

    }

    @Cancelable
    public static class BobHurt extends RenderItemInHandBobEvent {
        public BobHurt() {
        }
    }

    @Cancelable
    public static class BobView extends RenderItemInHandBobEvent {
        public BobView() {
        }
    }

}
