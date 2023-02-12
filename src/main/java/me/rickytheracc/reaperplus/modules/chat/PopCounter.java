package me.rickytheracc.reaperplus.modules.chat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.events.PopEvent;
import me.rickytheracc.reaperplus.util.combat.Statistics;
import me.rickytheracc.reaperplus.util.misc.MessageUtil;
import me.rickytheracc.reaperplus.util.misc.ReaperModule;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

import java.util.*;

public class PopCounter extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAnnounce = settings.createGroup("Announce");

    // General

    private final Setting<Boolean> despawn = sgGeneral.add(new BoolSetting.Builder()
        .name("despawn-alerts")
        .description("Alert you about players despawning.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> self = sgGeneral.add(new BoolSetting.Builder()
        .name("self")
        .description("Notifies you of your own totem pops.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> friends = sgGeneral.add(new BoolSetting.Builder()
        .name("friends")
        .description("Notifies you of your friends totem pops.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> others = sgGeneral.add(new BoolSetting.Builder()
        .name("others")
        .description("Notifies you of other players totem pops.")
        .defaultValue(true)
        .build()
    );

    // Announce

    private final Setting<Boolean> announce = sgAnnounce.add(new BoolSetting.Builder()
        .name("announce")
        .description("Announce when other players pop.")
        .defaultValue(false)
        .visible(others::get)
        .build()
    );

    private final Setting<Boolean> packet = sgAnnounce.add(new BoolSetting.Builder()
        .name("packet")
        .description("Send the messages with packets so only other players see them.")
        .defaultValue(false)
        .visible(() -> announce.get() && others.get())
        .build()
    );

    private final Setting<Boolean> privateMessage = sgAnnounce.add(new BoolSetting.Builder()
        .name("private")
        .description("Use /msg to dm the players the pop message.")
        .defaultValue(true)
        .visible(() -> announce.get() && others.get())
        .build()
    );

    private final Setting<Integer> messageDelay = sgAnnounce.add(new IntSetting.Builder()
        .name("message-delay")
        .description("Minimum ticks between sending messages.")
        .defaultValue(100)
        .sliderMax(500)
        .min(0)
        .visible(() -> announce.get() && others.get())
        .build()
    );

    private final Setting<Double> range = sgAnnounce.add(new DoubleSetting.Builder()
        .name("announce-range")
        .description("How close players need to be to announce pops.")
        .defaultValue(8)
        .min(0)
        .sliderMax(20)
        .visible(() -> announce.get() && others.get())
        .build()
    );

    private final Setting<List<String>> popMessages = sgAnnounce.add(new StringListSetting.Builder()
        .name("pop-messages")
        .description("Messages to use when announcing pops. Use {pops} and {player} to reference the target.")
        .defaultValue(Collections.emptyList())
        .visible(() -> announce.get() && others.get())
        .build()
    );

    public PopCounter() {
        super(ReaperPlus.C, "pop-counter", "Count player's totem pops.");
    }

    private final Object2IntMap<UUID> chatIdMap = new Object2IntOpenHashMap<>();
    private final Random random = new Random();
    private int announceWait;

    @Override
    public void onActivate() {
        chatIdMap.clear();
        announceWait = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (announceWait > 0) announceWait--;
    }

    @EventHandler
    private void onPop(PopEvent event) {
        if (event.player == mc.player && !self.get()) return;
        else if (Friends.get().isFriend(event.player) && !friends.get()) return;
        else if (!others.get()) return;
        else if (announce.get() && announceWait <= 0 && event.wasTarget) {
            if (mc.player.squaredDistanceTo(event.player) > range.get() * range.get()) return;

            String message;

            if (popMessages.get().isEmpty()) {
                message = "{player} popped {pops} totems to Reaper Plus!";
                warning("Your pop messages list is empty. Please add messages!");
            } else {
                int index = random.nextInt(0, popMessages.get().size() - 1);
                message = popMessages.get().get(index);
            }

            message = message.replace("{player}", event.name);
            message = message.replace("{pops}", String.valueOf(event.pops));

            if (!privateMessage.get()) MessageUtil.sendNormalMessage(message, packet.get());
            else MessageUtil.sendPrivateMessage(message, event.name, packet.get());
            announceWait = messageDelay.get();
        }

        ChatUtils.sendMsg(
            getChatId(event.player), Formatting.GRAY,
            "(highlight)%s (default)popped (highlight)%d (default)%s.",
            event.name, event.pops, event.pops == 1 ? "totem" : "totems"
        );
    }

    private int getChatId(Entity entity) {
        return chatIdMap.computeIfAbsent(entity.getUuid(), value -> random.nextInt());
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {
        if (!despawn.get()) return;

        if (event.entity instanceof PlayerEntity player) {
            int pops = Statistics.getPops(player);
            if (pops == 0) return;

            info(name + " despawned after popping " + pops + ((pops >= 1) ? "totems" : "totem") + ".");
        }
    }
}
