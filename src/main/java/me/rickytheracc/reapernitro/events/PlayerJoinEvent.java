package me.rickytheracc.reapernitro.events;

import net.minecraft.client.network.PlayerListEntry;

public class PlayerJoinEvent {
    private static final PlayerJoinEvent INSTANCE = new PlayerJoinEvent();

    public PlayerListEntry player;

    public static PlayerJoinEvent get(PlayerListEntry player) {
        INSTANCE.player = player;
        return INSTANCE;
    }
}
