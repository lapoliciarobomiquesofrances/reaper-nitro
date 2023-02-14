package me.rickytheracc.reaperplus.util.combat;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.events.DeathEvent;
import me.rickytheracc.reaperplus.events.PlayerJoinEvent;
import me.rickytheracc.reaperplus.events.PlayerLeaveEvent;
import me.rickytheracc.reaperplus.events.PopEvent;
import me.rickytheracc.reaperplus.modules.combat.*;
import me.rickytheracc.reaperplus.util.player.PlayerUtil;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.*;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.value.Value;
import net.minecraft.block.Block;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.awt.event.WindowFocusListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Statistics {
    private static final Collection<PlayerListEntry> prevPlayerList = new ArrayList<>();
    public static final Object2IntMap<UUID> totemPops = new Object2IntOpenHashMap<>();
    public static final Object2IntMap<UUID> playerDeaths = new Object2IntOpenHashMap<>();
    public static final HashMap<BlockPos, Long> pendingBlocks = new HashMap<>();

    public static List<Module> combatModules = new ArrayList<>(Arrays.asList(
        Modules.get().get(AnchorGod.class),
        Modules.get().get(BedGod.class),
        Modules.get().get(ReaperHoleFill.class),
        Modules.get().get(CrystalAura.class),
        Modules.get().get(BedAura.class),
        Modules.get().get(HoleFiller.class),
        Modules.get().get(KillAura.class),
        Modules.get().get(AutoAnvil.class),
        Modules.get().get(AnchorAura.class),
        Modules.get().get(AutoWeb.class)
    ));

    public static ArrayList<String> killfeed = new ArrayList<>();
    public static int kills = 0;
    public static int deaths = 0;
    public static int killStreak = 0;
    public static int highscore = 0;
    private static long startTime;

    private static int ticksPassed;
    public static int crystalsPerSec;
    public static int first;

    @PostInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(Statistics.class);
        ReaperPlus.scheduled.scheduleAtFixedRate(Statistics::removeExpiredPlacements, 0, 25, TimeUnit.MILLISECONDS);
        startTime = System.currentTimeMillis();
    }

    @EventHandler
    public static void onGameJoin(GameJoinedEvent event) {
        kills = 0;
        deaths = 0;
        killStreak = 0;
        highscore = 0;
        ticksPassed = 0;

        totemPops.clear();
        playerDeaths.clear();
        killfeed.clear();
        pendingBlocks.clear();

        if (!mc.isInSingleplayer()) {
            prevPlayerList.clear();
            prevPlayerList.addAll(mc.getNetworkHandler().getPlayerList());
        }
    }

    // Getting stats

    public static int getPops(PlayerEntity player) {
        return totemPops.getOrDefault(player.getUuid(), 0);
    }

    public static Value getPlaytime() {
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
    public static Value getHighScore() {
        return Value.number(highscore);
    }
    public static Value getKDR() {
        String kdr;

        if (deaths < 2) kdr = kills + ".00";
        else {
            double kd = (double) kills / deaths;
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            kdr =  df.format(kd);
        }

        return Value.string(kdr);
    }

    public static Value getCrystalsPs() {
        return Value.number(crystalsPerSec);
    }

    public int getPlayerDeaths(PlayerEntity player) {
        return playerDeaths.getOrDefault(player.getUuid(), 0);
    }

    // Handling events

    @EventHandler
    public static void onTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;

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

        if (ticksPassed < 21) ticksPassed++;
        else ticksPassed = 0;

        if (ticksPassed == 1) first = InvUtils.find(Items.END_CRYSTAL).count();

        if (ticksPassed == 21) {
            int second = InvUtils.find(Items.END_CRYSTAL).count();
            int difference = -(second - first);
            crystalsPerSec = Math.max(0, difference);
        }
    }

    @EventHandler
    public static void onPacketReceive(PacketEvent.Receive event) {
        if (!Utils.canUpdate()) return;

        if (event.packet instanceof EntityStatusS2CPacket packet && packet.getEntity(mc.world) instanceof PlayerEntity player) {
            // Deaths
            if (packet.getStatus() == 3) {
                boolean wasTarget = targetCheck(player);
                String name = player.getEntityName();

                if (player == mc.player) {
                    deaths++;
                    killStreak = 0;
                    killfeed.clear();
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
                    boolean wasTarget = targetCheck(player);
                    int pops = totemPops.getOrDefault(player.getUuid(), 0) + 1;
                    totemPops.put(player.getUuid(), pops);
                    MeteorClient.EVENT_BUS.post(PopEvent.get(player, pops, wasTarget));
                }
            }
        }

        if (event.packet instanceof DeathMessageS2CPacket packet) {
            Entity entity = mc.world.getEntityById(packet.getEntityId());
            if (entity instanceof PlayerEntity player) {
                // Prevent sending 2 death events for players in render distance (hopefully)
                if (mc.world.getPlayers().contains(player)) return;

                int deaths = playerDeaths.getOrDefault(player.getUuid(), 0) + 1;
                synchronized (playerDeaths) {totemPops.put(player.getUuid(), deaths);}

                MeteorClient.EVENT_BUS.post(DeathEvent.get(player, getPops(player), false));
                if (totemPops.containsKey(player.getUuid())) synchronized (totemPops) {
                    totemPops.removeInt(player.getUuid());
                }
            }
        }

        if (event.packet instanceof BlockUpdateS2CPacket packet) {
            if (pendingBlocks.isEmpty()) return;

            if (pendingBlocks.containsKey(packet.getPos())) {
                Block block = mc.world.getBlockState(packet.getPos()).getBlock();
                BlockSoundGroup group = block.getSoundGroup(block.getDefaultState());

                RenderSystem.recordRenderCall(() -> mc.world.playSound(
                    packet.getPos().getX(), packet.getPos().getY(),
                    packet.getPos().getZ(), group.getPlaceSound(),
                    SoundCategory.BLOCKS, group.getVolume(),
                    group.getPitch() * 0.8F, true
                ));
            }

            synchronized (pendingBlocks) {pendingBlocks.remove(packet.getPos());}
        }
    }

    public static boolean targetCheck(PlayerEntity player) {
        if (!mc.world.getPlayers().contains(player)) return false;
        if (mc.player.squaredDistanceTo(player) > 30 * 30) return false;

        String name = player.getEntityName();
        for (Module module : combatModules) {
            if (!module.isActive()) continue;
            if (module.getInfoString() == null) continue;
            if (module.getInfoString().contains(name)) return true;
        }

        return false;
    }

    public static void addPlacement(BlockPos pos) {
        synchronized (pendingBlocks) {
            pendingBlocks.putIfAbsent(pos, System.currentTimeMillis());
        }
    }

    public static void removeExpiredPlacements() {
        double latency = PlayerUtils.getPing() * 1.2;
        Iterator<Map.Entry<BlockPos, Long>> iterator = pendingBlocks.entrySet().iterator();
        while (iterator.hasNext()) {
            long passedTime = System.currentTimeMillis() - iterator.next().getValue();
            if (passedTime > latency) iterator.remove();
        }
    }
}
