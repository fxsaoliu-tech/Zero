package com.zero.client.util;

import org.joml.Quaternionf;
import org.joml.Vector3f;

@FunctionalInterface
public interface Axis {
    Axis XN = (degree) -> {
        return (new Quaternionf()).rotationX(-degree);
    };
    Axis XP = (degree) -> {
        return (new Quaternionf()).rotationX(degree);
    };
    Axis YN = (degree) -> {
        return (new Quaternionf()).rotationY(-degree);
    };
    Axis YP = (degree) -> {
        return (new Quaternionf()).rotationY(degree);
    };
    Axis ZN = (degree) -> {
        return (new Quaternionf()).rotationZ(-degree);
    };
    Axis ZP = (degree) -> {
        return (new Quaternionf()).rotationZ(degree);
    };

    static Axis of(Vector3f pAxis) {
        return (degree) -> {
            return (new Quaternionf()).rotationAxis(degree, pAxis);
        };
    }

    Quaternionf rotation(float pRadians);

    default Quaternionf rotationDegrees(float pDegrees) {
        return this.rotation(pDegrees * ((float)Math.PI / 180F));
    }
}