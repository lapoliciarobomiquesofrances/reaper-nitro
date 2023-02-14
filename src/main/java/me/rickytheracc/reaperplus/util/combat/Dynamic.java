package me.rickytheracc.reaperplus.util.combat;

import me.rickytheracc.reaperplus.enums.ResistType;
import me.rickytheracc.reaperplus.mixininterface.IBox;
import me.rickytheracc.reaperplus.util.player.PlayerUtil;
import me.rickytheracc.reaperplus.util.world.BlockUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Dynamic {
    private static final List<BlockPos> posList = new ArrayList<>();
    private static final BlockPos.Mutable testPos = new BlockPos.Mutable();
    private static Box box = new Box(0, 0, 0, 0, 0, 0);

    public static boolean allPlaced(List<BlockPos> list) {
        for (BlockPos pos : list) {
            BlockState state = mc.world.getBlockState(pos);
            if (state.isReplaceable()) return false;
        }

        return true;
    }

    public static double[][] eightWay(Box box) {
        return new double[][]{
            {box.minX, box.minZ - 1},
            {box.maxX, box.minZ - 1},
            {box.maxX + 1, box.minZ},
            {box.maxX + 1, box.maxZ},
            {box.maxX, box.maxZ + 1},
            {box.minX, box.maxZ + 1},
            {box.minX - 1, box.maxZ},
            {box.minX - 1, box.minZ}
        };
    }

    public static double[][] fourWay(Box box) {
        return new double[][]{
            {box.minX, box.minZ},
            {box.maxX, box.minZ},
            {box.maxX, box.maxZ},
            {box.minX, box.maxZ}
        };
    }

    private static void prepForPos(PlayerEntity player, boolean predict) {
        posList.clear();

        if (player == mc.player) ((IBox) box).set(PlayerUtil.properHitbox());
        else ((IBox) box).set(player.getBoundingBox());
        ((IBox) box).contract(0.01);

        if (predict) {
            double deltaX = player.getX() - player.prevX;
            double deltaY = player.getX() - player.prevX;
            double deltaZ = player.getX() - player.prevX;
            box = box.offset(deltaX, deltaY, deltaZ);
        }
    }

    public static List<BlockPos> headPos(PlayerEntity target, boolean predict) {
        prepForPos(target, predict);

        for (double[] point : fourWay(box)) {
            testPos.set(point[0], box.maxY + 0.5, point[1]);
            if (!posList.contains(testPos)) posList.add(testPos.toImmutable());
        }

        return posList;
    }

    public static List<BlockPos> facePos(PlayerEntity target, boolean predict) {
        prepForPos(target, predict);

        for (double[] point : eightWay(box)) {
            testPos.set(point[0], box.maxY - 0.5, point[1]);
            if (!posList.contains(testPos)) posList.add(testPos.toImmutable());
        }

        return posList;
    }

    public static List<BlockPos> feetPos(PlayerEntity target, boolean predict) {
        prepForPos(target, predict);

        for (double[] point : eightWay(box)) {
            testPos.set(point[0], box.minY + 0.5, point[1]);
            if (!posList.contains(testPos)) posList.add(testPos.toImmutable());
        }

        return posList;
    }

    public static List<BlockPos> bottomPos(PlayerEntity target, boolean predict) {
        prepForPos(target, predict);

        for (double[] point : fourWay(box)) {
            testPos.set(point[0], box.minY - 0.5, point[1]);
            if (!posList.contains(testPos)) posList.add(testPos.toImmutable());
        }

        return posList;
    }
}
