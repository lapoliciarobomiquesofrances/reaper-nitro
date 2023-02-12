package me.rickytheracc.reaperplus.util.combat;

import me.rickytheracc.reaperplus.enums.AntiCheat;
import me.rickytheracc.reaperplus.mixininterface.IBox;
import me.rickytheracc.reaperplus.mixininterface.IVec3d;
import me.rickytheracc.reaperplus.util.player.PlayerUtil;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Ranges {
    private static final Box box = new Box(0, 0, 0, 0, 0, 0);
    private static Vec3d vec3d = new Vec3d(0, 0, 0);
    private static RaycastContext raycastContext;

    @EventHandler
    public static void onGameJoin(GameJoinedEvent event) {
        raycastContext = new RaycastContext(new Vec3d(0, 0, 0), new Vec3d(0, 0, 0),
            RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player);
    }

    public static boolean inHitRange(
        Entity entity, double range, double walls,
        AntiCheat antiCheat, boolean hitboxes
    ) {
        double squaredDistance = mc.player.squaredDistanceTo(entity);
        if (squaredDistance >= 36) return true;

        if (antiCheat == AntiCheat.Vanilla) {
            if (mc.player.canSee(entity)) return squaredDistance < range * range;
            else return squaredDistance < walls * walls;
        } else {
            if (PlayerUtils.getGameMode() == GameMode.CREATIVE) range = 6.0;
            Vec3d eyePos = PlayerUtil.properEyePos();
            ((IBox) box).set(entity.getBoundingBox());

            double y = MathHelper.clamp(eyePos.x, box.minY, box.maxY);
            double x;
            double z;

            if (hitboxes) {
                x = MathHelper.clamp(eyePos.x, box.minX, box.maxX);
                z = MathHelper.clamp(eyePos.z, box.minZ, box.maxZ);
            } else {
                x = entity.getX();
                z = entity.getZ();
            }

            ((IVec3d) vec3d).set(x, y, z);

            if (mc.player.canSee(entity)) return eyePos.squaredDistanceTo(vec3d) < range * range;
            else return eyePos.squaredDistanceTo(vec3d) < walls * walls;
        }
    }

    public static boolean inBlockRange(BlockPos pos, AntiCheat antiCheat, double range) {
        vec3d = antiCheat.origin();
        double dX = vec3d.x - (pos.getX() + 0.5);
        double dY = vec3d.y - (pos.getY() + 0.5);
        double dZ = vec3d.z - (pos.getZ() + 0.5);
        return dX * dX + dY * dY + dZ * dZ < range * range;
    }

}
