package me.rickytheracc.reapernitro.util.combat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.rickytheracc.reapernitro.events.DeathEvent;
import me.rickytheracc.reapernitro.events.PlayerJoinEvent;
import me.rickytheracc.reapernitro.events.PlayerLeaveEvent;
import me.rickytheracc.reapernitro.events.PopEvent;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.value.Value;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Statistics {
    private static final Collection<PlayerListEntry> prevPlayerList = new ArrayList<>();
    public static final Object2IntMap<UUID> totemPops = new Object2IntOpenHashMap<>();
    public static final Object2IntMap<UUID> playerDeaths = new Object2IntOpenHashMap<>();
    public static ArrayList<String> killfeed = new ArrayList<>();

    public static int kills = 0;
    public static int deaths = 0;
    public static int killStreak = 0;
    public static int highscore = 0;
    private static long startTime;

    @PostInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(Statistics.class);
        startTime = System.currentTimeMillis();
    }

    @EventHandler
    public static void onGameJoin(GameJoinedEvent event) {
        kills = 0;
        deaths = 0;
        killStreak = 0;
        highscore = 0;

        totemPops.clear();
        playerDeaths.clear();

        if (!mc.isInSingleplayer()) {
            prevPlayerList.clear();
            prevPlayerList.addAll(mc.getNetworkHandler().getPlayerList());
        }
    }

    // Getting stats

    public static int getPops(PlayerEntity player) {
        return totemPops.getOrDefault(player.getUuid(), 0);
    }

    public static Value getTimeOnline() {
        return Value.string(DurationFormatUtils.formatDuration(
            System.currentTimeMillis() - startTime, "HH:mm:ss")
        );
    }

    public static Value getKills() {
        return Value.number(kills);
    }
    public static Value getDeaths() {
        return Value.number(deaths);
    }
    public static Value getStreak() {
        return Value.number(killStreak);
    }
    public static Value getHighSore() {
        return Value.number(highscore);
    }
    public static Value getKDR() {
        String kdr;

        if (deaths < 2) kdr = kills + ".00";
        else {
            double kd = (double) kills / deaths;
            kdr = String.format("%.2f", kd);
        }

        return Value.string(kdr);
    }

    public int getPlayerDeaths(PlayerEntity player) {
        return playerDeaths.getOrDefault(player.getUuid(), 0);
    }

    // Handling events

    @EventHandler
    public static void onTick(TickEvent.Post event) {
        if (!mc.isInSingleplayer() && mc.getNetworkHandler() != null) {
            Collection<PlayerListEntry> currentList1 = mc.getNetworkHandler().getPlayerList();
            currentList1.removeAll(prevPlayerList);
            currentList1.forEach(player -> MeteorClient.EVENT_BUS.post(PlayerJoinEvent.get(player)));

            Collection<PlayerListEntry> currentList2 = mc.getNetworkHandler().getPlayerList();
            prevPlayerList.removeAll(currentList2);
            prevPlayerList.forEach(player -> MeteorClient.EVENT_BUS.post(PlayerLeaveEvent.get(player)));

            prevPlayerList.clear();
            prevPlayerList.addAll(currentList2);
        }
    }

    @EventHandler
    public static void onPacketReceive(PacketEvent.Receive event) {
        if (!Utils.canUpdate()) return;

        if (event.packet instanceof EntityStatusS2CPacket packet && packet.getEntity(mc.world) instanceof PlayerEntity player) {
            // Deaths
            if (packet.getStatus() == 3) {
                boolean wasTarget = false;
                String name = player.getEntityName();
                for (Module module : Modules.get().getActive()) {
                    if (module.getInfoString() == null) continue;
                    if (module.getInfoString().contains(name)) {
                        wasTarget = true;
                        break;
                    }
                }

                if (player == mc.player) {
                    deaths++;
                    killStreak = 0;
                } else if (wasTarget) {
                    kills++;
                    killStreak++;
                    if (killStreak > highscore) highscore++;

                    killfeed.removeIf(killfeed -> killfeed.equals(name));
                    killfeed.add(name);
                    if (killfeed.size() > 10) killfeed.remove(0);
                }

                int deaths = playerDeaths.getOrDefault(player.getUuid(), 0) + 1;
                synchronized (playerDeaths) {totemPops.put(player.getUuid(), deaths);}

                MeteorClient.EVENT_BUS.post(DeathEvent.get(player, getPops(player), wasTarget));
                if (totemPops.containsKey(player.getUuid())) synchronized (totemPops) {
                    totemPops.removeInt(player.getUuid());
                }
            }

            // Pops
            if (packet.getStatus() == 35) {
                synchronized (totemPops) {
                    int pops = totemPops.getOrDefault(player.getUuid(), 0) + 1;
                    playerDeaths.put(player.getUuid(), pops);
                    MeteorClient.EVENT_BUS.post(PopEvent.get(player, pops));
                }
            }
        }

        if (event.packet instanceof DeathMessageS2CPacket packet) {
            Entity entity = mc.world.getEntityById(packet.getEntityId());
            if (entity instanceof PlayerEntity player) {
                int deaths = playerDeaths.getOrDefault(player.getUuid(), 0) + 1;
                synchronized (playerDeaths) {totemPops.put(player.getUuid(), deaths);}

                MeteorClient.EVENT_BUS.post(DeathEvent.get(player, getPops(player), false));
                if (totemPops.containsKey(player.getUuid())) synchronized (totemPops) {
                    totemPops.removeInt(player.getUuid());
                }
            }
        }
    }
}
