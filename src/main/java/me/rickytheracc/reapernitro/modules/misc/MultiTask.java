package me.rickytheracc.reapernitro.modules.misc;

import me.rickytheracc.reapernitro.events.InteractEvent;
import me.rickytheracc.reapernitro.modules.ML;
import me.rickytheracc.reapernitro.util.misc.ReaperModule;
import meteordevelopment.orbit.EventHandler;

public class MultiTask extends ReaperModule {
    public MultiTask() {
        super(ML.M, "multi-task", "Allows you to eat while mining a block.");
    }

    @EventHandler
    public void onInteractEvent(InteractEvent event) {
        event.usingItem = false;
    }
}
