package com.zero.client.gui.gun.button;

import java.util.List;
import java.util.function.Consumer;

public interface IComponentTooltip {
    /**
     * 添加此接口，会调用此渲染文本提示
     *  需要渲染的文本提示
     */
    List<String> getTooltip();
}
