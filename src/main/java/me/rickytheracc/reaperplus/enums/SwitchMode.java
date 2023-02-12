package me.rickytheracc.reaperplus.enums;

public enum SwitchMode {
    NORMAL,
    SILENT,
    QUICK,
    NONE;

    public boolean shouldSwap() {
        return this == SILENT || this == NORMAL;
    }
}
