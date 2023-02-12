package me.rickytheracc.reaperplus.util.world;

import me.rickytheracc.reaperplus.enums.AntiCheat;
import me.rickytheracc.reaperplus.enums.ResistType;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BlockUtil {
    private static final Box box = new Box(0, 0, 0, 0, 0, 0);
    private static final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private static final Vec3d vec3d = new Vec3d(0, 0, 0);

    public static List<BlockPos> getSphere(PlayerEntity player, double radius, AntiCheat antiCheat) {
        List<BlockPos> sphere = new ArrayList<>();

        int rad = (int) Math.ceil(radius);
        int x = player.getBlockX();
        int y = player.getBlockY();
        if (antiCheat == AntiCheat.NoCheat) y++;
        int z = player.getBlockZ();
        Vec3d origin = antiCheat.origin();

        for (int i = x - rad; i < x + rad; i++) {
            for (int j = y - rad; j < y + rad; j++) {
                for (int k = z - rad; k < z + rad; k++) {
                    blockPos.set(i, j, k);
                    if (blockPos.getSquaredDistance(origin) > radius * radius) continue;
                    sphere.add(blockPos.toImmutable());
                }
            }
        }

        return sphere;
    }

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
