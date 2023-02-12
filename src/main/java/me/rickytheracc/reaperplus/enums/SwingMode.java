package me.rickytheracc.reaperplus.enums;

public enum SwingMode {
    Both,
    Packet,
    Client,
    None;

    public boolean client() {
        return this == Client || this == Both;
    }

    public boolean packet() {
        return this == Packet || this == Both;
    }
}
