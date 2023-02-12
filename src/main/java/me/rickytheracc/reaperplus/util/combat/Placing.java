package me.rickytheracc.reaperplus.util.combat;

import me.rickytheracc.reaperplus.enums.SwingMode;
import me.rickytheracc.reaperplus.enums.SwitchMode;
import me.rickytheracc.reaperplus.util.player.PlayerUtil;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Placing {

    // Placing blocks

//    public static boolean place (
//        BlockPos pos, FindItemResult result,
//        SwingMode swingMode, SwitchMode switchMode,
//        boolean rotate, int priority,
//        boolean oneTwelve, boolean checkEntities
//    ) {
//        if (!canPlace(pos, oneTwelve) || !result.found()) return false;
//
//        if (checkEntities) {
//            ItemStack stack = mc.player.getInventory().getStack(result.slot());
//            BlockState state = Block.getBlockFromItem(stack.getItem()).getDefaultState();
//            if (!mc.world.canPlace(state, pos, ShapeContext.absent())) return false;
//        }
//
//        Hand hand = result.isOffhand() ? Hand.OFF_HAND : Hand.MAIN_HAND;
//        int slot = result.slot();
//        if (hand != Hand.OFF_HAND && !inventory) {
//            if (slot > 8) return false;
//            if (slot < 0) return false;
//        }
//
//
//
//        // Create the BlockHitResult
//        BlockHitResult hitResult = null;
//
//        // Switch to the slot
//
//        placeBlock(hitResult, hand, swingMode, rotate, priority);
//        Statistics.addPlacement(hitResult.getBlockPos());
//
//        // Switch back to the selected slot
//
//        return true;
//    }

    private static boolean canPlace(BlockPos pos, boolean oneTwelve) {
        if (Statistics.pendingBlocks.containsKey(pos)) return false;

        BlockState state = mc.world.getBlockState(pos);
        if (state.getMaterial().isReplaceable()) return false;

        if (mc.world.isOutOfHeightLimit(pos.getY())) return false;
        if (!mc.world.getWorldBorder().contains(pos)) return false;

        if (oneTwelve) {
            FluidState fluidState = state.getFluidState();
            if (fluidState.isOf(Fluids.LAVA) || fluidState.isOf(Fluids.FLOWING_LAVA)) return false;
            return !fluidState.isOf(Fluids.WATER) && !fluidState.isOf(Fluids.FLOWING_WATER);
        }

        return true;
    }
}
