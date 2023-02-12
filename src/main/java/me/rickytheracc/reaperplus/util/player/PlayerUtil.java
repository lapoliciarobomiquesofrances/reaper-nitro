package me.rickytheracc.reaperplus.util.player;

import me.rickytheracc.reaperplus.mixininterface.IBlink;
import me.rickytheracc.reaperplus.mixininterface.IVec3d;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Blink;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class PlayerUtil {
    private static final Vec3d vec3d = new Vec3d(0,0,0);
    private static final Blink blink = Modules.get().get(Blink.class);

    public static Box properHitbox() {
        if (!blink.isActive()) return mc.player.getBoundingBox();
        return ((IBlink) blink).getHitbox();
    }

    public static Vec3d properEyePos() {
        if (!blink.isActive()) return mc.player.getEyePos();
        return ((IBlink) blink).getEyePos();
    }

    public static Vec3d properFeetPos() {
        if (!blink.isActive()) return mc.player.getPos();
        return ((IBlink) blink).getFeetPos();
    }

    public static Vec3d smartVelocity(PlayerEntity player) {
        double dX = player.getX() - player.prevX;
        double dY = player.getY() - player.prevY;
        double dZ = player.getZ() - player.prevZ;

        ((IVec3d) vec3d).set(dX, dY ,dZ);
        return vec3d;
    }

    public static boolean isMoving(PlayerEntity player) {
        Vec3d vec = smartVelocity(player);
        return vec.x != 0 || vec.y != 0 || vec.z != 0;
    }

    public static PlayerListEntry getEntry(PlayerEntity player) {
        return mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
    }

    public static PlayerListEntry getEntry() {
        return getEntry(mc.player);
    }
}
