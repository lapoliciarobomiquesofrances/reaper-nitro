package me.rickytheracc.reaperplus.modules.external;

import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.util.combat.Statistics;
import me.rickytheracc.reaperplus.util.misc.Formatter;
import me.rickytheracc.reaperplus.util.render.ExternalRenderers;
import me.rickytheracc.reaperplus.util.services.SpotifyService;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ExternalHUD extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCoords = settings.createGroup("Coordinates");
    private final SettingGroup sgModules = settings.createGroup("Modules");
    private final SettingGroup sgPlayers = settings.createGroup("Players");
    private final SettingGroup sgSpotify = settings.createGroup("Spotify");

    // General

    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("debug")
        .description("Send debug information about the module.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> chroma = sgGeneral.add(new BoolSetting.Builder()
        .name("chroma")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> chromaSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("chroma-speed")
        .defaultValue(0.01)
        .sliderMax(1)
        .build()
    );

    private final Setting<Integer> width = sgGeneral.add(new IntSetting.Builder()
        .name("width")
        .defaultValue(25)
        .min(10)
        .sliderMax(50)
        .build()
    );

    private final Setting<Integer> height = sgGeneral.add(new IntSetting.Builder()
        .name("height")
        .defaultValue(30)
        .min(20)
        .sliderMax(50)
        .build()
    );

    public final Setting<SettingColor> backColor = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .defaultValue(new SettingColor(15, 255, 211))
        .build()
    );

    private final Setting<Boolean> watermark = sgGeneral.add(new BoolSetting.Builder().name("watermark").defaultValue(false).build());
    private final Setting<Boolean> fps = sgGeneral.add(new BoolSetting.Builder().name("fps").defaultValue(false).build());
    private final Setting<Boolean> tps = sgGeneral.add(new BoolSetting.Builder().name("tps").defaultValue(false).build());
    private final Setting<Boolean> bps = sgGeneral.add(new BoolSetting.Builder().name("bps").defaultValue(false).build());
    private final Setting<Boolean> ping = sgGeneral.add(new BoolSetting.Builder().name("ping").defaultValue(false).build());
    private final Setting<Boolean> biome = sgGeneral.add(new BoolSetting.Builder().name("biome").defaultValue(false).build());
    private final Setting<Boolean> coords = sgCoords.add(new BoolSetting.Builder().name("coordinates").defaultValue(false).build());

    private final Setting<Boolean> kills = sgCoords.add(new BoolSetting.Builder().name("kills").defaultValue(false).build());
    private final Setting<Boolean> deaths = sgCoords.add(new BoolSetting.Builder().name("deaths").defaultValue(false).build());
    private final Setting<Boolean> kd = sgCoords.add(new BoolSetting.Builder().name("kd-ratio").defaultValue(false).build());
    private final Setting<Boolean> killstreak = sgCoords.add(new BoolSetting.Builder().name("killstreak").defaultValue(false).build());
    private final Setting<Boolean> highscore = sgCoords.add(new BoolSetting.Builder().name("highscore").defaultValue(false).build());

    private final Setting<Boolean> modules = sgModules.add(new BoolSetting.Builder().name("modules").defaultValue(false).build());
    private final Setting<SortMode> moduleSortMode = sgModules.add(new EnumSetting.Builder<SortMode>().name("module-sort-mode").defaultValue(SortMode.Shortest).build());

    private final Setting<Boolean> playerList = sgPlayers.add(new BoolSetting.Builder().name("player-list").defaultValue(false).build());
    private final Setting<Boolean> playerListShowDistance = sgPlayers.add(new BoolSetting.Builder().name("show-distance").defaultValue(false).build());
    private final Setting<SortMode> playerListSortMode = sgPlayers.add(new EnumSetting.Builder<SortMode>().name("player-sort-mode").defaultValue(SortMode.Shortest).build());

    private final Setting<Boolean> showSpotify = sgSpotify.add(new BoolSetting.Builder().name("spotify").defaultValue(false).build());
    private final Setting<Boolean> spotifySingleLine = sgSpotify.add(new BoolSetting.Builder().name("single-line").defaultValue(false).build());

    private ExternalRenderers.ExternalFrame externalFrame;
    private RainbowColor rc = new RainbowColor();

    public ExternalHUD() {
        super(ReaperPlus.W, "external-hud", "render a simple hud outside the client");
    }

    @Override
    public void onActivate() {
        ExternalRenderers.activeFrames++;
        EventQueue.invokeLater(() -> { // external frames must *always* be started this way
            if (externalFrame == null) {
                if (debug.get()) info("Creating new external frame");
                externalFrame = new ExternalRenderers.ExternalFrame(
                    width.get(), height.get(), "Reaper " + ReaperPlus.VERSION, this
                );
            }
            if (debug.get()) info("making frame visible");
            externalFrame.setVisible(true);
            externalFrame.setBackColor(Formatter.sToC(backColor.get()));
        });
        rc.setSpeed(chromaSpeed.get());
    }

    @Override
    public void onDeactivate() {
        ExternalRenderers.activeFrames--;
        if (externalFrame != null) externalFrame.setVisible(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (externalFrame != null) {
            if (chroma.get()) {
                rc = rc.getNext();
                externalFrame.setTextColor(new Color(rc.r, rc.g, rc.b, rc.a));
            }
            Color c = Formatter.sToC(backColor.get());
            if (externalFrame.getBackColor() != c) externalFrame.setBackColor(c);
            setData();
        }
    }

    private void setData() {
        if (externalFrame == null) return;
        ArrayList<String> info = new ArrayList<>();
        if (watermark.get()) {
            info.add("Reaper " + ReaperPlus.VERSION);
            info.add("");
        }
        if (fps.get()) info.add("FPS: " + MinecraftClientAccessor.getFps());
        if (tps.get()) info.add("TPS: " + String.format("%.1f", TickRate.INSTANCE.getTickRate()));
        if (bps.get()) info.add("BPS: " + String.format("%.1f", Utils.getPlayerSpeed()));
        if (ping.get()) info.add("Ping: " + getPing());
        if (biome.get()) info.add("Biome: " + getBiome());
        if (kills.get()) info.add("Kills: " + Statistics.getKills());
        if (deaths.get()) info.add("Deaths: " + Statistics.getDeaths());
        if (kd.get()) info.add("KD: " + Statistics.getKDR());
        if (killstreak.get()) info.add("Killstreak: " + Statistics.getStreak());
        if (highscore.get()) info.add("Highscore: " + Statistics.getHighScore());
        info.add("");
        if (coords.get()) info.addAll(getCoords());
        if (showSpotify.get()) info.addAll(getSpotify());
        if (modules.get()) info.addAll(getModules());
        if (playerList.get()) info.addAll(getPlayers());
        if (debug.get()) info("setting data, size:" + info.size());
        externalFrame.setText(info);
    }


    private String getPing() {
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (playerListEntry != null) return Integer.toString(playerListEntry.getLatency());
        return "0";
    }

    private String getBiome() {
        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        blockPos.set(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        Identifier id = mc.world.getRegistryManager().get(RegistryKeys.BIOME).getId(mc.world.getBiome(blockPos).value());
        if (id == null) return "Unknown";
        return Arrays.stream(id.getPath().split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    private ArrayList<String> getCoords() {
        Freecam freecam = Modules.get().get(Freecam.class);
        double x, y, z;
        x = freecam.isActive() ? mc.gameRenderer.getCamera().getPos().x : mc.player.getX();
        y = freecam.isActive() ? mc.gameRenderer.getCamera().getPos().y - mc.player.getEyeHeight(mc.player.getPose()) : mc.player.getY();
        z = freecam.isActive() ? mc.gameRenderer.getCamera().getPos().z : mc.player.getZ();
        String right1 = String.format("%.1f %.1f %.1f", x, y, z);
        String right2 = null;
        String dimension = null;
        switch (PlayerUtils.getDimension()) {
            case Overworld -> {
                right2 = String.format("%.1f %.1f %.1f", x / 8.0, y, z / 8.0);
                dimension = "Nether Coords: ";
            }
            case Nether -> {
                right2 = String.format("%.1f %.1f %.1f", x * 8.0, y, z * 8.0);
                dimension = "Overworld Coords: ";
            }
        }
        ArrayList<String> s = new ArrayList<>();
        s.add("");
        s.add(right1);
        if (right2 != null) s.add(dimension + right2);
        return s;
    }

    private ArrayList<String> getSpotify() {
        ArrayList<String> s = new ArrayList<>();
        if (!SpotifyService.isSpotifyRunning) return new ArrayList<>(List.of("No song is playing."));
        String artist = SpotifyService.currentArtist;
        String track = SpotifyService.currentTrack;
        if (artist == null || track == null) return new ArrayList<>(List.of("Idle."));
        s.add("");
        s.add("[Media]");
        if (spotifySingleLine.get()) {
            s.add("Playing " + track + " by " + artist);
        } else {
            s.add("Playing " + track);
            s.add("By " + artist);
        }
        return s;
    }

    private ArrayList<String> getModules() {
        ArrayList<String> moduleList = new ArrayList<>();
        ArrayList<String> ml = new ArrayList<>();
        Modules.get().getList().forEach(module -> {
            if (module.isActive()) ml.add(module.title);
        });
        moduleList.add("");
        moduleList.add("[Modules]");
        moduleList.addAll(sort(ml, moduleSortMode.get()));
        return moduleList;
    }

    private ArrayList<String> getPlayers() {
        ArrayList<String> playerList = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            String playerName;
            if (entity instanceof PlayerEntity player) {
                if (!player.equals(mc.player)) {
                    if (Friends.get().isFriend(player)) playerName = "[F] " + player.getEntityName();
                    else playerName = player.getEntityName();
                    if (playerName != null) {
                        if (playerListShowDistance.get()) playerName = playerName + String.format("(%sm)", Math.round(mc.getCameraEntity().distanceTo(entity)));
                        playerList.add(playerName);
                    }
                }
            }
        }
        if (playerList.isEmpty()) {
            return new ArrayList<>(List.of("[Players]", "None"));
        }
        else {
            ArrayList<String> l = new ArrayList<>();
            l.add("");
            l.add("[Players]");
            l.addAll(sort(playerList, playerListSortMode.get()));
            return l;
        }
    }

    public static ArrayList<String> sort(ArrayList<String> list, SortMode sortMode) {
        if (sortMode.equals(SortMode.Shortest)) {
            list.sort(Comparator.comparing(String::length));
        } else {
            list.sort(Comparator.comparing(String::length).reversed());
        }
        return list;
    }


    public enum SortMode {
        Longest,
        Shortest
    }
}
