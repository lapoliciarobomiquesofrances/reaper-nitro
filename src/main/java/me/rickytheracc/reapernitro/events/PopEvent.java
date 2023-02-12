package me.rickytheracc.reapernitro.events;

import net.minecraft.entity.player.PlayerEntity;

public class PopEvent {
    public static PopEvent INSTANCE = new PopEvent();

    public PlayerEntity player;
    public boolean wasTarget;
    public String name;
    public int pops;

    public static PopEvent get(PlayerEntity player, int pops, boolean wasTarget) {
        INSTANCE.player = player;
        INSTANCE.wasTarget = wasTarget;
        INSTANCE.name = player.getEntityName();
        INSTANCE.pops = pops;

        return INSTANCE;
    }
}
