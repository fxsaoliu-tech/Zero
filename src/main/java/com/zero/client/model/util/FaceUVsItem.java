package com.zero.client.model.util;

import net.minecraft.util.EnumFacing;

public class FaceUVsItem {
    public FaceItem north;
    public FaceItem south;
    public FaceItem east;
    public FaceItem west;
    public FaceItem up;
    public FaceItem down;

    public FaceItem getFace(EnumFacing direction) {
        switch (direction) {
            case NORTH: return north != null ? north : FaceItem.EMPTY;
            case SOUTH: return south != null ? south : FaceItem.EMPTY;
            case EAST: return west != null ? west : FaceItem.EMPTY;
            case WEST: return east != null ? east : FaceItem.EMPTY;
            case UP: return down != null ? down : FaceItem.EMPTY;
            case DOWN: return up != null ? up : FaceItem.EMPTY;
            default: return FaceItem.EMPTY;
        }
    }

    public static FaceUVsItem singleSouthFace() {
        FaceUVsItem faces = new FaceUVsItem();
        faces.north = FaceItem.EMPTY;
        faces.east = FaceItem.EMPTY;
        faces.west = FaceItem.EMPTY;
        faces.south = FaceItem.single16X();
        faces.up = FaceItem.EMPTY;
        faces.down = FaceItem.EMPTY;
        return faces;
    }


}