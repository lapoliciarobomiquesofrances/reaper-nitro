package me.rickytheracc.reaperplus.mixin;

import meteordevelopment.meteorclient.settings.StringListSetting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = StringListSetting.class, remap = false)
public abstract class StringListSettingMixin {
    /**
     * @author RickyTheRacc
     * @reason Remove the line between the last string and the add button
     */
    @Redirect(method = "fillTable", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    private static boolean funnymixin(List<String> instance) {
        return false;
    }
}
