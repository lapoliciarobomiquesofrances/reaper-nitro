package me.rickytheracc.reaperplus.mixininterface;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public interface IBox {
    void expand(double v);
    void contract(double v);

    void set(double x1, double y1, double z1, double x2, double y2, double z2);
    void set(BlockPos pos);
    void set(Box box);
}
