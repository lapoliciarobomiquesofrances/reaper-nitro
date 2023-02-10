package me.rickytheracc.reapernitro.util.misc;

import me.rickytheracc.reapernitro.modules.chat.NotificationSettings;
import me.rickytheracc.reapernitro.util.services.NotificationManager;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.Text;

public class ReaperModule extends Module {
    public ReaperModule(Category category, String name, String description) {
        super(category, name, description);
        ns = Modules.get().get(NotificationSettings.class);
    }

    protected NotificationSettings ns;

    @Override
    public void info(Text message) {
        if (ns.info.get()) NotificationManager.addNotification(message.toString());
        if (!ns.hide.get()) super.info(message);
    }

    @Override
    public void warning(String message, Object... args) {
        if (ns.warning.get()) NotificationManager.addNotification(message);
        if (!ns.hide.get()) super.warning(message);
    }

    @Override
    public void error(String message, Object... args) {
        if (ns.error.get()) NotificationManager.addNotification(message);
        if (!ns.hide.get()) super.error(message);
    }
}
