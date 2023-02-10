package me.rickytheracc.reapernitro.events;

import net.minecraft.entity.player.PlayerEntity;

public class PopEvent {
    public static PopEvent INSTANCE = new PopEvent();

    public PlayerEntity player;
    public String name;
    public int pops;

    public static PopEvent get(PlayerEntity player, int pops) {
        INSTANCE.player = player;
        INSTANCE.name = player.getEntityName();
        INSTANCE.pops = pops;

        return INSTANCE;
    }
}
