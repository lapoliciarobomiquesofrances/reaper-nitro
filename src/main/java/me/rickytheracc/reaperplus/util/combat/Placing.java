package me.rickytheracc.reaperplus.util.combat;

import me.rickytheracc.reaperplus.enums.AntiCheat;
import me.rickytheracc.reaperplus.enums.SwingMode;
import me.rickytheracc.reaperplus.mixininterface.IVec3d;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.WorldChunk;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Placing {
    private static final BlockPos.Mutable testPos = new BlockPos.Mutable();
    private static Vec3d testVec = new Vec3d(0, 0, 0);

    public static boolean place (
        BlockPos pos, FindItemResult result,
        SwingMode swingMode, boolean rotate,
        int priority, boolean airplace,
        boolean checkEntities, boolean swapBack
    ) {
        if (!canPlace(pos, airplace) || !result.found()) {
            ChatUtils.sendMsg("Placing", Text.of("Failed canplace test or result not found"));
            return false;
        }

        if (checkEntities) {
            ItemStack stack = mc.player.getInventory().getStack(result.slot());
            BlockState state = Block.getBlockFromItem(stack.getItem()).getDefaultState();
            if (!mc.world.canPlace(state, pos, ShapeContext.absent())) {
                ChatUtils.sendMsg("Placing", Text.of("Failed entities check"));
                return false;
            }
        }

        // Create the BlockHitResult
        BlockHitResult hitResult = getPlaceResult(pos, airplace);
        if (hitResult == null) {
            ChatUtils.sendMsg("Placing", Text.of("Hitresult was null, failed"));
            return false;
        }

        place(hitResult, result, swingMode, rotate, priority, swapBack);
        Statistics.addPlacement(hitResult.getBlockPos());
        return true;
    }

    public static void place(
        BlockHitResult hitResult, FindItemResult result,
        SwingMode swingMode, boolean rotate,
        int priority, boolean swapBack
    ) {
        // Rotate to the pos if we should rotate

        if (rotate) Rotations.rotate(
            Rotations.getYaw(hitResult.getPos()),
            Rotations.getPitch(hitResult.getPos()),
            priority
        );

        // Figure out how and if we need to switch

        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        Hand hand = result.isOffhand() ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (!result.isHotbar() && hand == Hand.MAIN_HAND) return;
        int selectedSlot = mc.player.getInventory().selectedSlot;
        boolean selected = hand == Hand.OFF_HAND || result.slot() == selectedSlot;

        // Figure out if we need to sneak

        WorldChunk chunk = mc.world.getChunk(
            ChunkSectionPos.getSectionCoord(hitResult.getBlockPos().getX()),
            ChunkSectionPos.getSectionCoord(hitResult.getBlockPos().getZ())
        );
        BlockState state = chunk.getBlockState(hitResult.getBlockPos());
        boolean sneak = !mc.player.isSneaking() && state.onUse(mc.world, mc.player, hand, hitResult) != ActionResult.PASS;

        // Actually place the block

        if (!selected) InvUtils.swap(result.slot(), false);
        if (sneak) handler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

        handler.sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult, 0));
        if (swingMode.client()) mc.player.swingHand(hand, false);
        if (swingMode.packet()) handler.sendPacket(new HandSwingC2SPacket(hand));

        if (!selected && swapBack) InvUtils.swap(selectedSlot, false);
        if (sneak) handler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
    }

    public static BlockHitResult getPlaceResult(BlockPos pos, boolean airPlace) {
        BlockPos neighbor = null;
        Direction direction = closestDirection(pos, false);

        if (direction != null) {
            neighbor = pos.offset(direction);
            direction = direction.getOpposite();
        }

        if (neighbor == null || direction == null) {
            if (!airPlace) return null;
            neighbor = pos;
            direction = closestDirection(neighbor, true);
        };

        testVec = AntiCheat.NoCheat.origin();
        ((IVec3d) testVec).set(
            MathHelper.clamp(testVec.x, neighbor.getX(), neighbor.getX() + 1),
            MathHelper.clamp(testVec.y, neighbor.getY(), neighbor.getY() + 1),
            MathHelper.clamp(testVec.z, neighbor.getZ(), neighbor.getZ() + 1)
        );

        return new BlockHitResult(testVec, direction, neighbor, true);
    }

    public static Direction closestDirection(BlockPos pos, boolean airplace) {
        Direction bestDirection = null;
        double bestDistance = 36.0;

        for (Direction direction : Direction.values()) {
            testPos.set(pos.offset(direction));

            WorldChunk chunk = mc.world.getChunk(
                ChunkSectionPos.getSectionCoord(pos.getX()),
                ChunkSectionPos.getSectionCoord(pos.getZ())
            );

            if (airplace || !chunk.getBlockState(pos).isReplaceable() || Statistics.pendingBlocks.containsKey(pos)) {
                double distance = mc.player.squaredDistanceTo(sideVec(pos, direction));

                if (distance < bestDistance) {
                    if (direction == Direction.DOWN) return direction;
                    bestDistance = distance;
                    bestDirection = direction;
                }
            }
        }

        return bestDirection;
    }

    private static Vec3d sideVec(BlockPos pos, Direction direction) {
        ((IVec3d) testVec).set(pos);
        testVec = testVec.add(
            direction.getOffsetX() * 0.5,
            direction.getOffsetY() * 0.5,
            direction.getOffsetZ() * 0.5
        );
        return testVec;
    }

    private static boolean canPlace(BlockPos pos, boolean airplace) {
        BlockState state = mc.world.getBlockState(pos);
        if (!state.getMaterial().isReplaceable()) return false;

        if (mc.world.isOutOfHeightLimit(pos.getY())) return false;
        if (!mc.world.getWorldBorder().contains(pos)) return false;

        if (!airplace) {
            FluidState fluidState = state.getFluidState();
            if (fluidState.isOf(Fluids.LAVA) || fluidState.isOf(Fluids.FLOWING_LAVA)) return false;
            return !fluidState.isOf(Fluids.WATER) && !fluidState.isOf(Fluids.FLOWING_WATER);
        }

        return true;
    }
}
