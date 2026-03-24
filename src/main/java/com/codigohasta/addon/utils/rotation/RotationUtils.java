package com.codigohasta.addon.utils.rotation;

import com.codigohasta.addon.utils.RotationManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.*;
import org.joml.Vector2f;

public class RotationUtils {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static Vector2f calculate(final Vec3d from, final Vec3d to) {
        final Vec3d diff = to.subtract(from);
        final double distance = Math.hypot(diff.x, diff.z);
        final float yaw = (float) (MathHelper.atan2(diff.z, diff.x) * (180.0 / Math.PI)) - 90.0F;
        final float pitch = (float) (-(MathHelper.atan2(diff.y, distance) * (180.0 / Math.PI)));
        return new Vector2f(yaw, pitch);
    }

    public static Vector2f getRotationsToEntity(LivingEntity entity) {
        if (mc.player == null) return new Vector2f(0, 0);
        Vec3d eyePos = mc.player.getEyePos();
        // 修复：直接使用 getX, getY, getZ 避免 getPos() 映射冲突
        Vec3d targetPos = new Vec3d(entity.getX(), entity.getY() + entity.getHeight() / 2.0, entity.getZ());
        
        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(-Math.atan2(dx, dz));
        float pitch = (float) Math.toDegrees(-Math.atan2(dy, dist));

        return new Vector2f(yaw, MathHelper.clamp(pitch, -90.0f, 90.0f));
    }

    public static double getEyeDistanceToEntity(LivingEntity entity) {
        if (mc.player == null) return 0.0;
        Vec3d eyePos = mc.player.getEyePos();
        Box box = entity.getBoundingBox();
        double dx = Math.max(box.minX - eyePos.x, Math.max(0.0, eyePos.x - box.maxX));
        double dy = Math.max(box.minY - eyePos.y, Math.max(0.0, eyePos.y - box.maxY));
        double dz = Math.max(box.minZ - eyePos.z, Math.max(0.0, eyePos.z - box.maxZ));
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static Vector2f calculate(final Entity entity) {
        if (mc.player == null) return new Vector2f(0, 0);
        return calculate(new Vec3d(entity.getX(), entity.getY(), entity.getZ()).add(0, Math.max(0, Math.min(mc.player.getY() - entity.getY() + mc.player.getEyeHeight(mc.player.getPose()), (entity.getBoundingBox().maxY - entity.getBoundingBox().minY) * 0.9)), 0));
    }

    public static Vector2f calculate(final Vec3d to) {
        if (mc.player == null) return new Vector2f(0, 0);
        return calculate(mc.player.getEyePos(), to);
    }

    public static Vector2f calculate(final BlockPos to) {
        if (mc.player == null) return new Vector2f(0, 0);
        return calculate(mc.player.getEyePos(), new Vec3d(to.getX() + 0.5, to.getY() + 0.5, to.getZ() + 0.5));
    }

    public static Vector2f applySensitivityPatch(final Vector2f rotation) {
    if (mc.player == null) return rotation;
    
    // 修复：1.21.1+build.4 映射中 prevYaw -> lastYaw, prevPitch -> lastPitch
    final Vector2f previousRotation = new Vector2f(mc.player.lastYaw, mc.player.lastPitch);
    
    return applySensitivityPatch(rotation, previousRotation);
}

    public static Vector2f applySensitivityPatch(final Vector2f rotation, final Vector2f previousRotation) {
        final float mouseSensitivity = (float) (mc.options.getMouseSensitivity().getValue() * 0.6F + 0.2F);
        final double multiplier = mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0F * 0.15D;
        final float yaw = previousRotation.x + (float) (Math.round((rotation.x - previousRotation.x) / multiplier) * multiplier);
        final float pitch = previousRotation.y + (float) (Math.round((rotation.y - previousRotation.y) / multiplier) * multiplier);
        return new Vector2f(yaw, MathHelper.clamp(pitch, -90.0f, 90.0f));
    }

    public static Vector2f resetRotation(final Vector2f rotation) {
        if (rotation == null || mc.player == null) return null;
        final float yaw = rotation.x + MathHelper.wrapDegrees(mc.player.getYaw() - rotation.x);
        final float pitch = mc.player.getPitch();
        return new Vector2f(yaw, pitch);
    }
    public static Vector2f smooth(final Vector2f targetRotation, final double speed) {
        return smooth(RotationManager.INSTANCE.lastRotations, targetRotation, speed);
    }

    public static Vector2f smooth(final Vector2f lastRotation, final Vector2f targetRotation, final double speed) {
        float yaw = targetRotation.x;
        float pitch = targetRotation.y;
        final float lastYaw = lastRotation.x;
        final float lastPitch = lastRotation.y;

        if (speed != 0.0) {
            Vector2f move = getMove(lastRotation, targetRotation, speed);

            yaw = lastYaw + move.x;
            pitch = lastPitch + move.y;

            float motion = Math.abs(move.x) + Math.abs(move.y);
            int iterations = motion < 0.02f ? 1 : (motion < 0.2f ? 2 : (motion < 2.0f ? 3 : 4));

            for (int i = 0; i < iterations; i++) {
                if (motion > 0.0001f) {
                    // 模拟微小的抖动
                    yaw += (float) ((Math.random() - 0.5) * 0.0012);
                    pitch += (float) ((Math.random() - 0.5) * 0.007);
                }

                final Vector2f rotations = new Vector2f(yaw, pitch);
                final Vector2f fixedRotations = applySensitivityPatch(rotations);

                yaw = lastYaw + MathHelper.wrapDegrees(fixedRotations.x - lastYaw);
                pitch = MathHelper.clamp(fixedRotations.y, -90.0f, 90.0f);
            }
        }

        return new Vector2f(yaw, pitch);
    }

    private static Vector2f getMove(final Vector2f lastRotation, final Vector2f targetRotation, double speed) {
        double deltaYaw = MathHelper.wrapDegrees(targetRotation.x - lastRotation.x);
        final double deltaPitch = (targetRotation.y - lastRotation.y);
        final double distance = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);
        
        if (distance < 1.0E-6) return new Vector2f(0, 0);

        final double distributionYaw = Math.abs(deltaYaw / distance);
        final double distributionPitch = Math.abs(deltaPitch / distance);
        final double maxYaw = speed * distributionYaw;
        final double maxPitch = speed * distributionPitch;

        final float moveYaw = (float) MathHelper.clamp(deltaYaw, -maxYaw, maxYaw);
        final float movePitch = (float) MathHelper.clamp(deltaPitch, -maxPitch, maxPitch);

        return new Vector2f(moveYaw, movePitch);
    }
}