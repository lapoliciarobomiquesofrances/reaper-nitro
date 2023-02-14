package me.rickytheracc.reaperplus.mixin;


import me.rickytheracc.reaperplus.mixininterface.IBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Box.class)
public class BoxMixin implements IBox {
    @Shadow @Final @Mutable public double minX;
    @Shadow @Final @Mutable public double minY;
    @Shadow @Final @Mutable public double minZ;

    @Shadow @Final @Mutable public double maxX;
    @Shadow @Final @Mutable public double maxY;
    @Shadow @Final @Mutable public double maxZ;

    @Override
    public void expand(double v) {
        this.minX -= v;
        this.minY -= v;
        this.minZ -= v;
        this.maxX += v;
        this.maxY += v;
        this.maxZ += v;
    }

    @Override
    public void contract(double v) {
        expand(-v);
    }

    @Override
    public void set(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    @Override
    public void set(@NotNull BlockPos pos) {
        set(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    @Override
    public void set(@NotNull Box box) {
        set(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }
}
