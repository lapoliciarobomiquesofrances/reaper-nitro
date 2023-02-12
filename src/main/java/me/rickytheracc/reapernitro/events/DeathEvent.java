package me.rickytheracc.reapernitro.events;

import net.minecraft.entity.player.PlayerEntity;

public class DeathEvent {
    private static final DeathEvent INSTANCE = new DeathEvent();

    public PlayerEntity player;
    public boolean wasTarget;
    public String name;
    public int pops;

    public static DeathEvent get(PlayerEntity player, int pops, boolean wasTarget) {
        INSTANCE.player = player;
        INSTANCE.pops = pops;
        INSTANCE.wasTarget = wasTarget;
        INSTANCE.name = player.getEntityName();

        return INSTANCE;
    }
}
