package me.rickytheracc.reaperplus.util.misc;

import me.rickytheracc.reaperplus.modules.chat.NotifSettings;
import me.rickytheracc.reaperplus.util.services.NotificationManager;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.text.Text;

public class ReaperModule extends Module {
    public ReaperModule(Category category, String name, String description) {
        super(category, name, description);
    }

    @Override
    public void info(Text message) {
        NotifSettings ns = Modules.get().get(NotifSettings.class);
        if (ns.info.get()) NotificationManager.addNotification(message.toString());
        if (!ns.hide.get()) super.info(message);
    }

    @Override
    public void warning(String message, Object... args) {
        NotifSettings ns = Modules.get().get(NotifSettings.class);
        if (ns.warning.get()) NotificationManager.addNotification(message);
        if (!ns.hide.get()) super.warning(message);
    }

    @Override
    public void error(String message, Object... args) {
        NotifSettings ns = Modules.get().get(NotifSettings.class);
        if (ns.error.get()) NotificationManager.addNotification(message);
        if (!ns.hide.get()) super.error(message);
    }
}
