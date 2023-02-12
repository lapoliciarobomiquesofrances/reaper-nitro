package me.rickytheracc.reaperplus.modules.hud;

import me.rickytheracc.reaperplus.ReaperPlus;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;

public class TextPresets {
    public static final HudElementInfo<TextHud> INFO = new HudElementInfo<>(ReaperPlus.HUD_GROUP, "reaper-text", "Displays arbitrary text with Starscript.", TextPresets::create);

    static {
        addPreset("Kills", "Kills: #1{reaper.kills}");
        addPreset("Deaths", "Deaths: #1{reaper.deaths}");
        addPreset("KDR", "KDR: #1{reaper.kdr}");
        addPreset("Highscore", "Highscore: #1{reaper.highscore}");
        addPreset("Killstreak", "Killstreak: #1{reaper.killstreak}");
        addPreset("Playtime", "Playtime: #1{reaper.playtime}");
        addPreset("Crystals/s", "Crystals/s: #1{reaper.crystalsps}");
    }

    private static TextHud create() {
        return new TextHud(INFO);
    }

    private static void addPreset(String title, String text) {
        INFO.addPreset(title, textHud -> {
            if (text != null) textHud.text.set(text);
            textHud.updateDelay.set(0);
        });
    }
}
