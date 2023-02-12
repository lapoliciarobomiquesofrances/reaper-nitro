package me.rickytheracc.reaperplus.modules.misc;

import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.util.combat.Statistics;
import me.rickytheracc.reaperplus.util.player.StringUtil;
import me.rickytheracc.reaperplus.util.misc.ReaperModule;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AutoRespawn extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<Boolean> autoRekit = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-rekit")
        .description("Rekit after dying on pvp servers.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> kitName = sgGeneral.add(new StringSetting.Builder()
        .name("kit-name")
        .description("The name of your kit.")
        .defaultValue("")
        .visible(autoRekit::get)
        .build()
    );

    private final Setting<Boolean> excuse = sgGeneral.add(new BoolSetting.Builder()
        .name("excuse")
        .description("Send an excuse to chat after dying.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<String>> messages = sgGeneral.add(new StringListSetting.Builder()
        .name("excuse-messages")
        .description("Messages to use for AutoExcuse")
        .defaultValue(Collections.emptyList())
        .visible(excuse::get)
        .build()
    );

    private final Setting<Boolean> packet = sgGeneral.add(new BoolSetting.Builder()
        .name("packet")
        .description("Send the chat messages using packets.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> alertHS = sgGeneral.add(new BoolSetting.Builder()
        .name("alert")
        .description("Alerts you client side when you reach a new highscore.")
        .defaultValue(false)
        .build()
    );

    public AutoRespawn() {
        super(ReaperPlus.M, "auto-respawn", "Automatically respawns after death.");
    }

    Random random = new Random();

    @EventHandler
    private void onOpenScreenEvent(OpenScreenEvent event) {
        if (!(event.screen instanceof DeathScreen)) return;

        mc.player.requestRespawn();

        if (autoRekit.get()) StringUtil.sendNormalMessage("/kit " + kitName.get(), packet.get());
        if (excuse.get()) StringUtil.sendNormalMessage(getExcuseMessage(), packet.get());
        if (alertHS.get()) info("Your highscore is " + Statistics.getHighScore() + "!");
    }

    private String getExcuseMessage() {
        if (messages.get().isEmpty()) {
            error("Your excuse list is empty!");
            return "Lag";
        } else return messages.get().get(random.nextInt(messages.get().size()));
    }
}

