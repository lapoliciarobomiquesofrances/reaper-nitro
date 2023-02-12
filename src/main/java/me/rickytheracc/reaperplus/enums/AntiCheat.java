package me.rickytheracc.reaperplus.enums;

import me.rickytheracc.reaperplus.util.player.PlayerUtil;
import net.minecraft.util.math.Vec3d;

public enum AntiCheat {
    Vanilla,
    NoCheat;

    public Vec3d origin() {
        if (this == NoCheat) return PlayerUtil.properEyePos();
        return PlayerUtil.properFeetPos();
    }
}
