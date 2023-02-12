package me.rickytheracc.reapernitro.modules.chat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.rickytheracc.reapernitro.Reaper;
import me.rickytheracc.reapernitro.events.PopEvent;
import me.rickytheracc.reapernitro.util.misc.ReaperModule;
import me.rickytheracc.reapernitro.util.misc.Formatter;
import me.rickytheracc.reapernitro.util.misc.MessageUtil;
import me.rickytheracc.reapernitro.util.player.Stats;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
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

    private final Setting<Integer> messageDelay = sgAnnounce.add(new IntSetting.Builder()
        .name("message-delay")
        .description("Minimum ticks between sending messages.")
        .defaultValue(100)
        .sliderMax(500)
        .min(0)
        .visible(() -> announce.get() && others.get())
        .build()
    );

    private final Setting<Double> announceRange = sgAnnounce.add(new DoubleSetting.Builder()
        .name("announce-range")
        .description("How close players need to be to announce pops or AutoEz.")
        .defaultValue(3)
        .min(0)
        .sliderMax(10)
        .visible(() -> announce.get() && others.get())
        .build()
    );

    private final Setting<List<String>> popMessages = sgAnnounce.add(new StringListSetting.Builder()
        .name("pop-messages")
        .description("Messages to use when announcing pops.")
        .defaultValue(Collections.emptyList())
        .visible(() -> announce.get() && others.get())
        .build()
    );

    public PopCounter() {
        super(Reaper.C, "pop-counter", "Count player's totem pops.");
    }

    private final Object2IntMap<UUID> chatIdMap = new Object2IntOpenHashMap<>();
    private final Random random = new Random();
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
    private void onPop(PopEvent event) {
        if (event.player == mc.player && !self.get()) return;
        else if (Friends.get().isFriend(event.player) && !friends.get()) return;
        else if (!others.get()) return;

        ChatUtils.sendMsg(
            getChatId(event.player), Formatting.GRAY,
            "(highlight)%s (default)popped (highlight)%d (default)%s.",
            event.name, event.pops, event.pops == 1 ? "totem" : "totems"
        );
        announceWait = messageDelay.get();
    }

    private int getChatId(Entity entity) {
        return chatIdMap.computeIfAbsent(entity.getUuid(), value -> random.nextInt());
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {
        if (!despawn.get()) return;

        if (event.entity instanceof PlayerEntity player) {
            String name = player.getEntityName();
            UUID u = player.getUuid();
            if (totemPops.containsKey(u)) {
                int pops = totemPops.getOrDefault(u, 0);
                info(n + " despawned after popping " + pops + getPopGrammar(pops) + ".");
            } else {
                info(n + " despawned.");
            }
        }
    }


    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet && packet.getStatus() == 35) { // pop tracking
            Entity e = packet.getEntity(mc.world);
            PlayerEntity pl = null;
            boolean isFriend = false;
            if (e == null) { // null check
                return;
            } else if (e instanceof PlayerEntity p) { // make sure it's a player
                pl = p;
                isFriend = Friends.get().isFriend(p); // self, friend, others check
                if (p.equals(mc.player) && !own.get()) return;
                if (isFriend && !friends.get()) return;
                if (!others.get() && !isFriend && !p.equals(mc.player)) return;
            }
            if (pl == null) return; // double-check 'cached' player
            synchronized (totemPops) { // update totem pop database
                int pops = totemPops.getOrDefault(e.getUuid(), 0);
                totemPops.put(e.getUuid(), ++pops);
                sendPopAlert(pl , pops, false);
            }
            if (announce.get() && mc.player.distanceTo(e) <= announceRange.get() && announceWait <= 0) { // handle announcing
                if (isFriend && !dontAnnounceFriends.get()) return;
                String popMessage = getPopMessage(pl);
                String name = pl.getEntityName();
                if (doPlaceholders.get()) popMessage = Formatter.applyPlaceholders(popMessage);
                //if (suffix.get()) popMessage = popMessage + Formatter.getSuffix();
//                MessageUtil.sendClientMessage(popMessage);
                if (pmOthers.get()) MessageUtil.sendDM(name, popMessage);
                announceWait = messageDelay.get() * 20;
            }
        }
    }




    private String getPopAlert(PlayerEntity p, int pops, boolean died) {
        String popAlert;
        if (died) popAlert = p.getEntityName() + " died after popping " + pops + getPopGrammar(pops);
        else popAlert = p.getEntityName() + " popped " + pops + getPopGrammar(pops);
        return popAlert;
    }

    private void sendPopAlert(PlayerEntity p, int pops, boolean died) {
        String popAlert = getPopAlert(p, pops, died);
        if (!popAlerts.get() && popAlert.contains("popped")) return;
        if (!deathAlerts.get() && popAlert.contains("died")) return;
        info(getPopAlert(p, pops, died));
    }

    private String getPopGrammar(int pops) {
        if (pops <= 1) return " totem";
        else return " totems";
    }

    private String getPopMessage(PlayerEntity p) {
        if (popMessages.get().isEmpty()) return "Ez pop {player}";
        String playerName = p.getEntityName();
        String popMessage = popMessages.get().get(new Random().nextInt(popMessages.get().size()));
        if (popMessage.contains("{pops}") && totemPops.containsKey(p.getUuid())) {
            int pops = totemPops.getOrDefault(p.getUuid(), 0);
            popMessage = popMessage.replace("{pops}", pops + " " + getPopGrammar(pops));
        } else {
            boolean f = false;
            for (String s : popMessages.get()) {
                if (!s.contains("{pops}")) {
                    f = true;
                    popMessage = s;
                    break;
                }
            }
            if (!f) popMessage = "Ez pop {player}";
        }
        if (popMessage.contains("{player}")) popMessage = popMessage.replace("{player}", playerName);
        return popMessage;
    }
}
