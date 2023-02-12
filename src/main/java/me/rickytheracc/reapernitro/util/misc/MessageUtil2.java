package me.rickytheracc.reapernitro.util.misc;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MessageUtil2 {
    private static ScheduledExecutorService messageThread;
    public static ArrayList<Message> pendingMessages = new ArrayList<>();
    private static int timeUntilNextMessage;

    @PostInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(MessageUtil2.class);
        messageThread = Executors.newSingleThreadScheduledExecutor();
        messageThread.scheduleAtFixedRate(MessageUtil::update, 2500, 500, TimeUnit.MILLISECONDS);
    }

    public static void shutdown() {
        messageThread.shutdownNow();
    }

    // Handling queued messages

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        pendingMessages.clear();
        timeUntilNextMessage = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!Utils.canUpdate() || pendingMessages.isEmpty()) return;

        pendingMessages.sort(Comparator.comparingDouble(message -> message.delay));
        Iterator<Message> iterator = pendingMessages.iterator();

        while (iterator.hasNext()) {
            Message message = iterator.next();
            message.tick();
            if (message.delay <= 0 && timeUntilNextMessage <= 0) {
                message.send();
                iterator.remove();
                timeUntilNextMessage = 40;
            }
        }
    }

    private void queueMessage(String text, SendMode mode, boolean packet) {
        Message message = new Message(text, mode, packet);
        pendingMessages.add(message);
    }

    private void sendMessage(String text, SendMode mode, boolean packet) {
        if (packet) {
            if (!Utils.canUpdate()) return;
            mc.player.sendMessage(Text.of(mode.prefix + text));
        } else ChatUtils.sendPlayerMsg(mode.prefix + text);
    }

    public static class Message {
        private final String message;
        private final boolean packet;
        private int delay;

        public Message(String message, SendMode mode, boolean packet) {
            this.message = mode.prefix + message;
            this.delay = 40;
            this.packet = packet;
        }

        public void tick() {
            if (delay > 0) delay--;
        }

        public void send() {
            if (packet) {
                if (!Utils.canUpdate()) return;
                mc.player.sendMessage(Text.of(message));
            } else ChatUtils.sendPlayerMsg(message);
        }
    }

    public enum SendMode {
        Normal(""),
        Whisper("/w "),
        Message("/msg ");

        public final String prefix;
        SendMode(String prefix) {
            this.prefix = prefix;
        }
    }
}
