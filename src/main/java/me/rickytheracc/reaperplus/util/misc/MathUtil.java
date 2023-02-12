package me.rickytheracc.reaperplus.util.misc;

import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.Random;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MathUtil {
    public static final Random RANDOM = new Random();

    public static int random(int min, int max) {
        return RANDOM.nextInt(min, max);
    }

    public static double random(double min, double max) {
        return RANDOM.nextDouble(min, max);
    }

    public static int secondsToTicks(int i) {
        return i * 20;
    }
    public static int ticksToSeconds(int i) {
        return i / 20;
    }

    public static long msPassed(Long start) {
        return System.currentTimeMillis() - start;
    }
    public static long secondsPassed(Long start) {
        return msToSeconds(msToSeconds(start));
    }

    public static String timeElapsed(Long start) {
        return DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH:mm:ss");
    }
    public static String hoursElapsed(Long start) {
        return DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "HH");
    }
    public static String minutesElapsed(Long start) {
        return DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "mm");
    }
    public static String secondsElapsed(Long start) {
        return DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "ss");
    }
    public static String millisElapsed(Long start) {
        return Math.round(MathUtil.msPassed(start) * 100.0) / 100.0 + "ms";
    }

    public static long secondsToMS(int seconds) {
        return seconds * 1000L;
    }

    public static long msToSeconds(long ms) {
        return ms / 1000L;
    }
    public static int msToTicks(long ms) {
        return secondsToTicks((int) msToSeconds(ms));
    }

    public static BlockPos offsetByVelocity(BlockPos pos, PlayerEntity player) {
        if (pos == null || player == null) return null;
        Vec3d velocity = player.getVelocity();
        return pos.add(velocity.x, velocity.y, velocity.z);
    }

    public static BlockPos generatePredict(BlockPos pos, PlayerEntity player, int ticks) {
        if (pos == null || player == null) return null;
        Vec3d velocity = player.getVelocity();
        return pos.add(velocity.x * ticks, velocity.y * ticks, velocity.z * ticks);
    }

    public static boolean intersects(Box box) {
        return EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator());
    }
    public static boolean intersects(BlockPos pos) {
        return intersects(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ()));
    }
    public static boolean intersectsAbove(BlockPos pos) {
        return intersects(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY() + 1, pos.getZ()));
    }

    public static double[] directionSpeed(float speed) {
        float forward = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        float yaw = mc.player.prevYaw + (mc.player.getYaw() - mc.player.prevYaw);

        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += ((forward > 0.0f) ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += ((forward > 0.0f) ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }

        final double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        final double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        final double posX = forward * speed * cos + side * speed * sin;
        final double posZ = forward * speed * sin - side * speed * cos;

        return new double[] {posX, posZ};
    }
}
