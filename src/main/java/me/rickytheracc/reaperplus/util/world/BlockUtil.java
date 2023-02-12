package me.rickytheracc.reaperplus.util.world;

import me.rickytheracc.reaperplus.enums.ResistType;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BlockUtil {


    public static Direction dirFromCoords(PlayerEntity player, BlockPos pos) {
        double xOffset = player.getX() - pos.getX();
        double zOffset = player.getZ() - pos.getZ();

        if (Math.abs(xOffset) > Math.abs(zOffset)) {
            if (xOffset <= 0) return Direction.WEST;
            else return Direction.EAST;
        } else {
            if (zOffset <= 0) return Direction.NORTH;
            else return Direction.SOUTH;
        }
    }

    public static boolean isCombatBlock(Block block) {
        return block == Blocks.OBSIDIAN
            || block == Blocks.CRYING_OBSIDIAN
            || block instanceof AnvilBlock
            || block == Blocks.NETHERITE_BLOCK
            || block == Blocks.ENDER_CHEST
            || block == Blocks.RESPAWN_ANCHOR
            || block == Blocks.ANCIENT_DEBRIS
            || block == Blocks.ENCHANTING_TABLE;
    }

    public static boolean resistant(BlockPos pos, ResistType type) {
        Block block = mc.world.getBlockState(pos).getBlock();
        float hardness = block.getHardness();
        float resistance = block.getBlastResistance();

        return switch (type) {
            case BREAKABLE -> hardness > 0 && resistance >= 600;
            case PERMANENT -> hardness < 0;
            case ANY -> resistance >= 600 || hardness < 0;
        };
    }

    public static double getBreakDelta(int slot, BlockState state) {
        if (slot == -1) return 0.0f;
        if (PlayerUtils.getGameMode() == GameMode.CREATIVE) return 1.0f;
        else return BlockUtils.getBreakDelta(slot, state);
    }
}
