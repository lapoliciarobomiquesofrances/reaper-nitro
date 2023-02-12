package me.rickytheracc.reaperplus.modules.chat;

import me.rickytheracc.reaperplus.ReaperPlus;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

public class NotifSettings extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    public final Setting<Boolean> info = sgGeneral.add(new BoolSetting.Builder()
        .name("info")
        .description("show info messages as notifications")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> warning = sgGeneral.add(new BoolSetting.Builder()
        .name("warning")
        .description("show warning messages as notifications")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> error = sgGeneral.add(new BoolSetting.Builder()
        .name("error")
        .description("show error messages as notifications")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> hide = sgGeneral.add(new BoolSetting.Builder()
        .name("hide")
        .description("hide client-side messages")
        .defaultValue(false)
        .build()
    );

    public final Setting<Integer> displayTime = sgGeneral.add(new IntSetting.Builder()
        .name("display-time")
        .description("How long each notification displays for.")
        .defaultValue(2)
        .min(1)
        .build()
    );

    public NotifSettings() {
        super(ReaperPlus.C, "notifications", "Settings for hud notifications.");
    }
}
