package me.rickytheracc.reaperplus.mixininterface;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public interface IBlink {
    Box getHitbox();
    Vec3d getEyePos();
    Vec3d getFeetPos();
}
