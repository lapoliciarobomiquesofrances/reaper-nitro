package me.rickytheracc.reaperplus.modules.misc.elytrabot.utils;

public class TimerUtil {
    public long ms;

    public TimerUtil() {
        this.ms = 0;
    }

    public boolean hasPassed(int ms) {
        return System.currentTimeMillis() - this.ms >= ms;
    }

    public void reset() {
        this.ms = System.currentTimeMillis();
    }
}
