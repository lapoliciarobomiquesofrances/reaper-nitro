package me.rickytheracc.reapernitro.modules.external;

import me.rickytheracc.reapernitro.Reaper;
import me.rickytheracc.reapernitro.util.misc.ReaperModule;
import me.rickytheracc.reapernitro.util.render.ExternalRenderers;
import me.rickytheracc.reapernitro.util.services.NotificationManager;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.orbit.EventHandler;

import java.awt.*;
import java.util.ArrayList;

public class ExternalNotifs extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> chroma = sgGeneral.add(new BoolSetting.Builder()
        .name("chroma")
        .description("Make the notifications extra gamer.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> chromaSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("chroma-speed")
        .description("How fast the chroma should be.")
        .defaultValue(0.01)
        .sliderMax(1)
        .build()
    );

    private final Setting<Integer> width = sgGeneral.add(new IntSetting.Builder()
        .name("width")
        .description("The width of the external frame.")
        .defaultValue(25)
        .min(10)
        .sliderMax(50)
        .build()
    );

    private final Setting<Integer> height = sgGeneral.add(new IntSetting.Builder()
        .name("height")
        .description("The height of the external frame.")
        .defaultValue(30)
        .min(20)
        .sliderMax(50)
        .build()
    );

    public ExternalNotifs() {
        super(Reaper.W, "external-notifications", "render notifications outside the client");
    }

    private ExternalRenderers.ExternalFrame externalFrame;
    private RainbowColor rc = new RainbowColor();

    @Override
    public void onActivate() {
        ExternalRenderers.activeFrames++;
        EventQueue.invokeLater(() -> {
            if (externalFrame == null) externalFrame = new ExternalRenderers.ExternalFrame(width.get(), height.get(), "Reaper Notifications", this);
            externalFrame.setVisible(true);
        });
        rc.setSpeed(chromaSpeed.get());
    }

    @Override
    public void onDeactivate() {
        ExternalRenderers.activeFrames--;
        if (externalFrame != null) externalFrame.setVisible(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (chroma.get() && externalFrame != null) {
            rc = rc.getNext();
            externalFrame.setTextColor(new Color(rc.r, rc.g, rc.b, rc.a));
        }
        setData();
    }

    private void setData() {
        ArrayList<String> data = new ArrayList<>();
        for (NotificationManager.Notification n : NotificationManager.getNotifications()) data.add(n.text);
        externalFrame.setText(data);
    }
}
