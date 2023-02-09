package me.rickytheracc.reapernitro.modules.chat;

import me.rickytheracc.reapernitro.events.DeathEvent;
import me.rickytheracc.reapernitro.modules.ML;
import me.rickytheracc.reapernitro.util.misc.Formatter;
import me.rickytheracc.reapernitro.util.misc.MessageUtil;
import me.rickytheracc.reapernitro.util.misc.ReaperModule;
import me.rickytheracc.reapernitro.util.player.Stats;
import me.rickytheracc.reapernitro.util.services.GlobalManager;
import me.rickytheracc.reapernitro.util.services.GlobalManager.DeathEntry;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

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

    public final Setting<MessageMode> messageMode = sgGeneral.add(new EnumSetting.Builder<MessageMode>()
        .name("message-mode")
        .description("How the message should be sent, either normally or in priv messages.")
        .defaultValue(MessageMode.Message)
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
            "You have to be trying to be this bad right?"
        )
        .build()
    );

    public AutoEZ() {
        super(ML.M, "auto-ez", "Send a message when you kill somebody.");
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (deathEntries.isEmpty()) return;
        ArrayList<DeathEntry> toRemove = new ArrayList<>();
        for (DeathEntry entry : deathEntries) {
            // Remove all entries that are older than 2.5 seconds
            if (entry.getTime() + 2500 < System.currentTimeMillis()) toRemove.add(entry);
        }
        deathEntries.removeIf(toRemove::contains);
    }

    @EventHandler
    public void onKill(DeathEvent.KillEvent event) {
        if (event.player == null) return;
        String name = target.getEntityName();
        if (MessageUtil.pendingEZ.contains(name)) return; // no duplicate messages
        Stats.addKill(name);
        String ezMessage = "GG {player}";
        DeathEntry entry = GlobalManager.getDeathEntry(name); // try to get the death entry
        if (entry != null) {
            int pops = entry.getPops();
            ezMessage = ezMessage.replace("{pops}", getFormattedPops(pops));
        } else { ezMessage = getFixedEZ(); } // if no entry, use a 'fixed' message
        ezMessage = ezMessage.replace("{player}", name); // add the player's name
        ezMessage = Formatter.applyPlaceholders(ezMessage); // apply client placeholders
        if (useKillstreak.get()) ezMessage += Formatter.getKillstreak(); // add killstreak
        if (useSuffix.get()) ezMessage += suffix.get(); // add suffix
        MessageUtil.sendEzMessage(name, ezMessage, ezDelay.get() * 1000, pmEz.get());
    }


    public String getFixedEZ() {
        for (String m : ezMessages.get()) if (!m.contains("{pops}")) return m;
        return "GG {player}";
    }

    public String getFormattedPops(int pops) { // ex. Player died after popping {pops}
        if (pops < 1) return "no totems";
        if (pops == 1) return "1 totem";
        return "totems";
    }

    public enum MessageMode {
        Normal(""),
        Whisper("/w "),
        Message("/msg ");

        MessageMode(String string) {

        }
    }

}
