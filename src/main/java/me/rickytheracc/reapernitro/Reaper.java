package me.rickytheracc.reapernitro;

import me.rickytheracc.reapernitro.modules.ML;
import me.rickytheracc.reapernitro.util.services.SL;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.HudGroup;

import java.io.File;

public class Reaper extends MeteorAddon {
    public static String VERSION = "0.2.1";
    public static String INVITE_LINK = "https://discord.gg/RT5JFMZxvF";
    public static final File FOLDER = new File(MeteorClient.FOLDER, "Reaper");
    public static final File RECORDINGS = new File(FOLDER, "recordings");
    public static final File ASSETS = new File(FOLDER, "assets");
    public static final File USER_ASSETS = new File(ASSETS, "user");
    public static final HudGroup HUD_GROUP = new HudGroup("Reaper");

    @Override
    public void onInitialize() {
        log("Loading Reaper " + VERSION);
        ML.load(); // load modules
        SL.load(); // load services
        if (!FOLDER.exists()) FOLDER.mkdirs(); // make sure folders exists
        if (!RECORDINGS.exists()) RECORDINGS.mkdirs();
        if (!ASSETS.exists()) ASSETS.mkdirs();
        if (!USER_ASSETS.exists()) USER_ASSETS.mkdirs();
    }

    @Override
    public void onRegisterCategories() {
        ML.register();
    }

    @Override
    public String getWebsite() {
        return "https://github.com/RickyTheRacc/reaper-nitro";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("RickyTheRacc", "reaper-nitro");
    }

    public static void log(String m) {
        System.out.println("[Reaper Nitro] " + m);
    }

    public String getPackage() {
        return "me.ghosttypes.reaper";
    }
}
