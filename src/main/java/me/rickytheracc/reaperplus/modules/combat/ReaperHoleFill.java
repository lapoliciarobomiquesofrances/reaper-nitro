package me.rickytheracc.reaperplus.modules.combat;

import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.enums.AntiCheat;
import me.rickytheracc.reaperplus.enums.ResistType;
import me.rickytheracc.reaperplus.enums.SwingMode;
import me.rickytheracc.reaperplus.mixininterface.IVec3d;
import me.rickytheracc.reaperplus.util.combat.Placing;
import me.rickytheracc.reaperplus.util.combat.Ranges;
import me.rickytheracc.reaperplus.util.player.Interactions;
import me.rickytheracc.reaperplus.util.player.PlayerUtil;
import me.rickytheracc.reaperplus.util.world.BlockUtil;
import me.rickytheracc.reaperplus.util.world.CombatHelper;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.AbstractBlockAccessor;
import meteordevelopment.meteorclient.mixininterface.IBox;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;

public class ReaperHoleFill extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");
    private final SettingGroup sgRange = settings.createGroup("Ranges");
    private final SettingGroup sgRender = settings.createGroup("Pause");

    // General

    private final Setting<AntiCheat> antiCheat = sgGeneral.add(new EnumSetting.Builder<AntiCheat>()
        .name("anti-cheat")
        .description("Which anti cheat the server uses.")
        .defaultValue(AntiCheat.NoCheat)
        .build()
    );

    private final Setting<Boolean> swapBack = sgTargeting.add(new BoolSetting.Builder()
        .name("swap-back")
        .description("Swap back to the previous slot after placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> airPlace = sgTargeting.add(new BoolSetting.Builder()
        .name("airplace")
        .description("Allow the module to place mid air.")
        .defaultValue(true)
        .build()
    );

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
            Blocks.COBWEB,
            Blocks.NETHERITE_BLOCK,
            Blocks.CRYING_OBSIDIAN
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

    public final Setting<Boolean> pauseOnUse = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-on-use")
        .description("Pauses while using an item.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> pauseOnMine = sgGeneral.add(new BoolSetting.Builder()
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
        .visible(rotate::get)
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

    private final Setting<Boolean> doubles = sgTargeting.add(new BoolSetting.Builder()
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

    // Ranges

    private final Setting<Double> targetRange = sgRange.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("How far away to target players.")
        .defaultValue(10)
        .sliderRange(0,20)
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

    private final Setting<Double> targetXZDistance = sgRange.add(new DoubleSetting.Builder()
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

    private final Setting<SwingMode> swingMode = sgRender.add(new EnumSetting.Builder<SwingMode>()
        .name("swing-mode")
        .description("How to swing your hand while filling holes.")
        .defaultValue(SwingMode.Packet)
        .build()
    );

    public final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Render the blocks being placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> fadeTime = sgRender.add(new IntSetting.Builder()
        .name("fade-time")
        .description("How long the renders should take to fade out.")
        .defaultValue(8)
        .min(1)
        .sliderRange(1,20)
        .visible(render::get)
        .build()
    );

    public final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("The shape mode of the renders.")
        .defaultValue(ShapeMode.Both)
        .visible(render::get)
        .build()
    );

    public final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the renders.")
        .defaultValue(new SettingColor(114, 11, 135,75))
        .visible(() -> render.get() && shapeMode.get().sides())
        .build()
    );

    public final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the renders.")
        .defaultValue(new SettingColor(114, 11, 135))
        .visible(() -> render.get() && shapeMode.get().lines())
        .build()
    );

    public ReaperHoleFill() {
        super(ReaperPlus.R, "reaper-holefill", "The best way to make your enemies really, REALLY mad.");
    }

    List<PlayerEntity> targets = new ArrayList<>();
    List<BlockPos> blockPosList = new ArrayList<>();
    HashMap<BlockPos, Byte> holes = new HashMap<>();

    private final BlockPos.Mutable testPos = new BlockPos.Mutable();
    private final Box testBox= new Box(0, 0, 0, 0, 0, 0);
    private Vec3d testVec = new Vec3d(0, 0, 0);

    private int delay, blocksPlaced;

    @Override
    public void onActivate() {
        targets.clear();
        blocksPlaced = 0;
        delay = 0;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onTick(TickEvent.Pre event) {
        if (delay > 0) {
            delay--;
            return;
        } else {
            delay = placeDelay.get();
            blocksPlaced = 0;
        }

        boolean shouldFill = switch (fillMode.get()) {
            case Both -> Interactions.isBurrowed() && Interactions.isInHole();
            case Burrow -> Interactions.isBurrowed();
            case InHole -> Interactions.isInHole();
            case Always -> true;
        };
        if (!shouldFill) return;

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
            if (onlyMoving.get()) if (!PlayerUtil.isMoving(player)) continue;
            targets.add(player);
            if (targets.size() >= maxTargets.get()) break;
        }

        if (targets.isEmpty()) return;

        // Find holes
        blockPosList.clear();
        holes.clear();
        blockPosList= BlockUtil.getSphere(mc.player, placeRange.get(), antiCheat.get());

        Iterator<BlockPos> iterator = blockPosList.iterator();
        while (iterator.hasNext()) {
            BlockPos blockPos = iterator.next();
            if (!validHole(blockPos)) iterator.remove();

            int blocks = 0;
            Direction air = null;

            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP) continue;

                BlockPos offsetPos = blockPos.offset(direction);
                if (BlockUtil.resistant(blockPos, ResistType.ANY)) blocks++;

                else if (mc.world.getBlockState(offsetPos).isAir() && !doubles.get()) {
                    iterator.remove();
                    break;
                }
                else if (direction == Direction.DOWN || air != null) {
                    iterator.remove();
                    break;
                }

                else if (validHole(offsetPos)) {
                    for (Direction dir : Direction.values()) {
                        if (dir == direction.getOpposite() || dir == Direction.UP) continue;

                        if (BlockUtil.resistant(blockPos, ResistType.PERMANENT)) blocks++;
                        else {
                            iterator.remove();
                            break;
                        }
                    }

                    air = direction;
                }
            }

            if (blocks == 5 && air == null) {
                holes.putIfAbsent(blockPos, null);
            } else if (blocks == 8 && doubles.get() && air != null) {
                holes.putIfAbsent(blockPos, Dir.get(air));
                holes.putIfAbsent(blockPos.offset(air), Dir.get(air.getOpposite()));
            }
        }

        for (Map.Entry<BlockPos, Byte> entry : holes.entrySet()) {
            if (Placing.place(entry.getKey(), item, swingMode.get(),
                rotate.get(), rotatePrio.get(), airPlace.get(),
                true, swapBack.get())
            ) {
                RenderUtils.renderTickingBlock(
                    entry.getKey(), sideColor.get(), lineColor.get(),
                    shapeMode.get(), entry.getValue(), fadeTime.get(),
                    true, false
                );

                blocksPlaced++;

                if (blocksPlaced >= holesPerTick.get()) {
                    delay = placeDelay.get();
                    break;
                }
            }
        }
    }

    private boolean validHole(BlockPos pos) {
        testPos.set(pos);
        if (!Ranges.inBlockRange(pos, antiCheat.get(), placeRange.get())) return false;

        WorldChunk chunk = mc.world.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()));
        BlockState state = chunk.getBlockState(pos);
        if (state.getBlock() == Blocks.COBWEB) return false;

        if (((AbstractBlockAccessor) mc.world.getBlockState(testPos).getBlock()).isCollidable()) return false;
        testPos.set(testPos.up());
        if (((AbstractBlockAccessor) mc.world.getBlockState(testPos).getBlock()).isCollidable()) return false;
        testPos.set(testPos.down());

        ((IBox) testBox).set(pos);
        if (!mc.world.getOtherEntities(null, testBox, entity
            -> entity instanceof PlayerEntity
            || entity instanceof TntEntity
            || entity instanceof EndCrystalEntity
        ).isEmpty()) return false;

        ((IVec3d) testVec).set(pos);
        testVec = testVec.add(0, 0.5, 0);

        return targets.stream().anyMatch(target -> {
            boolean yCheck = target.getY() > testVec.y && target.getY() - testVec.y <= targetYDistance.get();
            double dX = target.getX() - testVec.x, dZ = target.getZ() - testVec.z;
            boolean xzCheck = Math.sqrt(dX * dX + dZ * dZ) <= targetXZDistance.get();

            return yCheck && xzCheck;
        });
    }

    private FindItemResult getItemResult() {
        if (preferWebs.get()) {
            FindItemResult cobwebs = InvUtils.findInHotbar(Items.COBWEB);
            if (cobwebs.found()) return cobwebs;
        }
        return InvUtils.findInHotbar(itemStack -> blocks.get().contains(Block.getBlockFromItem(itemStack.getItem())));
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
        if (targets.isEmpty()) return null;
        return String.valueOf(targets.size());
    }

    public enum CheckMode {
        Always,
        InHole,
        Burrow,
        Both
    }

}
