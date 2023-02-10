package me.rickytheracc.reapernitro.modules.chat;

import me.rickytheracc.reapernitro.modules.ML;
import me.rickytheracc.reapernitro.modules.combat.ReaperSurround;
import me.rickytheracc.reapernitro.util.misc.ReaperModule;
import me.rickytheracc.reapernitro.util.world.BlockHelper;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.Surround;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

import static me.rickytheracc.reapernitro.util.world.BlockHelper.isOurSurroundBlock;

public class BreakAlert extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<Boolean> onlyOnSurround = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-surround")
        .description("Only notify you of breaking blocks while you have surround on.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Ignore break packets if the player is your friend.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("debug")
        .description("Send messages about what the module is doing.")
        .defaultValue(false)
        .build()
    );

    // Render

    private final Setting<Boolean> chatWarning = sgRender.add(new BoolSetting.Builder()
        .name("chat-warning")
        .description("Send a warning message in chat.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> renderBlock = sgRender.add(new BoolSetting.Builder()
        .name("render-block")
        .description("Render the block being broken.")
        .defaultValue(false)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(renderBlock::get)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color.")
        .defaultValue(new SettingColor(15, 255, 211,75))
        .visible(renderBlock::get)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color.")
        .defaultValue(new SettingColor(15, 255, 211))
        .visible(renderBlock::get)
        .build()
    );

    private final Setting<Boolean> renderText = sgRender.add(new BoolSetting.Builder()
        .name("render-name")
        .description("Will render the breaker's name over a breaking block.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> threeD = sgRender.add(new BoolSetting.Builder()
        .name("3D-text")
        .description("Will make the text scale with distance.")
        .defaultValue(false)
        .visible(renderText::get)
        .build()
    );

    private final Setting<Integer> divisor = sgRender.add(new IntSetting.Builder()
        .name("divisor")
        .defaultValue(6)
        .range(1,10)
        .sliderRange(1,10)
        .visible(() -> renderText.get() && threeD.get())
        .build()
    );

    private final Setting<Double> minScale = sgRender.add(new DoubleSetting.Builder()
        .name("min-scale")
        .description("The minimum scale of the nametag.")
        .defaultValue(0.5)
        .min(0.1)
        .visible(() -> renderText.get() && threeD.get())
        .build()
    );

    private final Setting<Double> textScale = sgRender.add(new DoubleSetting.Builder()
        .name("text-scale")
        .description("How big the damage text should be.")
        .defaultValue(1.5)
        .range(0.5,4)
        .sliderRange(0.5,4)
        .visible(renderText::get)
        .build()
    );

    private final Setting<Double> maxScale = sgRender.add(new DoubleSetting.Builder()
        .name("max-scale")
        .description("The scale of the nametag.")
        .defaultValue(1.5)
        .min(0.1)
        .visible(() -> renderText.get() && threeD.get())
        .build()
    );

    private final Setting<SettingColor> textColor = sgRender.add(new ColorSetting.Builder()
        .name("name-color")
        .description("What the color of the damage text should be.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(renderText::get)
        .build()
    );

    private final Setting<Boolean> textShadow = sgRender.add(new BoolSetting.Builder()
        .name("text-shadow")
        .description("Will render \"burrowed\" over burrowed targets.")
        .defaultValue(false)
        .visible(renderText::get)
        .build()
    );

    private final HashMap<BlockPos, String> breaking = new HashMap<>();
    private final Surround surround = Modules.get().get(Surround.class);
    private final ReaperSurround reaperSurround = Modules.get().get(ReaperSurround.class);
    private String lastMsg;

    public BreakAlert() {
        super(ML.C, "break-alert", "Alerts you when one of your surround blocks is being broken.");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof BlockBreakingProgressS2CPacket packet) {
            if (!surround.isActive() && !reaperSurround.isActive() && onlyOnSurround.get()) return;
            if (debug.get()) info("Received block break progress packet, checking");

            if (packet.getProgress() > 1 || !isOurSurroundBlock(packet.getPos())) {
                if (debug.get()) info("Progress > 0 or it's not our surround block.");
                return;
            }

            if (!BlockHelper.isTrapBlock(packet.getPos())) {
                if (debug.get()) info("Ignoring packet (entity id: " + packet.getEntityId() + ") - invalid block");
                return;
            }

            Entity player = mc.world.getEntityById(packet.getEntityId());
            if (Friends.get().isFriend((PlayerEntity) player) && ignoreFriends.get()) return;
            String message = player.getEntityName() + " is breaking your surround!";

            if (!message.equals(lastMsg)) {
                lastMsg = message;
                if (chatWarning.get()) warning(message);
                breaking.put(packet.getPos(), player.getEntityName());
            }
        }

        if (event.packet instanceof BlockUpdateS2CPacket packet) {
            if (!breaking.containsKey(packet.getPos())) return;

            if (packet.getState().isAir()) {
                breaking.remove(packet.getPos());
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (breaking.isEmpty()) return;

        breaking.entrySet().removeIf(entry -> !isOurSurroundBlock(entry.getKey()));
    }

    // Rendering
    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!renderBlock.get() || breaking.isEmpty()) return;

        for (Map.Entry<BlockPos, String> entry : breaking.entrySet()) {
            event.renderer.box(entry.getKey(), sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (!renderText.get() || breaking.isEmpty()) return;
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        for (Map.Entry<BlockPos, String> entry : breaking.entrySet()) {
            Vector3d vec3 = new Vector3d(
                entry.getKey().getX() + 0.5,
                entry.getKey().getY() + 0.5,
                entry.getKey().getZ() + 0.5
            );

            double distanceToCamera = vec3.distance(cameraPos.x, cameraPos.y, cameraPos.z);
            double scale = textScale.get() / (distanceToCamera / divisor.get());
            scale = MathHelper.clamp(scale, minScale.get(), maxScale.get());

            if (NametagUtils.to2D(vec3, (threeD.get()) ? scale : textScale.get())) {
                NametagUtils.begin(vec3);
                TextRenderer.get().begin(1, false, true);

                double width = TextRenderer.get().getWidth(entry.getValue()) * 0.5;
                TextRenderer.get().render(entry.getValue(), -width, 0, textColor.get(), textShadow.get());

                TextRenderer.get().end();
                NametagUtils.end();
            }
        }
    }
}
