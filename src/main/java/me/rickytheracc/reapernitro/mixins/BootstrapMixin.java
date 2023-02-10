package me.rickytheracc.reapernitro.mixins;


import me.rickytheracc.reapernitro.Reaper;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class BootstrapMixin {
    @Inject(method = "main*", at = @At("HEAD"), remap = false)
    private static void doBootstrap(CallbackInfo ci) {
        Reaper.log("[Bootstrapper] Patching AWT properties");
        System.setProperty("java.awt.headless", "false");
    }
}
