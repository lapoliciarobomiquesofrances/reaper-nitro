package me.rickytheracc.reaperplus.util.misc;

import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.Text;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MessageUtil {
    public static void sendNormalMessage(String text, boolean packet) {
        if (packet) {
            if (!Utils.canUpdate()) return;
            mc.player.sendMessage(Text.of(text));
        } else ChatUtils.sendPlayerMsg(text);
    }

    public static void sendPrivateMessage(String text, String name, boolean packet) {
        text = "/msg " + name + " " + text;
        if (packet) {
            if (!Utils.canUpdate()) return;
            mc.player.sendMessage(Text.of(text));
        } else ChatUtils.sendPlayerMsg(text);
    }
}
