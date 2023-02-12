package me.rickytheracc.reaperplus.modules.misc;

import me.rickytheracc.reaperplus.ReaperPlus;
import me.rickytheracc.reaperplus.util.misc.MathUtil;
import me.rickytheracc.reaperplus.util.misc.ModuleHelper;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class StrictMove extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> alert = sgGeneral.add(new BoolSetting.Builder().name("alert").description("Alerts you when lag-back is detected.").defaultValue(false).build());
    private final Setting<Boolean> toggleCombat = sgGeneral.add(new BoolSetting.Builder().name("toggle-combat").description("Disable combat modules when lag-back is detected.").defaultValue(false).build());
    private final Setting<Boolean> toggleMovement = sgGeneral.add(new BoolSetting.Builder().name("toggle-movement").description("Disable movement modules when lag-back is detected.").defaultValue(false).build());

    private long lastPearl, lastChorus;

    public StrictMove() {
        super(ReaperPlus.M, "strict-move", "Mitigate lag-back from modules and/or strict anti-cheat");
    }

    @Override
    public void onActivate() {
        lastPearl = System.currentTimeMillis() - 5000;
        lastChorus = System.currentTimeMillis() - 5000;
        PacketFly pfly = Modules.get().get(PacketFly.class);
        if (pfly.isActive()) {
            error("Cannot use StrictMove and PacketFly at the same time!");
            toggle();
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket lookPacket) {
            if (MathUtil.msPassed(lastPearl) < 500 || MathUtil.msPassed(lastChorus) < 150) return;
            if (alert.get()) warning("Lag-back detected!");
            if (toggleCombat.get()) ModuleHelper.disableCombat(this);
            if (toggleMovement.get()) ModuleHelper.disableMovement(this);
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof PlayerInteractItemC2SPacket packet) {
            if (mc.player.getStackInHand(packet.getHand()).getItem().equals(Items.ENDER_PEARL)) {
                lastPearl = System.currentTimeMillis();
            } else if (mc.player.getStackInHand(packet.getHand()).getItem().equals(Items.CHORUS_FRUIT))  {
                lastChorus = System.currentTimeMillis();
            }
        }
    }
}
