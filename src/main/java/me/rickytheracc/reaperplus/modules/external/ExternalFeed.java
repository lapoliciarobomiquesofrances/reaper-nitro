package me.rickytheracc.reaperplus.modules.external;

import me.rickytheracc.reaperplus.ReaperPlus;
import meteordevelopment.meteorclient.systems.modules.Module;

public class ExternalFeed extends Module {
    public ExternalFeed() {
        super(ReaperPlus.W, "external-killfeed", "render a killfeed outside the client");
    }
}
