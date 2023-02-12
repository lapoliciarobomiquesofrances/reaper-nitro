package me.rickytheracc.reaperplus.modules.combat;

import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.util.misc.ReaperModule;
import me.rickytheracc.reaperplus.util.player.Interactions;
import me.rickytheracc.reaperplus.util.world.BlockHelper;
import me.rickytheracc.reaperplus.util.world.CombatHelper;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class SmartHoleFill extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");
    private final SettingGroup sgRange = settings.createGroup("Ranges");
    private final SettingGroup sgPause = settings.createGroup("Pause");
    private final SettingGroup sgRender = settings.createGroup("Pause");

    // General

    private final Setting<Integer> placeDelay = sgGeneral.add(new IntSetting.Builder()
        .name("place-delay")
        .description("Delay between placing in holes.")
        .defaultValue(1)
        .min(0)
        .sliderMax(10)
        .build()
    );

    private final Setting<Integer> holesPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("holes-/-tick")
        .description("How many holes to fill per tick.")
        .defaultValue(3)
        .min(0)
        .sliderMax(10)
        .build()
    );

    private final Setting<CheckMode> fillMode = sgGeneral.add(new EnumSetting.Builder<CheckMode>()
        .name("fill-mode")
        .description("When to fill holes.")
        .defaultValue(CheckMode.Always)
        .build()
    );

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("block")
        .description("What blocks to use for surround.")
        .defaultValue(
            Blocks.OBSIDIAN,
            Blocks.COBWEB
        )
        .filter(this::blockFilter)
        .build()
    );

    private final Setting<Boolean> preferWebs = sgGeneral.add(new BoolSetting.Builder()
        .name("prefer-webs")
        .description("Always use webs if they're available.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> pauseOnUse = sgPause.add(new BoolSetting.Builder()
        .name("pause-on-use")
        .description("Pauses while using an item.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> pauseOnMine = sgPause.add(new BoolSetting.Builder()
        .name("pause-on-mine")
        .description("Pauses while mining a block.")
        .defaultValue(true)
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
        .build()
    );

    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder()
        .name("debug")
        .defaultValue(false)
        .build()
    );

    // Targeting

    private final Setting<Integer> maxTargets = sgTargeting.add(new IntSetting.Builder()
        .name("max-targets")
        .description("How many targets to fill at once.")
        .defaultValue(3)
        .min(1)
        .sliderRange(1,5)
        .build()
    );

    private final Setting<Boolean> fillDoubles = sgTargeting.add(new BoolSetting.Builder()
        .name("fill-doubles")
        .description("Fill double holes.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyMoving = sgTargeting.add(new BoolSetting.Builder()
        .name("only-moving")
        .description("Only fill holes of players that are moving.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ignoreSafe = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-safe")
        .description("Ignore players that are already in holes.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> predict = sgTargeting.add(new BoolSetting.Builder()
        .name("predict")
        .description("Predict where a player will be next tick.")
        .defaultValue(false)
        .build()
    );

    // Ranges

    private final Setting<Double> targetRange = sgRange.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("How far away to target players.")
        .defaultValue(4)
        .sliderRange(0,10)
        .build()
    );

    private final Setting<Double> placeRange = sgRange.add(new DoubleSetting.Builder()
        .name("place-range")
        .description("How far away to try and place blocks.")
        .defaultValue(4.5)
        .min(0)
        .sliderRange(0,6)
        .build()
    );

    private final Setting<Double> targetXDistance = sgRange.add(new DoubleSetting.Builder()
        .name("target-XZ-distance")
        .description("How close to a hole an enemy must be horizontally to fill it.")
        .defaultValue(2.5)
        .min(0)
        .sliderRange(0,5)
        .build()
    );

    private final Setting<Double> targetYDistance = sgRange.add(new DoubleSetting.Builder()
        .name("target-Y-distance")
        .description("How close to a hole an enemy must be vertically to fill it.")
        .defaultValue(4)
        .min(0)
        .sliderRange(0,5)
        .build()
    );

    // Render

    public final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .defaultValue(true)
        .build()
    );

    public final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    public final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .defaultValue(new SettingColor(114, 11, 135,75))
        .build()
    );

    public final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .defaultValue(new SettingColor(114, 11, 135))
        .build()
    );

    public SmartHoleFill() {
        super(ReaperPlus.R, "smart-holefill", "Hole fill but smart");
    }

    List<PlayerEntity> targets = new ArrayList<>();
    private int delay, blocksPlaced;

    @Override
    public void onActivate() {
        targets.clear();
        blocksPlaced = 0;
        delay = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (delay > 0) {
            delay--;
            return;
        } else {
            delay = placeDelay.get();
            blocksPlaced = 0;
        }

        if (pauseOnUse.get() && mc.player.isUsingItem()) return;
        if (pauseOnMine.get() && mc.interactionManager.isBreakingBlock()) return;

        // Get item
        FindItemResult item = getItemResult();
        if (!item.found()) return;

        // Targeting
        targets.clear();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player.isCreative() || player == mc.player || player.isDead() || !Friends.get().shouldAttack(player)) continue;
            if (player.squaredDistanceTo(mc.player) > targetRange.get() * targetRange.get()) continue;
            if (ignoreSafe.get()) if (CombatHelper.isInHole(player)) continue;
            if (onlyMoving.get()) {
                double dX = player.getX() - player.prevX;
                double dY = player.getY() - player.prevY;
                double dZ = player.getZ() - player.prevZ;
                if (dX == 0 && dY == 0 && dZ == 0) continue;
            }
            targets.add(player);
        }

        if (targets.isEmpty()) return;



        delayTimer--;
        if (delayTimer <= 0) {
            delayTimer = placeDelay.get();
            if (debug.get()) info("Getting holes");
            List<BlockPos> holes = BlockHelper.getHoles(mc.player.getBlockPos(), selfHoleRangeH.get(), selfHoleRangeV.get()); // get all nearby holes
            if (debug.get()) info("Starting list size: " + holes.size());
            holes.removeIf(hole -> BlockHelper.distanceBetween(target.getBlockPos(), hole) <= targetHoleRange.get()); // check target distance to hole
            if (debug.get()) info("List size after range check: " + holes.size());
            renderBlocks.addAll(holes);
            int filled = 0;
            for (BlockPos hole : holes) { // iterate through them
                BlockUtils.place(hole, item, rotate.get(), rotatePrio.get(), true);
                renderBlocks.removeIf(renderBlock -> BlockHelper.getBlock(renderBlock) != Blocks.AIR);
                filled++;
                if (filled >= holesPerTick.get()) break;
            }
        }
    }

    private FindItemResult getItemResult() {
        if (preferWebs.get()) {
            FindItemResult cobwebs = InvUtils.findInHotbar(Items.COBWEB);
            if (cobwebs.found()) return cobwebs;
        }
        return InvUtils.findInHotbar(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));
    }

    private boolean shouldFill() {
        if (debug.get()) info("Checking should fill");

        return switch (fillMode.get()) {
            case Both -> Interactions.isBurrowed() && Interactions.isInHole();
            case Burrow -> Interactions.isBurrowed();
            case InHole -> Interactions.isInHole();
            case Always -> true;
        };
    }

    private boolean blockFilter(Block block) {
        return block == Blocks.OBSIDIAN
            || block == Blocks.CRYING_OBSIDIAN
            || block == Blocks.NETHERITE_BLOCK
            || block == Blocks.ENDER_CHEST
            || block == Blocks.RESPAWN_ANCHOR
            || block == Blocks.COBWEB;
    }


    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }

    public enum CheckMode {
        Always,
        InHole,
        Burrow,
        Both
    }

}
