package com.zero.client.model.display;

public enum Align {
    LEFT,
    CENTER,
    RIGHT;

    public static Align getAlignType(String name) {
        if (name.equalsIgnoreCase("LEFT")) {
            return LEFT;
        } else if (name.equalsIgnoreCase("CENTER")) {
            return CENTER;
        } else if (name.equalsIgnoreCase("RIGHT")) {
            return RIGHT;
        }
        return null;
    }
}