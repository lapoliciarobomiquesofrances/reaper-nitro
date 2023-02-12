package me.rickytheracc.reaperplus.modules.misc;

import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.events.InteractEvent;
import me.rickytheracc.reaperplus.util.misc.ReaperModule;
import meteordevelopment.orbit.EventHandler;

public class MultiTask extends ReaperModule {
    public MultiTask() {
        super(ReaperPlus.M, "multi-task", "Allows you to eat while mining a block.");
    }

    @EventHandler
    public void onInteractEvent(InteractEvent event) {
        event.usingItem = false;
    }
}
