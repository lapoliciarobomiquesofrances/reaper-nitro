package me.rickytheracc.reaperplus.mixins;

import me.rickytheracc.reaperplus.mixininterface.IBlink;
import me.rickytheracc.reaperplus.mixininterface.IBox;
import me.rickytheracc.reaperplus.mixininterface.IVec3d;
import meteordevelopment.meteorclient.systems.modules.movement.Blink;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(value = Blink.class, remap = false)
public abstract class BlinkMixin implements IBlink {
    private static final Box hitbox = new Box(0, 0, 0, 0, 0, 0);
    private static final Vec3d eyes = new Vec3d(0, 0, 0);
    private static final Vec3d feet = new Vec3d(0, 0, 0);

    @Inject(method = "onActivate", at = @At(value = "HEAD", remap = false))
    private void setOldPositions(CallbackInfo ci) {
        ((IBox) hitbox).set(mc.player.getBoundingBox());
        ((IVec3d) eyes).set(mc.player.getEyePos());
        ((IVec3d) feet).set(mc.player.getPos());
    }

    @Override
    public Box getHitbox() {
        return hitbox;
    }

    @Override
    public Vec3d getEyePos() {
        return eyes;
    }

    @Override
    public Vec3d getFeetPos() {
        return feet;
    }
}
