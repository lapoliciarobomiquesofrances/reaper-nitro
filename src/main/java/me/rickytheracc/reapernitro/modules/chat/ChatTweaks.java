package me.rickytheracc.reapernitro.modules.chat;

import me.rickytheracc.reapernitro.Reaper;
import me.rickytheracc.reapernitro.util.misc.ReaperModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public class ChatTweaks extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPrefix = settings.createGroup("Prefix");
//    private final SettingGroup sgPlaceHolders = settings.createGroup("Placeholders");

    // General

    private final Setting<Boolean> easyReply = sgGeneral.add(new BoolSetting.Builder()
        .name("easy-reply")
        .description("Lets you use /r on every server.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> cancelErrors = sgGeneral.add(new BoolSetting.Builder()
        .name("cancel-errors")
        .description("Cancels the reply errors.")
        .defaultValue(true)
        .visible(easyReply::get)
        .build()
    );

    // Prefix

    public final Setting<Boolean> usePrefix = sgPrefix.add(new BoolSetting.Builder()
        .name("custom-prefix")
        .description("Lets you set a custom prefix.")
        .defaultValue(false)
        .onChanged(setting -> handlePrefix())
        .build()
    );

    public final Setting<Boolean> override = sgPrefix.add(new BoolSetting.Builder()
        .name("override-meteor")
        .description("Lets you set a custom prefix.")
        .defaultValue(false)
        .visible(usePrefix::get)
        .onChanged(setting -> handlePrefix())
        .build()
    );

    public final Setting<String> prefixText = sgPrefix.add(new StringSetting.Builder()
        .name("prefix")
        .description("What the name in the prefix should be.")
        .defaultValue("Reaper")
        .visible(usePrefix::get)
        .onChanged(setting -> handlePrefix())
        .build()
    );

    public final Setting<SettingColor> prefixColor = sgPrefix.add(new ColorSetting.Builder()
        .name("prefix-color")
        .description("Color of the prefix text.")
        .defaultValue(new SettingColor(234, 76, 76))
        .visible(usePrefix::get)
        .onChanged(setting -> handlePrefix())
        .build()
    );

    public final Setting<String> leftBracket = sgPrefix.add(new StringSetting.Builder()
        .name("left-bracket")
        .description("What the left bracket should be.")
        .defaultValue("[")
        .visible(usePrefix::get)
        .onChanged(setting -> handlePrefix())
        .build()
    );

    public final Setting<SettingColor> leftBracketColor = sgPrefix.add(new ColorSetting.Builder()
        .name("bracket-color")
        .description("Color of the left bracket.")
        .defaultValue(new SettingColor(150,150,150,255))
        .visible(usePrefix::get)
        .onChanged(setting -> handlePrefix())
        .build()
    );

    public final Setting<String> rightBracket = sgPrefix.add(new StringSetting.Builder()
        .name("right-bracket")
        .description("What the right bracket should be.")
        .defaultValue("]")
        .visible(usePrefix::get)
        .onChanged(setting -> handlePrefix())
        .build()
    );

    public final Setting<SettingColor> rightBracketColor = sgPrefix.add(new ColorSetting.Builder()
        .name("bracket-color")
        .description("Color of the right bracket.")
        .defaultValue(new SettingColor(150,150,150,255))
        .visible(usePrefix::get)
        .onChanged(setting -> handlePrefix())
        .build()
    );

    public ChatTweaks() {
        super(Reaper.C, "chat-tweaks", "Various tweaks to the chat and prefix controller.");
    }

    private String whisperSender;
    private boolean whispered;

    @Override
    public void onActivate() {
        handlePrefix();
        whispered = false;
        whisperSender = null;
    }

    @Override
    public void onDeactivate() {
        ChatUtils.unregisterCustomPrefix("me.rickytheracc.reapernitro");
        if (override.get()) ChatUtils.unregisterCustomPrefix("meteordevelopment");
    }

    // Handling Prefix

    private void handlePrefix() {
        if (isActive()) {
            ChatUtils.registerCustomPrefix("me.rickytheracc.reapernitro", this::getPrefix);
            if (override.get()) ChatUtils.registerCustomPrefix("meteordevelopment", this::getPrefix);
        }
    }

    public Text getPrefix() {
        MutableText logo = Text.literal(prefixText.get());
        MutableText left = Text.literal(leftBracket.get());
        MutableText right = Text.literal(rightBracket.get());
        MutableText prefix = Text.literal("");

        logo.setStyle(logo.getStyle().withColor(TextColor.fromRgb(prefixColor.get().getPacked())));
        left.setStyle(left.getStyle().withColor(TextColor.fromRgb(leftBracketColor.get().getPacked())));
        right.setStyle(right.getStyle().withColor(TextColor.fromRgb(rightBracketColor.get().getPacked())));

        prefix.append(left);
        prefix.append(logo);
        prefix.append(right);
        prefix.append(" ");

        return prefix;
    }

    // Handling easy reply

    @EventHandler
    private void onPacketRecieve(PacketEvent.Receive event) {
        if (!easyReply.get()) return;

        if (event.packet instanceof GameMessageS2CPacket packet) {
            String text = packet.content().getString();

            // Prevent people just sending you the word whispers to break it
            if (text.startsWith("<")) return;

            if (text.contains("whispers")) {
                whisperSender = text.split(" ")[0];
                whispered = true;
                info("You are now replying to " + whisperSender + ".");
            } else if (text.contains("Unknown or incomplete command") || text.contains("<--[HERE]")) {
                if (cancelErrors.get()) event.cancel();
            }
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!easyReply.get()) return;

        if (event.packet instanceof ChatMessageC2SPacket packet) {
            String text = packet.chatMessage();
            if (easyReply.get() && whisperSender != null && whispered && text.split(" ")[0].equalsIgnoreCase("/r")) {
                event.cancel();
                mc.player.sendMessage(Text.of("/msg " + whisperSender + " " + text.substring(3)), false);
            }
        }
    }
}
