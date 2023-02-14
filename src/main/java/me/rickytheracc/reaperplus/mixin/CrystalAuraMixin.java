package me.rickytheracc.reaperplus.mixin;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import me.rickytheracc.reaperplus.enums.SwingMode;
import me.rickytheracc.reaperplus.mixininterface.ICrystalAura;
import me.rickytheracc.reaperplus.util.combat.Crystals;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CrystalAura.class, remap = false)
public abstract class CrystalAuraMixin extends Module implements ICrystalAura {
    @Shadow @Final private Setting<Boolean> antiWeakness;
    @Shadow @Final private Setting<Boolean> rotate;
    @Shadow @Final private Setting<Integer> attackFrequency;
    @Shadow @Final private Setting<Integer> breakRenderTime;
    @Shadow @Final private Setting<Integer> breakDelay;
    @Shadow @Final public Setting<CrystalAura.SwingMode> swingMode;

    @Shadow @Final private BlockPos.Mutable breakRenderPos;
    @Shadow private int switchTimer;
    @Shadow private int breakTimer;
    @Shadow private int breakRenderTimer;
    @Final @Shadow private IntSet removed;
    @Final @Shadow private Int2IntMap attemptedBreaks;
    @Final @Shadow private Int2IntMap waitingToExplode;

    @Shadow protected abstract boolean isValidWeaknessItem(ItemStack itemStack);
    @Shadow public abstract boolean doYawSteps(double targetYaw, double targetPitch);
    @Shadow protected abstract void setRotation(boolean isPos, Vec3d pos, double yaw, double pitch);

    public CrystalAuraMixin() {
        super(Categories.Combat, "crystal-aura", "Automatically places and attacks crystals.");
    }

    @Override
    public int getFrequency() {
        return attackFrequency.get();
    }

    /**
     * @author RickyTheRacc
     * @reason Add proper break util to breaking, this is just until I finish the CA
     */
    @Overwrite
    private void doBreak(Entity crystal) {
        // Anti weakness
        if (antiWeakness.get()) {
            StatusEffectInstance weakness = mc.player.getStatusEffect(StatusEffects.WEAKNESS);
            StatusEffectInstance strength = mc.player.getStatusEffect(StatusEffects.STRENGTH);

            // Check for strength
            if (weakness != null && (strength == null || strength.getAmplifier() <= weakness.getAmplifier())) {
                // Check if the item in your hand is already valid
                if (!isValidWeaknessItem(mc.player.getMainHandStack())) {
                    // Find valid item to break with
                    if (!InvUtils.swap(InvUtils.findInHotbar(this::isValidWeaknessItem).slot(), false)) return;

                    switchTimer = 1;
                    return;
                }
            }
        }

        // Rotate and attack

        // Trollface below
        boolean attacked = true;
        SwingMode mode = switch (swingMode.get()) {
            case Both -> SwingMode.Both;
            case Packet -> SwingMode.Packet;
            case Client -> SwingMode.Client;
            case None -> SwingMode.None;
        };

        if (rotate.get()) {
            double yaw = Rotations.getYaw(crystal);
            double pitch = Rotations.getPitch(crystal, Target.Feet);

            if (doYawSteps(yaw, pitch)) {
                setRotation(true, crystal.getPos(), 0, 0);
                Rotations.rotate(yaw, pitch, 50);

                attacked = Crystals.attackCrystal(crystal, mode);
                breakTimer = breakDelay.get();
            } else attacked = false;
        } else {
            attacked = Crystals.attackCrystal(crystal, mode);
            if (attacked) breakTimer = breakDelay.get();
        }

        if (attacked) {
            // Update state
            removed.add(crystal.getId());
            attemptedBreaks.put(crystal.getId(), attemptedBreaks.get(crystal.getId()) + 1);
            waitingToExplode.put(crystal.getId(), 0);

            // Break render
            breakRenderPos.set(crystal.getBlockPos().down());
            breakRenderTimer = breakRenderTime.get();
        }
    }
}
