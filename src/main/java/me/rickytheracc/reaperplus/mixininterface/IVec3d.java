package me.rickytheracc.reaperplus.mixininterface;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public interface IVec3d {
    void set(double x, double y, double z);
    void setX(double x);
    void setY(double y);
    void setZ(double z);

    default void set(@NotNull Position pos) {
        set(pos.getX(), pos.getY(), pos.getZ());
    }
    default void set(@NotNull Vec3i vec) {
        set(vec.getX(), vec.getY(), vec.getZ());
    }
    default void set(@NotNull Vector3d vec) {
        set(vec.x, vec.y, vec.z);
    }
    default void set(@NotNull BlockPos pos) {
        set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }
}
