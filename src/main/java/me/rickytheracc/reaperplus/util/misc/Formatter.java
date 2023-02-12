package me.rickytheracc.reaperplus.util.misc;

import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.util.combat.Statistics;
import me.rickytheracc.reaperplus.util.services.SpotifyService;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.SharedConstants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Calendar;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Formatter {

    public static String stripName(String playerName, String msg) {
        return msg.replace(playerName, "");
    }

    @Nullable
    public static java.awt.Color mcToC(Color c) { // MeteorColor -> java.awt.color
        if (c == null) return null;
        return new java.awt.Color(c.r, c.g, c.b, c.a);
    }

    @Nullable
    public static java.awt.Color sToC(SettingColor sc) { // SettingColor -> java.awt.color
        if (sc == null) return null;
        return new java.awt.Color(sc.r, sc.g, sc.b, sc.a);
    }



    /*public static final List<String> spotTitle = List.of("{song}", "{songtitle}");
    public static final List<String> spotTrack = List.of("{songname}", "{currentsong}");
    public static final List<String> disc = List.of("{discord}", "{invite}");*/

    public static String applyPlaceholders(String m) {
        // stats
        if (m.contains("{highscore}")) m = m.replace("{highscore}", String.valueOf(Statistics.getHighScore()));
        if (m.contains("{killstreak}")) m = m.replace("{killstreak}", String.valueOf(Statistics.getStreak()));
        if (m.contains("{kills}")) m = m.replace("{kills}", String.valueOf(Statistics.getKills()));
        if (m.contains("{deaths}")) m = m.replace("{deaths}", String.valueOf(Statistics.getDeaths()));

        // minecraft
        if (m.contains("{server}")) m = m.replace("{server}", Utils.getWorldName());
        if (m.contains("{version}")) m = m.replace("{version}", SharedConstants.getGameVersion().getName());
        if (m.contains("{rversion}")) m = m.replace("{rversion}", ReaperPlus.VERSION);
        if (m.contains("{random}")) m = m.replace("{random}", String.valueOf(MathUtil.random(1, 9)));
        if (m.contains("{username}")) m = m.replace("{username}", mc.getSession().getUsername());
        if (m.contains("{hp}")) m = m.replace("{hp}", String.valueOf(Math.rint(PlayerUtils.getTotalHealth())));

        // spotify
        if (SpotifyService.hasMedia()) {
            if (m.contains("{songtitle}")) m = m.replace("{songtitle}", SpotifyService.currentTrack);
            if (m.contains("{songname}")) m = m.replace("{songname}", SpotifyService.currentArtist);
        } else {
            if (m.contains("{songtitle}")) m = m.replace("{songtitle}", "Idle");
            if (m.contains("{songname}")) m = m.replace("{songname}", "Selecting a song");
        }

        return m;
    }

    /*public static String replaceAll(List<String> toReplace, String replacement, String str) {
        for (String s : toReplace) if (str.contains(s)) str = str.replace(s, replacement);
        return str;
    }*/

    public static String applyEmotes(String msg) {
        if (msg.contains(":smile:")) msg = msg.replace(":smile:", "☺");
        if (msg.contains(":sad:")) msg = msg.replace(":sad:", "☹");
        if (msg.contains(":heart:")) msg = msg.replace(":heart:", "❤");
        if (msg.contains(":skull:")) msg = msg.replace(":skull:", "☠");
        if (msg.contains(":star:")) msg = msg.replace(":star:", "★");
        if (msg.contains(":flower:")) msg = msg.replace(":flower:", "❀");
        if (msg.contains(":pick:")) msg = msg.replace(":pick:", "⛏");
        if (msg.contains(":wheelchair:")) msg = msg.replace(":wheelchair:", "♿");
        if (msg.contains(":lightning:")) msg = msg.replace(":lightning:", "⚡");
        if (msg.contains(":rod:")) msg = msg.replace(":rod:", "🎣");
        if (msg.contains(":potion:")) msg = msg.replace(":potion:", "🧪");
        if (msg.contains(":fire:")) msg = msg.replace(":fire:", "🔥");
        if (msg.contains(":shears:")) msg = msg.replace(":shears:", "✂");
        if (msg.contains(":bell:")) msg = msg.replace(":bell:", "🔔");
        if (msg.contains(":bow:")) msg = msg.replace(":bow:", "🏹");
        if (msg.contains(":trident:")) msg = msg.replace(":trident:", "🔱");
        if (msg.contains(":cloud:")) msg = msg.replace(":cloud:", "☁");
        if (msg.contains(":meteor:")) msg = msg.replace(":meteor:", "☄");
        if (msg.contains(":nuke:")) msg = msg.replace(":nuke:", "☢");
        return msg;
    }

    public static String getCurrentTrack() {
        String currentTrack = SpotifyService.currentTrack;
        if (currentTrack == null) return "Idle";
        if (currentTrack.isBlank() || currentTrack.isEmpty()) return "Idle";
        return currentTrack;
    }

    public static String getCurrentArtist() {
        String currentArtist = SpotifyService.currentArtist;
        if (currentArtist == null) return "No song playing";
        if (currentArtist.isBlank() || currentArtist.isEmpty()) return "No song playing";
        return currentArtist;
    }

    public static String getGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay < 12){
            return "Good morning, ";
        } else if(timeOfDay < 16){
            return "Good afternoon, ";
        } else if(timeOfDay < 21){
            return "Good evening, ";
        } else {
            return "Good night, ";
        }
    }

    // for Killfeed HUD module lol
    public static boolean hasKillFeed() { return Statistics.killfeed.isEmpty();}
    public static ArrayList<String> getKillFeed() { return Statistics.killfeed;}
}
