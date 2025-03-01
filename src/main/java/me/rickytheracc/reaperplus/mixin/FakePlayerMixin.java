package me.rickytheracc.reaperplus.mixin;

import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.util.objects.AnglePos;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.player.FakePlayer;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerManager;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Mixin(value = FakePlayer.class, remap = false)
public class FakePlayerMixin extends Module {
    @Shadow(remap = false) @Final private SettingGroup sgGeneral;
    @Shadow(remap = false) @Final public Setting<String> name;

    @Unique private Setting<Boolean> loop = null;
    @Unique private boolean recording = false;
    @Unique private final List<AnglePos> posList = new ArrayList<>();
    @Unique private final List<AnglePos> posList2 = new ArrayList<>();
    @Unique private boolean playing = false;
    @Unique private WTextBox b;

    public FakePlayerMixin() {
        super(Categories.Player, "fake-player", "Spawns a client-side fake player for testing usages. No need to be active.");
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void onInit(CallbackInfo ci) {
        loop = sgGeneral.add(new BoolSetting.Builder().name("loop").description("Whether to loop the recorded movement after playing.").defaultValue(true).build());
    }

    @Inject(method = "getWidget", at = @At("RETURN"), cancellable = true, remap = false)
    private void onGetWidget(GuiTheme theme, CallbackInfoReturnable<WWidget> info) {
        WHorizontalList w = theme.horizontalList();
        WVerticalList l = theme.verticalList(); // setup lists

        WButton start = w.add(theme.button("Start Recording")).widget();
        WButton stop = w.add(theme.button("Stop Recording")).widget();
        WButton play = w.add(theme.button("Play Recording")).widget();
        WButton importrec = w.add(theme.button("Import")).widget();
        WButton exportrec = w.add(theme.button("Export")).widget();

        play.action = () -> playing = true;
        start.action = () -> {
            posList.clear();
            posList2.clear();
            recording = true;
        };
        stop.action = () -> {
            recording = false;
            posList2.addAll(posList);
        };
        importrec.action = this::importRecording;
        exportrec.action = () -> {
            if (posList.isEmpty()) {
                ChatUtils.error("No recording!");
            } else {
                exportRecording(posList);
            }
        };

        l.add(info.getReturnValue());
        l.add(theme.horizontalSeparator()).expandX();
        l.add(w); // setup input box for recording name
        WHorizontalList w2 = theme.horizontalList();
        b = w2.add(theme.textBox("Recording Name")).minWidth(400).expandX().widget();
        l.add(w2);
        info.setReturnValue(l);
    }

    @Unique
    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (recording)
            posList.add(new AnglePos(mc.player.getPos(), mc.player.getYaw(), mc.player.getPitch())); // recording
        if (playing) { // playback
            if (!posList.isEmpty()) {
                AnglePos angles = posList.remove(0);
                FakePlayerManager.forEach(entity -> {
                    entity.updateTrackedPositionAndAngles(angles.pos().x, angles.pos().y, angles.pos().z, angles.yaw(), angles.pitch(), 3, false);
                    entity.updateTrackedHeadRotation(angles.yaw(), 3);
                });
            } else {
                if (!posList2.isEmpty() && loop.get()) posList.addAll(posList2); // loop at the end
                else playing = false;
            }
        }
    }

    /**
     * @author GhostTypes
     */

    @Unique
    private void importRecording() {
        if (!ReaperPlus.RECORDINGS.exists()) {
            ReaperPlus.RECORDINGS.mkdirs();
            ChatUtils.error("You haven't saved any recordings yet!");
            return;
        }

        File i = new File(ReaperPlus.RECORDINGS, b.get() + ".rec");
        if (i.exists()) {
            try {
                posList.clear();
                BufferedReader reader = new BufferedReader(new FileReader(i));
                String l = reader.readLine();
                while (l != null) {
                    try {
                        String[] data = l.split(","); /* reconstruct angle pos data */
                        float yaw = Float.parseFloat(data[0]);
                        float pitch = Float.parseFloat(data[1]);
                        Vec3d pos = new Vec3d(Double.parseDouble(data[2]), Double.parseDouble(data[3]), Double.parseDouble(data[4]));
                        posList.add(new AnglePos(pos, yaw, pitch));
                    } catch (Exception e) {
                        ReaperPlus.log("Error reading FakePlayer recording.");
                        e.printStackTrace();
                        ChatUtils.error("Error reading recording " + i.getName());
                        posList.clear();
                        break;
                    }
                    l = reader.readLine();
                }
                ChatUtils.info("Imported recording " + i.getName() + "(Size: " + posList.size() + ")");
            } catch (Exception e) {
                ReaperPlus.log("Error importing FakePlayer recording.");
                e.printStackTrace();
                ChatUtils.error("Error importing recording " + i.getName());
                posList.clear();
            }
        } else {
            ChatUtils.error("Recording not found!");
            posList.clear();
        }
    }

    @Unique
    private void exportRecording(List<AnglePos> recording) {
        if (!ReaperPlus.RECORDINGS.exists()) ReaperPlus.RECORDINGS.mkdirs();

        SimpleDateFormat tstamp = new SimpleDateFormat("MM.dd.HH.mm"); /* setup output file */
        String timestamp = tstamp.format(Calendar.getInstance().getTime());
        File output = new File(ReaperPlus.RECORDINGS, timestamp + ".rec");

        try {

            boolean created = output.createNewFile();
            if (!created) {
                ChatUtils.error("Error creating recording file.");
                return;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(output.getPath()));

            for (AnglePos ap : recording) { /* store the angle pos list to a file */
                float yaw = ap.yaw();
                float pitch = ap.pitch();
                Vec3d pos = ap.pos();
                String l = yaw + "," + pitch + "," + pos.x + "," + pos.y + "," + pos.z;
                writer.write(l + "\n");
            }

            writer.close();
            ChatUtils.info("Exported recording " + timestamp + " (Size: " + recording.size() + ")");

        } catch (Exception e) {
            ReaperPlus.log("Error exporting FakePlayer recording.");
            e.printStackTrace();
            ChatUtils.error("Error exporting recording.");
        }
    }
}
