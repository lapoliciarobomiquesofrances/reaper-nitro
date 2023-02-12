package me.rickytheracc.reapernitro.modules.external;

import me.rickytheracc.reapernitro.Reaper;
import me.rickytheracc.reapernitro.util.misc.ReaperModule;

public class ExternalFeed extends ReaperModule {
    public ExternalFeed() {
        super(Reaper.W, "external-killfeed", "render a killfeed outside the client");
    }
}
