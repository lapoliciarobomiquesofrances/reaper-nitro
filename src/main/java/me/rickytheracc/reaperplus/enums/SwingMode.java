package me.rickytheracc.reaperplus.enums;

public enum SwingMode {
    BOTH,
    PACKET,
    CLIENT,
    NONE;

    public boolean client() {
        return this == CLIENT || this == BOTH;
    }

    public boolean packet() {
        return this == PACKET || this == BOTH;
    }
}
