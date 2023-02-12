package me.rickytheracc.reaperplus.util.combat;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Blink;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Dynamic {
    private static final List<BlockPos> posList = new ArrayList<>();
    private static final BlockPos.Mutable testPos = new BlockPos.Mutable();
    private static Box testBox = new Box(testPos);
    private static final Blink blink = Modules.get().get(Blink.class);

    public static boolean allPlaced(List<BlockPos> list) {
        for (BlockPos pos : list) {
            if (!BlockUtil.resistant(pos, ResistType.Any)) {
                return false;
            }
        }

        return true;
    }

    public static List<BlockPos> headPos(PlayerEntity target, boolean predict) {
        posList.clear();
        setBox(target);

        if (predict) {
            double deltaX = target.getX() - target.prevX;
            double deltaY = target.getX() - target.prevX;
            double deltaZ = target.getX() - target.prevX;
            testBox = testBox.offset(deltaX, deltaY, deltaZ);
        }

        for (double[] point : fourWay(testBox)) {
            testPos.set(point[0], testBox.maxY + 0.5, point[1]);
            if (!posList.contains(testPos)) posList.add(testPos.toImmutable());
        }

        return posList;
    }

    public static List<BlockPos> facePos(PlayerEntity target, boolean predict) {
        posList.clear();
        setBox(target);

        if (predict) {
            double deltaX = target.getX() - target.prevX;
            double deltaY = target.getX() - target.prevX;
            double deltaZ = target.getX() - target.prevX;
            testBox = testBox.offset(deltaX, deltaY, deltaZ);
        }

        for (double[] point : eightWay(testBox)) {
            testPos.set(point[0], testBox.maxY - 0.5, point[1]);
            if (!posList.contains(testPos)) posList.add(testPos.toImmutable());
        }

        return posList;
    }

    public static List<BlockPos> feetPos(PlayerEntity target, boolean predict) {
        posList.clear();
        setBox(target);

        if (predict) {
            double deltaX = target.getX() - target.prevX;
            double deltaY = target.getX() - target.prevX;
            double deltaZ = target.getX() - target.prevX;
            testBox = testBox.offset(deltaX, deltaY, deltaZ);
        }

        for (double[] point : eightWay(testBox)) {
            testPos.set(point[0], testBox.minY + 0.5, point[1]);
            if (!posList.contains(testPos)) posList.add(testPos.toImmutable());
        }

        return posList;
    }

    public static List<BlockPos> bottomPos(PlayerEntity target, boolean predict) {
        posList.clear();
        setBox(target);

        if (predict) {
            double deltaX = target.getX() - target.prevX;
            double deltaY = target.getX() - target.prevX;
            double deltaZ = target.getX() - target.prevX;
            testBox = testBox.offset(deltaX, deltaY, deltaZ);
        }

        for (double[] point : fourWay(testBox)) {
            testPos.set(point[0], testBox.minY - 0.5, point[1]);
            if (!posList.contains(testPos)) posList.add(testPos.toImmutable());
        }

        return posList;
    }

    private static void setBox(PlayerEntity player) {
        if (blink.isActive() && player == mc.player){
            ((IBox) testBox).set(((IBlink) blink).getOldBox());
        } else ((IBox) testBox).set(player.getBoundingBox());
        testBox = testBox.contract(0.01);
    }

    /**
     * Helper method to calculate the 8 neighboring blocks of a box, written by BigIron
     */
    private static double[][] eightWay(Box box) {
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

    /**
     * Helper method to calculate the 4 neighboring blocks of a box, written by BigIron
     */
    private static double[][] fourWay(Box box) {
        return new double[][]{
            {box.minX, box.minZ},
            {box.maxX, box.minZ},
            {box.maxX, box.maxZ},
            {box.minX, box.maxZ}
        };
    }
}
