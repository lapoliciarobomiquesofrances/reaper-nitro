package me.rickytheracc.reapernitro.modules.chat;

import me.rickytheracc.reapernitro.modules.ML;
import me.rickytheracc.reapernitro.util.misc.MessageUtil;
import me.rickytheracc.reapernitro.util.misc.ReaperModule;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import java.util.ArrayList;

public class AutoLogin extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> password = sgGeneral.add(new StringSetting.Builder()
        .name("password")
        .description("The password to log in with.")
        .defaultValue("password")
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay in ticks to wait before sending the login message")
        .defaultValue(20)
        .build()
    );

    public AutoLogin() {
        super(ML.M, "auto-login", "Automatically log into servers that use /login.");
    }

    private int messageDelay;
    private boolean shouldSend;

    @EventHandler
    private void onJoin(GameJoinedEvent event) {
        shouldSend = true;
        messageDelay = 40;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!shouldSend) return;
        if (messageDelay > 0) {
            messageDelay --;
            return;
        }

        ChatUtils.sendPlayerMsg("/login " + password.get());
        shouldSend = false;
    }
}
