package me.rickytheracc.reaperplus.modules.chat;

import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.events.DeathEvent;
import me.rickytheracc.reaperplus.util.combat.Statistics;
import me.rickytheracc.reaperplus.util.player.StringUtil;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.util.List;
import java.util.Random;

public class AutoEasy extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    public final Setting<Integer> ezDelay = sgGeneral.add(new IntSetting.Builder()
        .name("message-delay")
        .description("How many seconds before sending a message.")
        .defaultValue(100)
        .min(1)
        .sliderMax(200)
        .build()
    );

    private final Setting<Boolean> packet = sgGeneral.add(new BoolSetting.Builder()
        .name("packet")
        .description("Send the messages with packets so only other players see them.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> privateMessage = sgGeneral.add(new BoolSetting.Builder()
        .name("private")
        .description("Use /msg to dm the players the ez message.")
        .defaultValue(true)
        .build()
    );

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
        .defaultValue(" | Reaper")
        .visible(useSuffix::get)
        .build()
    );

    private final Setting<List<String>> ezMessages = sgGeneral.add(new StringListSetting.Builder()
        .name("ez-messages")
        .description("Messages to use for AutoEz.")
        .defaultValue(
            "At least your totem knows what it's doing!",
            "Could you try harder? I'm getting bored...",
            "Reaper Plus owns me and all :yawn:",
            "You have to be trying to be this bad right?",
            "At this point you should honestly just give up."
        )
        .build()
    );

    public AutoEasy() {
        super(ReaperPlus.C, "auto-easy", "Send a message when you kill somebody.");
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
            info("Your message list is empty. Please add messages!");
        } else {
            int index = random.nextInt(0, ezMessages.get().size() - 1);
            message = ezMessages.get().get(index);
        }

        message = message.replace("{player}", name);
        message = message.replace("{pops}", String.valueOf(event.pops));

        if (useKillstreak.get()) message += " Killstreak: " + Statistics.getStreak();
        if (useSuffix.get()) message += suffix.get(); // add suffix

        if (!privateMessage.get()) StringUtil.sendNormalMessage(message, packet.get());
        else StringUtil.sendPrivateMessage(message, event.name, packet.get());

        announceWait = ezDelay.get();
    }
}
