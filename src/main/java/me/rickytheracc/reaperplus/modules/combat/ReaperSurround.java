package me.rickytheracc.reaperplus.modules.combat;

import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.enums.SwingMode;
import me.rickytheracc.reaperplus.mixininterface.IBox;
import me.rickytheracc.reaperplus.util.combat.Crystals;
import me.rickytheracc.reaperplus.util.combat.Dynamic;
import me.rickytheracc.reaperplus.util.combat.Placing;
import me.rickytheracc.reaperplus.util.combat.Statistics;
import me.rickytheracc.reaperplus.util.world.BlockUtil;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Blink;
import meteordevelopment.meteorclient.systems.modules.movement.Step;
import meteordevelopment.meteorclient.systems.modules.movement.speed.Speed;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.*;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class ReaperSurround extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAntiCheat = settings.createGroup("AntiCheat");
    private final SettingGroup sgAntiCity = settings.createGroup("AntiCity");
    private final SettingGroup sgToggles = settings.createGroup("Toggles");
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General

    private final Setting<List<Block>> primary = sgGeneral.add(new BlockListSetting.Builder()
        .name("primary-blocks")
        .description("What blocks to use for Surround.")
        .defaultValue(Blocks.OBSIDIAN)
        .filter(BlockUtil::isCombatBlock)
        .build()
    );

    private final Setting<List<Block>> fallback = sgGeneral.add(new BlockListSetting.Builder()
        .name("fallback-blocks")
        .description("What blocks to use for Surround if no primary block is found.")
        .defaultValue(Blocks.ENDER_CHEST)
        .filter(BlockUtil::isCombatBlock)
        .build()
    );

    private final Setting<Center> center = sgGeneral.add(new EnumSetting.Builder<Center>()
        .name("center")
        .description("Move the player to the middle of a block to not obstruct placing.")
        .defaultValue(Center.OnActivate)
        .build()
    );

    private final Setting<Boolean> onlyGround = sgGeneral.add(new BoolSetting.Builder()
        .name("on-ground")
        .description("Only try to place blocks when you're on the ground (not required while blinking).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlySneak = sgGeneral.add(new BoolSetting.Builder()
        .name("on-sneak")
        .description("Only try to place blocks when you're sneaking (not required while blinking).")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<Module>> modules = sgGeneral.add(new ModuleListSetting.Builder()
        .name("toggle-off")
        .description("Toggle off certain modules when activating surround.")
        .defaultValue(
            Step.class,
            Speed.class
        )
        .build()
    );

    private final Setting<Boolean> toggleBack = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-back")
        .description("Turn the other modules back on when surround is deactivated.")
        .defaultValue(true)
        .visible(() -> !modules.get().isEmpty())
        .build()
    );

    private final Setting<Boolean> chatInfo = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-info")
        .description("Send info in chat about surround.")
        .defaultValue(true)
        .build()
    );

    // Anticheat

    private final Setting<Integer> placeDelay = sgAntiCheat.add(new IntSetting.Builder()
        .name("place-delay")
        .description("Tick delay between block placements.")
        .defaultValue(1)
        .range(0,20)
        .sliderRange(0,20)
        .build()
    );

    private final Setting<Integer> blocksPerTick = sgAntiCheat.add(new IntSetting.Builder()
        .name("blocks-/-tick")
        .description("Blocks placed per tick.")
        .defaultValue(4)
        .min(0)
        .sliderRange(1,12)
        .build()
    );

    private final Setting<Boolean> airPlace = sgAntiCheat.add(new BoolSetting.Builder()
        .name("airplace")
        .description("Will try to find support blocks if turned off. (Basically for 1.12)")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> swapBack = sgAntiCheat.add(new BoolSetting.Builder()
        .name("swap-back")
        .description("Swap back to your original slot after placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgAntiCheat.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotate towards the blocks you're placing.")
        .defaultValue(false)
        .build()
    );

    // Anticity

    private final Setting<AntiCityMode> antiCityMode = sgAntiCity.add(new EnumSetting.Builder<AntiCityMode>()
        .name("anti-city-mode")
        .description("Try to place back broken blocks instantly. BreakPacket could cause desync.")
        .defaultValue(AntiCityMode.Smart)
        .build()
    );

    private final Setting<Boolean> checkEntities = sgAntiCity.add(new BoolSetting.Builder()
        .name("check-entities")
        .description("Check if there are entities in the way before placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> breakCrystals = sgAntiCity.add(new BoolSetting.Builder()
        .name("break-crystals")
        .description("Break beds holding your surround from being replaced.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> suicideBreak = sgAntiCity.add(new BoolSetting.Builder()
        .name("suicide")
        .description("Break crystals that could pop or kill you.")
        .defaultValue(false)
        .visible(breakCrystals::get)
        .build()
    );

    private final Setting<Boolean> crystalsRotate = sgAntiCity.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotate towards the crystals you're breaking server side.")
        .defaultValue(false)
        .visible(breakCrystals::get)
        .build()
    );

    private final Setting<Integer> attackDelay = sgAntiCity.add(new IntSetting.Builder()
        .name("break-delay")
        .description("How long to wait between breaking crystals.")
        .defaultValue(5)
        .min(0)
        .sliderRange(0,20)
        .visible(breakCrystals::get)
        .build()
    );

    private final Setting<Integer> ticksExisted = sgAntiCity.add(new IntSetting.Builder()
        .name("ticks-existed")
        .description("How many ticks a crystal must have existed to be broken.")
        .defaultValue(0)
        .min(0)
        .sliderRange(0,20)
        .visible(breakCrystals::get)
        .build()
    );

    private final Setting<SwingMode> breakSwingMode = sgAntiCity.add(new EnumSetting.Builder<SwingMode>()
        .name("swing-mode")
        .description("How to swing your hand while attacking a crystal.")
        .defaultValue(SwingMode.Both)
        .visible(breakCrystals::get)
        .build()
    );

    // Toggles

    private final Setting<Boolean> toggleOnYChange = sgToggles.add(new BoolSetting.Builder()
        .name("toggle-on-y-change")
        .description("Automatically disables when your Y level changes.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> toggleOnComplete = sgToggles.add(new BoolSetting.Builder()
        .name("toggle-on-complete")
        .description("Toggles off when all blocks are placed.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> toggleOnTeleport = sgToggles.add(new BoolSetting.Builder()
        .name("toggle-on-teleport")
        .description("Toggles off when you teleport (use a pearl or chorus).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> toggleOnDeath = sgToggles.add(new BoolSetting.Builder()
        .name("toggle-on-death")
        .description("Toggles off when you die.")
        .defaultValue(true)
        .build()
    );

    // Render

    private final Setting<SwingMode> placeSwingMode = sgRender.add(new EnumSetting.Builder<SwingMode>()
        .name("swing-mode")
        .description("How to swing your hand while placing a block.")
        .defaultValue(SwingMode.Both)
        .build()
    );

    private final Setting<Boolean> renderPlace = sgRender.add(new BoolSetting.Builder()
        .name("render-place")
        .description("Will render where surround is placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> fadeTime = sgRender.add(new IntSetting.Builder()
        .name("fade-time")
        .description("How many ticks the fade render should last.")
        .defaultValue(8)
        .min(1)
        .sliderRange(1,20)
        .visible(renderPlace::get)
        .build()
    );

    private final Setting<ShapeMode> placeShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered for placing.")
        .defaultValue(ShapeMode.Both)
        .visible(renderPlace::get)
        .build()
    );

    private final Setting<SettingColor> placeSides = sgRender.add(new ColorSetting.Builder()
        .name("place-side-color")
        .description("The color of placing blocks.")
        .defaultValue(new SettingColor(255, 255, 255, 25))
        .visible(() -> renderPlace.get() && placeShapeMode.get().sides())
        .build()
    );

    private final Setting<SettingColor> placeLines = sgRender.add(new ColorSetting.Builder()
        .name("place-line-color")
        .description("The color of placing line.")
        .defaultValue(new SettingColor(255, 255, 255, 150))
        .visible(() -> renderPlace.get() && placeShapeMode.get().lines())
        .build()
    );

    private final Setting<Boolean> renderActive = sgRender.add(new BoolSetting.Builder()
        .name("render-active")
        .description("Will render which blocks surround is currently protecting.")
        .defaultValue(false)
        .build()
    );

    private final Setting<ShapeMode> activeShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered for active blocks.")
        .defaultValue(ShapeMode.Lines)
        .visible(renderActive::get)
        .build()
    );

    private final Setting<SettingColor> safeSides = sgRender.add(new ColorSetting.Builder()
        .name("safe-sides")
        .description("The side color for safe blocks.")
        .defaultValue(new SettingColor(13, 255, 0, 0))
        .visible(() -> renderActive.get() && activeShapeMode.get().sides())
        .build()
    );

    private final Setting<SettingColor> safeLines = sgRender.add(new ColorSetting.Builder()
        .name("safe-lines")
        .description("The line color for safe blocks.")
        .defaultValue(new SettingColor(13, 255, 0, 0))
        .visible(() -> renderActive.get() && activeShapeMode.get().lines())
        .build()
    );

    private final Setting<SettingColor> normalSides = sgRender.add(new ColorSetting.Builder()
        .name("normal-sides")
        .description("The side color for normal blocks.")
        .defaultValue(new SettingColor(0, 255, 238, 15))
        .visible(() -> renderActive.get() && activeShapeMode.get().sides())
        .build()
    );

    private final Setting<SettingColor> normalLines = sgRender.add(new ColorSetting.Builder()
        .name("normal-lines")
        .description("The line color for normal blocks.")
        .defaultValue(new SettingColor(0, 255, 238, 125))
        .visible(() -> renderActive.get() && activeShapeMode.get().lines())
        .build()
    );

    private final Setting<SettingColor> unsafeSides = sgRender.add(new ColorSetting.Builder()
        .name("unsafe-sides")
        .description("The side color for unsafe blocks.")
        .defaultValue(new SettingColor(204, 0, 0, 15))
        .visible(() -> renderActive.get() && activeShapeMode.get().sides())
        .build()
    );

    private final Setting<SettingColor> unsafeLines = sgRender.add(new ColorSetting.Builder()
        .name("unsafe-lines")
        .description("The line color for unsafe blocks.")
        .defaultValue(new SettingColor(204, 0, 0, 125))
        .visible(() -> renderActive.get() && activeShapeMode.get().lines())
        .build()
    );

    public ReaperSurround() {
        super(ReaperPlus.R, "reaper-surround", "The best surround in the game ong");
    }

    private final List<BlockPos> positions = new ArrayList<>();
    private final List<BlockPos> breaking = new ArrayList<>();
    private final List<Module> toActivate = new ArrayList<>();
    private final Blink blink = Modules.get().get(Blink.class);

    private final Box testBox = new Box(0, 0, 0, 0, 0, 0);

    private int blockDelay;
    private int crystalDelay;
    public boolean complete;

    @Override
    public void onActivate() {
        positions.clear();
        breaking.clear();

        blockDelay = 0;
        crystalDelay = 0;

        if (center.get() == Center.OnActivate && !blink.isActive()) {
            PlayerUtils.centerPlayer();
        }

        for (Module module : modules.get()) {
            if (module.isActive()) {
                module.toggle();
                toActivate.add(module);
            }
        }
    }

    @Override
    public void onDeactivate() {
        if (!toggleBack.get()) return;

        for (Module module : toActivate) {
            if (!module.isActive()) {
                module.toggle();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (toggleOnYChange.get() && !blink.isActive()) {
            if (mc.player.getY() != mc.player.prevY) {
                if (chatInfo.get()) warning("Toggled off because your Y changed.");
                toggle();
                return;
            }
        }

        if (blockDelay > 0) blockDelay--;
        if (crystalDelay > 0) crystalDelay--;

        if (!blink.isActive()) {
            if (onlyGround.get() && !mc.player.isOnGround()) return;
            if (onlySneak.get() && (!mc.options.sneakKey.isPressed() || mc.player.isSneaking())) return;
        }

        positions.clear();
        positions.addAll(Dynamic.bottomPos(mc.player, false));
        positions.addAll(Dynamic.feetPos(mc.player, false));
        breaking.removeIf(pos -> pos.getSquaredDistance(mc.player.getPos()) > 7 * 7);

        complete = Dynamic.allPlaced(positions);
        if (complete) {
            if (toggleOnComplete.get()) {
                if (chatInfo.get()) warning("Toggled off because your surround is complete.");
                toggle();
                return;
            }
        } else if (center.get() == Center.Incomplete && !blink.isActive()) {
            PlayerUtils.centerPlayer();
        }

        int blocksPlaced = 0;
        boolean hasBroken = false;
        FindItemResult itemResult = getTargetBlock();

        for (BlockPos pos : positions) {
            if (crystalDelay <= 0 && breakCrystals.get() && !hasBroken) {
                ((IBox) testBox).set(pos);
                List<EndCrystalEntity> crystals = mc.world.getEntitiesByClass(EndCrystalEntity.class, testBox, Entity::isAlive);

                for (EndCrystalEntity crystal : crystals) {
                    if (crystal.age < ticksExisted.get()) continue;

                    double damage = DamageUtils.crystalDamage(mc.player, crystal.getPos());
                    if (damage >= PlayerUtils.getTotalHealth() && !suicideBreak.get()) continue;

                    if (crystalsRotate.get()) Rotations.rotate(Rotations.getYaw(crystal), Rotations.getPitch(crystal));

                    if (Crystals.attackCrystal(crystal, breakSwingMode.get())) {
                        hasBroken = true;
                        crystalDelay = attackDelay.get();
                        break;
                    }
                }
            }

            if (!itemResult.found()) continue;
            if (Statistics.pendingBlocks.containsKey(pos)) continue;

            if (blockDelay <= 0) {
                if (Placing.place(pos, itemResult, placeSwingMode.get(),
                    rotate.get(), 999, airPlace.get(),
                    true, swapBack.get())
                ) {
                    RenderUtils.renderTickingBlock(
                        pos, placeSides.get(), placeLines.get(),
                        placeShapeMode.get(), 0,
                        fadeTime.get(), true, false
                    );

                    blocksPlaced++;

                    if (blocksPlaced >= blocksPerTick.get()) {
                        blockDelay = placeDelay.get();
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (!Utils.canUpdate()) return;

        if (event.packet instanceof EntityStatusS2CPacket packet && toggleOnDeath.get()) {
            if (packet.getEntity(mc.world) != mc.player) return;
            if (packet.getStatus() != 3) return;

            if (chatInfo.get()) warning("Toggled off because you died.");
            toggle();
            return;
        }

        if (event.packet instanceof TeleportConfirmC2SPacket && toggleOnTeleport.get()) {
            if (chatInfo.get()) warning("Toggled off because you teleported.");
            toggle();
            return;
        }

        if (event.packet instanceof BlockBreakingProgressS2CPacket packet) {
            if (!positions.contains(packet.getPos()) || antiCityMode.get() == AntiCityMode.None) return;
            FindItemResult itemResult = getTargetBlock();

            if (antiCityMode.get() == AntiCityMode.Instant) {
                BlockHitResult placeResult = Placing.getPlaceResult(packet.getPos(), airPlace.get());
                Placing.place(placeResult, itemResult, placeSwingMode.get(), rotate.get(), 99999, swapBack.get());
            }

            if (antiCityMode.get() == AntiCityMode.Smart) {

            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!renderActive.get() || positions.isEmpty()) return;

        for (BlockPos pos : positions) {
            Color[] holeColors = getPosColors(pos);
            event.renderer.box(pos, holeColors[0], holeColors[1], activeShapeMode.get(), 0);
        }
    }

    private Color[] getPosColors(BlockPos pos) {
        WorldChunk chunk = mc.world.getChunk(
            ChunkSectionPos.getSectionCoord(pos.getX()),
            ChunkSectionPos.getSectionCoord(pos.getZ())
        );
        BlockState state = chunk.getBlockState(pos);

        if (state.getHardness(mc.world, pos) < 0) return new Color[]{safeSides.get(), safeLines.get()};
        else if (state.getBlock().getBlastResistance() >= 600) return new Color[]{normalSides.get(), normalLines.get()};
        else return new Color[]{unsafeSides.get(), unsafeLines.get()};
    }

    private FindItemResult getTargetBlock() {
        FindItemResult result = InvUtils.findInHotbar(itemStack -> primary.get().contains(Block.getBlockFromItem(itemStack.getItem())));
        if (!result.found()) result = InvUtils.findInHotbar(itemStack -> fallback.get().contains(Block.getBlockFromItem(itemStack.getItem())));

        return result;
    }

    public enum AntiCityMode {
        Instant,
        Smart,
        None
    }

    public enum Center {
        OnActivate,
        Incomplete,
        Never
    }
}
