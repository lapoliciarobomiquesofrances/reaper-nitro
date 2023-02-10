package me.rickytheracc.reapernitro.util.services;

import me.rickytheracc.reapernitro.Reaper;
import me.rickytheracc.reapernitro.modules.misc.RPC;
import me.rickytheracc.reapernitro.util.misc.MathUtil;
import me.rickytheracc.reapernitro.util.misc.MessageUtil;
import me.rickytheracc.reapernitro.util.os.OSUtil;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class SL { // Service loader
    public static void load() {
        long start = MathUtil.now();
        OSUtil.init(); // setup current OS for stuff like spotify
        ResourceLoaderService.init(); // Download assets
        MeteorClient.EVENT_BUS.subscribe(GlobalManager.class);
        MessageUtil.init();
        NotificationManager.init();
        Runtime.getRuntime().addShutdownHook(new Thread(TL::shutdown));
        Reaper.log("Started services (" + MathUtil.msPassed(start) + "ms)");

    }

    public static void setupRPC() {
        TL.cached.execute(() -> {
            try { Thread.sleep(5000); } catch (Exception ignored) {}
            RPC rpc = Modules.get().get(RPC.class);
            if (rpc == null) return;
            if (!rpc.runInMainMenu) rpc.runInMainMenu = true;
            rpc.checkMeteorRPC();
            if (!rpc.isActive()) rpc.toggle();
        });
    }



}
