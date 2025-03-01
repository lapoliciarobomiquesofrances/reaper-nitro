package me.rickytheracc.reaperplus.mixin;


import me.rickytheracc.reaperplus.modules.chat.NotifSettings;
import me.rickytheracc.reaperplus.util.services.NotificationManager;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatUtils.class, remap = false)
public class ChatUtilsMixin {

    // This is for redirecting Meteor (and other addon's) modules info/warning/error methods to a notification
    // Reaper modules should extend ExtendedModule instead.

    @Inject(method = "info(Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void infoProxy(String message, Object[] args, CallbackInfo ci) {
        NotifSettings ns = Modules.get().get(NotifSettings.class);
        if (ns.info.get()) NotificationManager.addNotification(message);
        if (ns.hide.get()) ci.cancel();
    }

    @Inject(method = "warning(Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void warningProxy(String message, Object[] args, CallbackInfo ci) {
        NotifSettings ns = Modules.get().get(NotifSettings.class);
        if (ns.warning.get()) NotificationManager.addNotification(message);
        if (ns.hide.get()) ci.cancel();
    }

    @Inject(method = "error(Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void errorProxy(String message, Object[] args, CallbackInfo ci) {
        NotifSettings ns = Modules.get().get(NotifSettings.class);
        if (ns.error.get()) NotificationManager.addNotification(message);
        if (ns.hide.get()) ci.cancel();
    }
}
