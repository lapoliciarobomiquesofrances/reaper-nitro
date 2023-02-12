package me.rickytheracc.reapernitro.util.misc;


import me.rickytheracc.reapernitro.Reaper;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MessageUtil {
    private static ScheduledExecutorService messageThread;
    public static ArrayList<String> pendingEZ = new ArrayList<>();
    public static ArrayList<Message> queue = new ArrayList<>();
    public static ArrayList<Message> removalQueue = new ArrayList<>();

    @PostInit
    public static void init() {
        messageThread = Executors.newSingleThreadScheduledExecutor();
        messageThread.scheduleAtFixedRate(MessageUtil::update, 2500, 500, TimeUnit.MILLISECONDS);
    }

    public static void shutdown() {
        messageThread.shutdownNow();
    }

    // Queueing Messages


    public static boolean isServerMessage(GameMessageS2CPacket packet) {
        return true;
    }

    // Sending messages
    public static void sendDM(String name, String msg) {
        if (name == null || msg == null || mc.player == null) return;
        msg = Formatter.stripName(name, msg);
        ChatUtils.sendPlayerMsg("/msg " + name + " " + msg);
    }

    public static void sendDM(String player, String msg, boolean stripName) {
        if (stripName) msg = Formatter.stripName(player, msg);
        sendMessage("/msg " + player + " " + msg);
    }

    public static void sendMessage(String msg) {
        if (!Utils.canUpdate()) return;
        if (mc.player == null || msg == null) return;
        mc.player.sendMessage(Text.of(msg));
    }

    public static void sendEzMessage(String target, String ezMessage, long delay, boolean sendDM) {
        Reaper.cached.execute(() -> {
            pendingEZ.add(target); // "lock" the name so no duplicates can be sent
            try {Thread.sleep(delay);} catch (Exception ignored) {}
            ChatUtils.sendPlayerMsg(ezMessage);
            if (sendDM) sendDM(target, ezMessage, true);
            pendingEZ.remove(target); // "unlock" it after the message is sent
        });
    }

    public static void sendDelayedMessage(String msg, long delay) {
        Reaper.cached.execute(() -> {
            try {Thread.sleep(delay);} catch (Exception ignored) {}
            ChatUtils.sendPlayerMsg(msg);
        });
    }

    public static void sendDelayedDM(String target, String msg, long delay, boolean stripName) {
        Reaper.cached.execute(() -> {
            try {Thread.sleep(delay);} catch (Exception ignored) {}
            sendDM(target, msg, stripName);
        });
    }


    public static void update() {
        if (!queue.isEmpty()) {
            queue.forEach(MessageUtil::check);
            removalQueue.forEach(message -> queue.remove(message));
        }
    }

    public enum MessageType {
        Client,
        Packet
    }

    public static void add(String player, String msg, int delay, boolean sendToDm, MessageType type) {
        queue.add(new Message(player, msg, delay, sendToDm, type));
    }

    public static void check(Message message) {
        message.tick();
        if (message.ticksLeft <= 0) {
            removalQueue.add(message);
            send(message);
        }
    }

    public static void send(Message message) {
        switch (message.messageType) {
            case Client -> ChatUtils.sendPlayerMsg(message.message);
            case Packet -> sendMessage(message.message);
        }
        if (message.sendDM) sendToPlayer(message.playerName, message.message, message.messageType);
    }

    public static void sendToPlayer(String playerName, String m, MessageType type) {
        if (playerName == null || m == null || mc.player == null) return;
        switch (type) {
            case Client -> ChatUtils.sendPlayerMsg("/msg " + playerName + " " + m);
            case Packet -> sendMessage("/msg " + playerName + " " +  m);
        }
    }

    public static class Message {
        public final String message;
        public final String playerName;
        public final boolean sendDM;
        public int ticksLeft;
        public MessageType messageType;

        public Message(String player, String msg, int delay, boolean sendToDm, MessageType type) {
            playerName = player;
            message = msg;
            ticksLeft = MathUtil.intToTicks(delay);
            sendDM = sendToDm;
            messageType = type;
        }

        public void tick() {
            ticksLeft--;
        }
    }
}
