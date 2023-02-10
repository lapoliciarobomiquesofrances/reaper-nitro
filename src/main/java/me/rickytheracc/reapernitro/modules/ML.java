package me.rickytheracc.reapernitro.modules;

import me.rickytheracc.reapernitro.Reaper;
import me.rickytheracc.reapernitro.modules.chat.*;
import me.rickytheracc.reapernitro.modules.combat.*;
import me.rickytheracc.reapernitro.modules.misc.*;
import me.rickytheracc.reapernitro.modules.misc.elytrabot.ElytraBotThreaded;
import me.rickytheracc.reapernitro.modules.render.ExternalFeed;
import me.rickytheracc.reapernitro.modules.render.ExternalHUD;
import me.rickytheracc.reapernitro.modules.render.ExternalNotifications;
import me.rickytheracc.reapernitro.modules.render.ReaperHoleESP;
import me.rickytheracc.reapernitro.util.misc.MathUtil;
import me.rickytheracc.reapernitro.modules.hud.CustomImage;
import me.rickytheracc.reapernitro.modules.hud.Notifications;
import me.rickytheracc.reapernitro.modules.hud.SpotifyHud;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;

public class ML { // Module loader

    public static final Category R = new Category("Reaper Combat", Items.END_CRYSTAL.getDefaultStack());
    public static final Category C = new Category("Reaper Chat", Items.BOOK.getDefaultStack());
    public static final Category M = new Category("Reaper Misc.", Items.SKELETON_SKULL.getDefaultStack());
    public static final Category W = new Category("Windows", Items.GLASS_PANE.getDefaultStack());

    public static void register() {
        Reaper.log("Registering module categories.");

        Modules.registerCategory(R);
        Modules.registerCategory(C);
        Modules.registerCategory(M);
        Modules.registerCategory(W);
    }

    public static void load() {
        long start = MathUtil.now();
        Reaper.log("Loading modules...");

        loadCombat();
        loadChat();
        loadMisc();
        loadW();
        loadH();
        Reaper.log("Loaded Reaper Nitro in " + MathUtil.millisElapsed(start));
    }

    // Combat Moduels
    public static void loadCombat() {
        addModules(
            new AnchorGod(),
            new AntiSurround(),
            new BedGod(),
            new ReaperLongJump(),
            new QuickMend(),
            new ReaperSurround(),
            new SelfTrapPlus(),
            new SmartHoleFill()
        );
    }

    // Chat Modules
    public static void loadChat() {
        addModules(
            new ArmorAlert(),
            new AutoEasy(),
            new AutoLogin(),
            new BedAlerts(),
            new ChatTweaks(),
            new BreakAlert(),
            new NotifSettings(),
            new PopManager()
        );
    }

    // Misc Modules
    public static void loadMisc() {
        addModules(
          new AntiAim(),
          new AutoRespawn(),
          new ChorusPredict(),
          new ElytraBotThreaded(),
          new MultiTask(),
          new NoDesync(),
          new OneTap(),
          new PacketFly(),
          new RPC(),
          new StrictMove(),
          new WideScaffold()
        );

        // render
        addModules(
            new ExternalFeed(),
            new ExternalHUD(),
            new ExternalNotifications(),
            new ReaperHoleESP()
        );
    }

    public static void loadW() { // load modules in window category
        addModules(
            new ExternalHUD(),
            new ExternalNotifications()
        );
    }

    // Hud Modules
    public static void loadH() { // load hud modules
        Hud hud = Hud.get();
        hud.register(CustomImage.INFO);
        hud.register(Notifications.INFO);
        hud.register(SpotifyHud.INFO);
    }

    public static void addModules(Module... module) {
        for (Module module1 : module) {
            Modules.get().add(module1);
        }
    }
}
