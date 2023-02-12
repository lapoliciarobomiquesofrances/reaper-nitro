package me.rickytheracc.reapernitro.modules.chat;

import me.rickytheracc.reapernitro.Reaper;
import me.rickytheracc.reapernitro.events.DeathEvent;
import me.rickytheracc.reapernitro.util.combat.Statistics;
import me.rickytheracc.reapernitro.util.misc.ReaperModule;
import me.rickytheracc.reapernitro.util.services.GlobalManager.DeathEntry;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static me.rickytheracc.reapernitro.util.services.GlobalManager.deathEntries;

public class AutoEZ extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    public final Setting<Integer> ezDelay = sgGeneral.add(new IntSetting.Builder()
        .name("message-delay")
        .description("How many seconds before sending a message.")
        .defaultValue(1)
        .min(1)
        .sliderMax(100)
        .build()
    );

//    public final Setting<MessageMode> messageMode = sgGeneral.add(new EnumSetting.Builder<MessageMode>()
//        .name("message-mode")
//        .description("How the message should be sent, either normally or in priv messages.")
//        .defaultValue(MessageMode.Message)
//        .build()
//    );

    public final Setting<Boolean> useKillstreak = sgGeneral.add(new BoolSetting.Builder()
        .name("killstreak")
        .description("Add your killstreak to the end of messages.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> useSuffix = sgGeneral.add(new BoolSetting.Builder()
        .name("use-suffix")
        .description("Add a suffix to the end of pop messages.")
        .defaultValue(false)
        .build()
    );

    public final Setting<String> suffix = sgGeneral.add(new StringSetting.Builder()
        .name("suffix")
        .description("What the suffix for Auto EZ should be.")
        .defaultValue(" | Nitro")
        .visible(useSuffix::get)
        .build()
    );

    private final Setting<List<String>> ezMessages = sgGeneral.add(new StringListSetting.Builder()
        .name("ez-messages")
        .description("Messages to use for AutoEz.")
        .defaultValue(
            "At least your totem knows what it's doing!",
            "Could you try harder? I'm getting bored...",
            "Reaper Nitro owns me and all :yawn:",
            "You have to be trying to be this bad right?",
            "At this point you should honestly just give up."
        )
        .build()
    );

    public AutoEZ() {
        super(Reaper.C, "auto-ez", "Send a message when you kill somebody.");
    }

    Random random = new Random();
    private int announceWait;

    @Override
    public void onActivate() {
        announceWait = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (announceWait > 0) announceWait--;
    }

    @EventHandler
    public void onDeath(DeathEvent event) {
        if (!event.wasTarget || announceWait > 0) return;

        String name = event.name;
        String message;

        if (ezMessages.get().isEmpty()) {
            message = "GG {player}!";
            info("Your message list is empty! Using default message.");
        } else {
            int index = random.nextInt(0, ezMessages.get().size() - 1);
            message = ezMessages.get().get(index);
        }

        message = message.replace("{player}", name);
        message = message.replace("{pops}", String.valueOf(event.pops));

        if (useKillstreak.get()) message += " Killstreak: " + Statistics.getStreak();
        if (useSuffix.get()) message += suffix.get(); // add suffix

        ChatUtils.sendPlayerMsg(message);
        announceWait = ezDelay.get();
    }
}
