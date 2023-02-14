package me.rickytheracc.reaperplus.modules.combat;

import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.enums.AntiCheat;
import me.rickytheracc.reaperplus.enums.SwingMode;
import me.rickytheracc.reaperplus.util.combat.Placing;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class PlacingTest extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("block")
        .description("What blocks to use for surround.")
        .defaultValue(
            Blocks.OBSIDIAN,
            Blocks.COBWEB,
            Blocks.NETHERITE_BLOCK,
            Blocks.CRYING_OBSIDIAN
        )
        .build()
    );

    private final Setting<AntiCheat> antiCheat = sgGeneral.add(new EnumSetting.Builder<AntiCheat>()
        .name("anti-cheat")
        .description("Which anti cheat the server uses.")
        .defaultValue(AntiCheat.NoCheat)
        .build()
    );

    private final Setting<Boolean> swapBack = sgGeneral.add(new BoolSetting.Builder()
        .name("swap-back")
        .description("Swap back to the previous slot after placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> airPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("airplace")
        .description("Allow the module to place mid air.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SwingMode> swingMode = sgGeneral.add(new EnumSetting.Builder<SwingMode>()
        .name("swing-mode")
        .description("How to swing your hand while filling holes.")
        .defaultValue(SwingMode.Both)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> rotatePrio = sgGeneral.add(new IntSetting.Builder()
        .name("rotate-priority")
        .defaultValue(50)
        .min(1)
        .sliderMax(100)
        .visible(rotate::get)
        .build()
    );

    public final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("The shape mode of the renders.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    public final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the renders.")
        .defaultValue(new SettingColor(114, 11, 135,75))
        .visible(() -> shapeMode.get().sides())
        .build()
    );

    public final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the renders.")
        .defaultValue(new SettingColor(114, 11, 135))
        .visible(() -> shapeMode.get().lines())
        .build()
    );

    public PlacingTest() {
        super(ReaperPlus.R, "placing-test", "Test module for the new placing utils.");
    }

    private BlockPos pos;
    private int ticksUntil;

    @Override
    public void onActivate() {
        ticksUntil = 40;
        pos = mc.player.getBlockPos();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (ticksUntil > 0) {
            ticksUntil--;
            return;
        }

        FindItemResult result = InvUtils.findInHotbar(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));
        if (Placing.place(pos, result, swingMode.get(), rotate.get(), rotatePrio.get(), airPlace.get(), true, swapBack.get())) {
            info("Ezpz");
        }

        toggle();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (pos == null) return;

        event.renderer.box(pos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }
}
