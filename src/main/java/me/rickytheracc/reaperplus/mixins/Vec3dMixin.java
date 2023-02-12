package me.rickytheracc.reaperplus.mixins;

import me.rickytheracc.reaperplus.mixininterface.IVec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Vec3d.class)
public class Vec3dMixin implements IVec3d {
    @Mutable @Shadow @Final public double x;
    @Mutable @Shadow @Final public double y;
    @Mutable @Shadow @Final public double z;

    @Override
    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void set(@NotNull Vec3d vec) {
        set(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public void set(@NotNull Vec3i vec) {
        set(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public void set(@NotNull BlockPos pos) {
        set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    @Override
    public void set(@NotNull Vector3d vec) {
        set(vec.x, vec.y, vec.z);
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void setZ(double z) {
        this.z = z;
    }

}
