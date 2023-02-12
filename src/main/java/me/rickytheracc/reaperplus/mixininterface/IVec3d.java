package me.rickytheracc.reaperplus.mixininterface;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

public interface IVec3d {
    void set(double x, double y, double z);
    void set(Vec3d vec);
    void set(Vec3i vec);
    void set(Vector3d vec);

    void setX(double x);
    void setY(double y);
    void setZ(double z);
}
