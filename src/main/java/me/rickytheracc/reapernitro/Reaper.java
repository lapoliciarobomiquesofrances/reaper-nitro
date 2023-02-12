package me.rickytheracc.reapernitro;

import me.rickytheracc.reapernitro.modules.chat.*;
import me.rickytheracc.reapernitro.modules.combat.*;
import me.rickytheracc.reapernitro.modules.hud.CustomImage;
import me.rickytheracc.reapernitro.modules.hud.Notifications;
import me.rickytheracc.reapernitro.modules.hud.SpotifyHud;
import me.rickytheracc.reapernitro.modules.misc.*;
import me.rickytheracc.reapernitro.modules.misc.elytrabot.ElytraBotThreaded;
import me.rickytheracc.reapernitro.modules.external.ExternalFeed;
import me.rickytheracc.reapernitro.modules.external.ExternalHUD;
import me.rickytheracc.reapernitro.modules.external.ExternalNotifs;
import me.rickytheracc.reapernitro.util.combat.Statistics;
import me.rickytheracc.reapernitro.util.misc.MathUtil;
import me.rickytheracc.reapernitro.util.misc.MessageUtil;
import me.rickytheracc.reapernitro.util.services.GlobalManager;
import me.rickytheracc.reapernitro.util.services.NotificationManager;
import me.rickytheracc.reapernitro.util.services.ResourceLoaderService;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.value.ValueMap;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Reaper extends MeteorAddon {
    public static final Category R = new Category("Reaper Combat");
    public static final Category C = new Category("Reaper Chat");
    public static final Category M = new Category("Reaper Misc.");
    public static final Category W = new Category("Windows");
    public static final HudGroup HUD_GROUP = new HudGroup("Reaper");
    public static String VERSION = "0.2.1";

    public static ExecutorService cached;
    public static ScheduledExecutorService scheduled;
    public static ExecutorService modules;

    public static final File FOLDER = new File(MeteorClient.FOLDER, "Reaper");
    public static final File RECORDINGS = new File(FOLDER, "recordings");
    public static final File ASSETS = new File(FOLDER, "assets");
    public static final File USER_ASSETS = new File(ASSETS, "user");


    @Override
    public void onInitialize() {
        long start = MathUtil.now();
        log("Loading Reaper " + VERSION);

        // Make sure directories exist
        log("Creating folders...");
        if (!FOLDER.exists()) FOLDER.mkdirs();
        if (!RECORDINGS.exists()) RECORDINGS.mkdirs();
        if (!ASSETS.exists()) ASSETS.mkdirs();
        if (!USER_ASSETS.exists()) USER_ASSETS.mkdirs();

        // Init services
        log("Creating threads...");
        cached = Executors.newCachedThreadPool();
        scheduled = Executors.newScheduledThreadPool(10);
        modules = Executors.newFixedThreadPool(10);
        MeteorClient.EVENT_BUS.subscribe(GlobalManager.class);
        ResourceLoaderService.init();
        MessageUtil.init();
        NotificationManager.init();

        // Adding StarScript
        log("Adding StarScript values...");
        MeteorStarscript.ss.set("reaper", new ValueMap()
            .set("kills", Statistics::getKills)
            .set("deaths", Statistics::getDeaths)
            .set("kdr", Statistics::getKDR)
            .set("killstreak", Statistics::getStreak)
            .set("highscore", Statistics::getHighSore)
            .set("timeplayed", Statistics::getTimeOnline)
        );

        // Load Modules
        log("Adding modules...");
        Modules modules = Modules.get();

        modules.add(new AnchorGod());
        modules.add(new AntiSurround());
        modules.add(new BedGod());
        modules.add(new ReaperLongJump());
        modules.add(new QuickMend());
        modules.add(new ReaperSurround());
        modules.add(new SelfTrapPlus());
        modules.add(new SmartHoleFill());

        modules.add(new ArmorAlert());
        modules.add(new AutoEZ());
        modules.add(new AutoLogin());
        modules.add(new BedAlerts());
        modules.add(new ChatTweaks());
        modules.add(new BreakAlert());
        modules.add(new NotifSettings());
        modules.add(new PopCounter());

        modules.add(new AutoRespawn());
        modules.add(new ChorusPredict());
        modules.add(new ElytraBotThreaded());
        modules.add(new MultiTask());
        modules.add(new NoDesync());
        modules.add(new OneTap());
        modules.add(new PacketFly());
        modules.add(new RPC());
        modules.add(new StrictMove());
        modules.add(new WideScaffold());

        modules.add(new ExternalFeed());
        modules.add(new ExternalHUD());
        modules.add(new ExternalNotifs());

        Hud hud = Hud.get();
        hud.register(CustomImage.INFO);
        hud.register(Notifications.INFO);
        hud.register(SpotifyHud.INFO);

        log("Reaper loaded in " + (System.currentTimeMillis() - start) + "ms!");

//        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
//            Reaper.modules.shutdownNow();
//            cached.shutdownNow();
//            scheduled.shutdownNow();
//        }));

    }

    @Override
    public void onRegisterCategories() {
        Reaper.log("Registering module categories.");
        Modules.registerCategory(R);
        Modules.registerCategory(C);
        Modules.registerCategory(M);
        Modules.registerCategory(W);
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
        return "me.rickytheracc.reapernitro";
    }
}
