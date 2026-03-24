package com.codigohasta.addon.utils.player;

import com.codigohasta.addon.events.MovementInputEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import java.lang.reflect.Field;

public class MoveUtils {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static Field movementVectorField;

    static {
        try {
            // 初始化反射字段，规避 protected 访问限制
            movementVectorField = Input.class.getDeclaredField("movementVector");
            movementVectorField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static boolean isMoving() {
        if (mc.player == null) return false;
        PlayerInput pi = mc.player.input.playerInput;
        return pi.forward() || pi.backward() || pi.left() || pi.right();
    }

    public static void fixMovement(MovementInputEvent event, float yaw) {
        if (mc.player == null || event.input == null) return;

        // 使用公共方法获取当前的移动向量
        Vec2f currentInput = event.input.getMovementInput();
        float forward = currentInput.y;
        float sideways = currentInput.x;

        double angle = MathHelper.wrapDegrees(Math.toDegrees(getDirection(mc.player.getYaw(), forward, sideways)));
        if (forward == 0.0f && sideways == 0.0f) return;

        float closestForward = 0.0f;
        float closestSideways = 0.0f;
        float closestDifference = Float.MAX_VALUE;

        for (float pForward = -1.0f; pForward <= 1.0f; pForward += 1.0f) {
            for (float pSideways = -1.0f; pSideways <= 1.0f; pSideways += 1.0f) {
                if (pSideways == 0.0f && pForward == 0.0f) continue;
                double pAngle = MathHelper.wrapDegrees(Math.toDegrees(getDirection(yaw, pForward, pSideways)));
                double difference = Math.abs(MathHelper.wrapDegrees(angle - pAngle));
                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = pForward;
                    closestSideways = pSideways;
                }
            }
        }

        // 写入修改后的向量
        setMovementVector(event.input, new Vec2f(closestSideways, closestForward));

        // 更新 PlayerInput Record 以确保逻辑同步
        event.input.playerInput = new PlayerInput(
            closestForward > 0.0f,
            closestForward < 0.0f,
            closestSideways > 0.0f,
            closestSideways < 0.0f,
            event.input.playerInput.jump(),
            event.input.playerInput.sneak(),
            event.input.playerInput.sprint()
        );
    }

    // 反射写入辅助方法
    private static void setMovementVector(Input input, Vec2f vector) {
        try {
            if (movementVectorField != null) {
                movementVectorField.set(input, vector);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static double getDirection(float rotationYaw, float moveForward, float moveStrafing) {
        if (moveForward < 0.0f) rotationYaw += 180.0f;
        float forward = 1.0f;
        if (moveForward < 0.0f) forward = -0.5f;
        else if (moveForward > 0.0f) forward = 0.5f;
        if (moveStrafing > 0.0f) rotationYaw -= 90.0f * forward;
        if (moveStrafing < 0.0f) rotationYaw += 90.0f * forward;
        return Math.toRadians(rotationYaw);
    }
}