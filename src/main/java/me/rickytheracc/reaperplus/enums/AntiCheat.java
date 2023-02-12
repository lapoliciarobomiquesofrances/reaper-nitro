package me.rickytheracc.reaperplus.enums;

import net.minecraft.util.math.Vec3d;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public enum AntiCheat {
    VANILLA,
    NOCHEAT;

    public Vec3d origin() {
        if (this == NOCHEAT) return mc.player.getEyePos();
        return mc.player.getPos();
    }
}
