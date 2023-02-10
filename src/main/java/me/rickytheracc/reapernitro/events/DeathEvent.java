package me.rickytheracc.reapernitro.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class DeathEvent {
    private static final DeathEvent INSTANCE = new DeathEvent();

    public PlayerEntity player;
    public String name;
    public int pops;
    public Vec3d pos;

    public static DeathEvent get(PlayerEntity player, int pops) {
        INSTANCE.player = player;
        INSTANCE.pops = pops;
        INSTANCE.pos = player.getPos();
        INSTANCE.name = player.getEntityName();

        return INSTANCE;
    }
}
