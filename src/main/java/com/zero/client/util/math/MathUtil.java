package com.zero.client.util.math;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MathUtil {

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    //平滑插值
    public static float[] lerp(float[] a, float[] b, float t) {
        int len = Math.min(a.length, b.length);
        float[] out = new float[len];
        for (int i = 0; i < len; i++) {
            out[i] = a[i] + (b[i] - a[i]) * t;
        }
        return out;
    }

    public static float[] add(float[] a, float[] b) {
        int len = Math.min(a.length, b.length);
        float[] out = new float[len];
        for (int i = 0; i < len; i++) {
            out[i] = a[i] + b[i];
        }
        return out;
    }

    public static float[] max(float[] a, float[] b) {
        int len = Math.min(a.length, b.length);
        float[] out = new float[len];
        for (int i = 0; i < len; i++) {
            out[i] = Math.max(a[i], b[i]);
        }
        return out;
    }

    public static float[] multiply(float[] a, float[] b) {
        int len = Math.min(a.length, b.length);
        float[] out = new float[len];

        for (int i = 0; i < len; i++) {
            out[i] = a[i] * b[i];
        }

        return out;
    }

    /**
     * Catmull-Rom 插值
     *
     * @param p0 前一个点
     * @param p1 当前点
     * @param p2 下一个点
     * @param p3 下下个点
     * @param t  0~1
     * @return float[] 插值结果
     */
    public static float[] catmullRom(float[] p0, float[] p1, float[] p2, float[] p3, float t) {
        int len = Math.min(Math.min(p0.length, p1.length), Math.min(p2.length, p3.length));
        float t2 = t * t;
        float t3 = t2 * t;
        float[] out = new float[len];

        for (int i = 0; i < len; i++) {
            out[i] = 0.5f * (2f * p1[i] + (p2[i] - p0[i]) * t + (2f * p0[i] - 5f * p1[i] + 4f * p2[i] - p3[i]) * t2 + (-p0[i] + 3f * p1[i] - 3f * p2[i] + p3[i]) * t3);
        }
        return out;
    }

    /**
     * 将负旋转角(弧度)转换为等效的正角(角度)
     *
     * @param angle 弧度
     * @return 等效正角(角度)
     */
    public static double toDegreePositive(double angle) {
        while (angle < 0) {
            angle += Math.PI * 2;
        }
        return Math.toDegrees(angle);
    }

    /**
     * 弧度转换为四元数 顺序(ZYX)
     *
     * @param radian 弧度
     * @return 返回四元数
     */
    public static float[] toQuaternion(float[] radian) {
        if (radian == null) {
            return null;
        }
        float[] quaternion = new float[4];

        float roll = radian[2];
        float yaw = radian[1];
        float pitch = radian[0];

        float halfRoll = roll * 0.5f;
        float halfYaw = yaw * 0.5f;
        float halfPitch = pitch * 0.5f;

        float cr = (float) Math.cos(halfPitch);
        float sr = (float) Math.sin(halfPitch);
        float cp = (float) Math.cos(halfYaw);
        float sp = (float) Math.sin(halfYaw);
        float cy = (float) Math.cos(halfRoll);
        float sy = (float) Math.sin(halfRoll);

        quaternion[0] = cy * cp * sr - sy * sp * cr; // x
        quaternion[1] = sy * cp * sr + cy * sp * cr; // y
        quaternion[2] = sy * cp * cr - cy * sp * sr; // z
        quaternion[3] = cy * cp * cr + sy * sp * sr; // w
        return quaternion;
    }

    /**
     * 将四元数转换为欧拉角，
     * @param q 四元数
     * @return 按照 x(pitch) -> y(yaw) -> z(roll) 的顺序的三轴角数组。
     */
    public static float[] toEulerAngles(Quaternionf q) {
        float[] angles = new float[3];
        double sinrCosp = 2 * (q.w() * q.x() + q.y() * q.z());
        double cosrCosp = 1 - 2 * (q.x() * q.x() + q.y() * q.y());
        angles[0] = (float) Math.atan2(sinrCosp, cosrCosp);
        double sinp = 2 * (q.w() * q.y() - q.x() * q.z());
        if (Math.abs(sinp) >= 1) {
            angles[1] = (float) copySign(Math.PI / 2, sinp);
        } else {
            angles[1] = (float) Math.asin(sinp);
        }
        double sinyCosp = 2 * (q.w() * q.z() + q.y() * q.x());
        double cosyCosp = 1 - 2 * (q.y() * q.y() + q.z() * q.z());
        angles[2] = (float) Math.atan2(sinyCosp, cosyCosp);
        return angles;
    }

    public static double copySign(double magnitude, double sign) {
        return Math.abs(magnitude) * (sign < 0 ? -1 : 1);
    }

    /**
     * 将四元数转换为欧拉角，
     * @param q 四元数，前三个数是虚部，最后一个数是实部。
     * @return 按照 x(pitch) -> y(yaw) -> z(roll) 的顺序的三轴角数组。
     */
    public static float[] toEulerAngles(float[] q) {
        float[] angles = new float[3];
        double sinrCosp = 2 * (q[3] * q[0] + q[1] * q[2]);
        double cosrCosp = 1 - 2 * (q[0] * q[0] + q[1] * q[1]);
        angles[0] = (float) Math.atan2(sinrCosp, cosrCosp);
        double sinp = 2 * (q[3] * q[1] - q[2] * q[0]);
        if (Math.abs(sinp) >= 1) {
            angles[1] = (float) copySign(Math.PI / 2, sinp); // use 90 degrees if out of range
        } else {
            angles[1] = (float) Math.asin(sinp);
        }
        double sinyCosp = 2 * (q[3] * q[2] + q[1] * q[0]);
        double cosyCosp = 1 - 2 * (q[1] * q[1] + q[2] * q[2]);
        angles[2] = (float) Math.atan2(sinyCosp, cosyCosp);
        return angles;
    }

    /**
     * 两个四元数的球面线性插值（slerp）
     *
     * @param from  起始四元数 [x,y,z,w]
     * @param to    目标四元数 [x,y,z,w]
     * @param alpha 插值比例 0~1
     * @return 新四元数 [x,y,z,w]
     */
    public static float[] slerp(float[] from, float[] to, float alpha) {
        float ax = from[0];
        float ay = from[1];
        float az = from[2];
        float aw = from[3];
        float bx = to[0];
        float by = to[1];
        float bz = to[2];
        float bw = to[3];

        float dot = ax * bx + ay * by + az * bz + aw * bw;
        if (dot < 0) {
            bx = -bx;
            by = -by;
            bz = -bz;
            bw = -bw;
            dot = -dot;
        }
        float epsilon = 1e-6f;
        float s0, s1;
        if ((1.0 - dot) > epsilon) {
            float omega = (float) Math.acos(dot);
            float invSinOmega = 1.0f / (float) Math.sin(omega);
            s0 = (float) Math.sin((1.0 - alpha) * omega) * invSinOmega;
            s1 = (float) Math.sin(alpha * omega) * invSinOmega;
        } else {
            s0 = 1.0f - alpha;
            s1 = alpha;
        }
        float[] result = new float[4];
        result[0] = s0 * ax + s1 * bx;
        result[1] = s0 * ay + s1 * by;
        result[2] = s0 * az + s1 * bz;
        result[3] = s0 * aw + s1 * bw;
        return result;
    }

    public static Vector3f getEulerAngles(Matrix4f m) {
        Vector3f vector3f = new Vector3f();
        m.getEulerAnglesZYX(vector3f);
        return vector3f;
    }

    /**
     * 按照 z(roll) -> y(yaw) -> x(pitch) 的旋转顺序，求四元数。
     *
     * @param pitch      绕 x 轴旋转的弧度
     * @param yaw        绕 y 轴旋转的弧度
     * @param roll       绕 z 轴旋转的弧度
     * @param quaternion 求解的结果将写入这个四元数中。
     */
    public static Quaternionf toQuaternion(float pitch, float yaw, float roll,Quaternionf quaternion) {
        double cy = Math.cos(roll * 0.5);
        double sy = Math.sin(roll * 0.5);
        double cp = Math.cos(yaw * 0.5);
        double sp = Math.sin(yaw * 0.5);
        double cr = Math.cos(pitch * 0.5);
        double sr = Math.sin(pitch * 0.5);
        quaternion.set((float) (cy * cp * sr - sy * sp * cr), (float) (sy * cp * sr + cy * sp * cr), (float) (sy * cp * cr - cy * sp * sr), (float) (cy * cp * cr + sy * sp * sr));
        return quaternion;
    }

    /**
     * 按照 z(roll) -> y(yaw) -> x(pitch) 的旋转顺序，求四元数。
     * @param pitch 绕 x 轴旋转的弧度
     * @param yaw 绕 y 轴旋转的弧度
     * @param roll 绕 z 轴旋转的弧度
     * @return 四元数，前三个数是虚部，最后一个数是实部。
     */
    public static float[] toQuaternion(float pitch, float yaw, float roll) {
        double cy = Math.cos(roll * 0.5);
        double sy = Math.sin(roll * 0.5);
        double cp = Math.cos(yaw * 0.5);
        double sp = Math.sin(yaw * 0.5);
        double cr = Math.cos(pitch * 0.5);
        double sr = Math.sin(pitch * 0.5);
        return new float[]{(float) (cy * cp * sr - sy * sp * cr), (float) (sy * cp * sr + cy * sp * cr), (float) (sy * cp * cr - cy * sp * sr), (float) (cy * cp * cr + sy * sp * sr)};
    }

    public static float[] blendQuaternion(float[] to, float[] from) {
        Quaternionf q1 = new Quaternionf(to[0], to[1], to[2], to[3]);
        Quaternionf q2 = new Quaternionf(from[0], from[1], from[2], from[3]);
        normalizeQuaternion(q1);
        normalizeQuaternion(q2);
        logQuaternion(q1);
        logQuaternion(q2);
        q1.set(q1.x + q2.x, q1.y + q2.y, q1.z + q2.z, q1.w + q2.w);
        expQuaternion(q1);
        normalizeQuaternion(q1);
        return new float[] {q1.x, q1.y, q1.z, q1.w};
    }

    public static void normalizeQuaternion(Quaternionf q) {
        float f = q.x() * q.x() + q.y() * q.y() + q.z() * q.z() + q.w() * q.w();
        if (f > 0) {
            float f1 = MathUtil.fastInvSqrt(f);
            q.set(f1 * q.x(), f1 * q.y(), f1 * q.z(), f1 * q.w());
        } else {
            q.set(0, 0, 0, 1);
        }
    }

    public static float fastInvSqrt(float x) {
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f3759df - (i >> 1);
        x = Float.intBitsToFloat(i);
        x = x * (1.5f - xhalf * x * x);
        return x;
    }

    public static void logQuaternion(Quaternionf q) {
        double norm = Math.sqrt(q.x() * q.x() + q.y() * q.y() + q.z() * q.z() + q.w() * q.w());
        double vec = Math.sqrt(q.x() * q.x() + q.y() * q.y() + q.z() * q.z());
        double i = q.w() / norm;
        if (i > 1) {
            i = 1;
        }
        if (i < -1) {
            i = -1;
        }
        double theta = Math.acos(i);
        double factor = vec == 0 ? 0 : theta / vec;
        q.set((float) (q.x() * factor), (float) (q.y() * factor), (float) (q.z() * factor), (float) Math.log(norm));
    }

    public static void expQuaternion(Quaternionf q) {
        double magnitude = Math.sqrt(q.x() * q.x() + q.y() * q.y() + q.z() * q.z());
        double expW = Math.exp(q.w());
        double sinMagnitude = Math.sin(magnitude);
        double coef = magnitude == 0 ? 0 : expW * sinMagnitude / magnitude;
        q.set(
                (float) (coef * q.x()),
                (float) (coef * q.y()),
                (float) (coef * q.z()),
                (float) (expW * Math.cos(magnitude))
        );
    }

    /**
     * 缩放四元数表示的旋转角度（弧度），轴不变。
     * @param q         输入四元数（应为单位四元数，但方法内部会正确处理非单位）
     * @param multiplier 角度缩放系数（如 0.5 减半，2.0 加倍）
     * @return 缩放后的单位四元数
     */
    public static Quaternionf multiplyQuaternion(Quaternionf q, float multiplier) {
        double w = q.w();
        // 计算原始旋转角度（弧度）
        double angle = 2 * Math.acos(w);
        double halfAngle = angle / 2;
        double sinHalf = Math.sin(halfAngle);
        // 处理零旋转（角度为0或2π）
        if (Math.abs(sinHalf) < 1e-8) {
            // 无旋转，直接返回单位四元数（或原始 q，但通常需要保证单位）
            return new Quaternionf(0, 0, 0, 1);
        }
        // 提取单位旋转轴
        double invSinHalf = 1.0 / sinHalf;
        double axisX = q.x() * invSinHalf;
        double axisY = q.y() * invSinHalf;
        double axisZ = q.z() * invSinHalf;

        // 新旋转角度
        double newAngle = angle * multiplier;
        double newHalfAngle = newAngle / 2;
        double newSinHalf = Math.sin(newHalfAngle);
        double newCosHalf = Math.cos(newHalfAngle);

        // 构造新四元数（自动单位化，但浮点误差可能导致不严格单位，可选归一化）
        float newX = (float) (axisX * newSinHalf);
        float newY = (float) (axisY * newSinHalf);
        float newZ = (float) (axisZ * newSinHalf);
        float newW = (float) newCosHalf;
        return new Quaternionf(newX, newY, newZ, newW);
    }

    public static float[] multiplyQuaternion(float[] q, float multiplier) {
        double w = q[3];
        double angle = 2 * Math.acos(w);
        double halfAngle = angle / 2;
        double sinHalf = Math.sin(halfAngle);
        if (Math.abs(sinHalf) < 1e-8) {
            return new float[]{0, 0, 0, 1};
        }
        double invSinHalf = 1.0 / sinHalf;
        double axisX = q[0] * invSinHalf;
        double axisY = q[1] * invSinHalf;
        double axisZ = q[2] * invSinHalf;

        double newAngle = angle * multiplier;
        double newHalfAngle = newAngle / 2;
        double newSinHalf = Math.sin(newHalfAngle);
        double newCosHalf = Math.cos(newHalfAngle);

        float newX = (float) (axisX * newSinHalf);
        float newY = (float) (axisY * newSinHalf);
        float newZ = (float) (axisZ * newSinHalf);
        float newW = (float) newCosHalf;
        return new float[]{newX, newY, newZ, newW};
    }

    public static float[] squad(float[] q0, float[] q1, float[] q2, float[] q3, float t) {
        float[] slerp1 = slerp(q1, q2, t);
        float[] slerp2 = slerp(q0, q3, t);
        float s = 2.0F * t * (1.0F - t);
        return slerp(slerp1, slerp2, s);
    }

    public static double easeOutCubic(double x) {
        return 1 - Math.pow(1 - x, 3);
    }

    public static void applyMatrixLerp(Matrix4f from, Matrix4f to, Matrix4f result, float alpha) {
        Vector3f fromPos = new Vector3f();
        Vector3f toPos = new Vector3f();

        from.getTranslation(fromPos);
        to.getTranslation(toPos);

        Vector3f pos = new Vector3f(fromPos.x + (toPos.x - fromPos.x) * alpha, fromPos.y + (toPos.y - fromPos.y) * alpha, fromPos.z + (toPos.z - fromPos.z) * alpha);

        Quaternionf fromRot = new Quaternionf();
        Quaternionf toRot = new Quaternionf();

        from.getUnnormalizedRotation(fromRot);
        to.getUnnormalizedRotation(toRot);

        Quaternionf rot = new Quaternionf();
        fromRot.slerp(toRot, alpha, rot);
        // 重建矩阵
        result.identity().translate(pos).rotate(rot);
    }
}
