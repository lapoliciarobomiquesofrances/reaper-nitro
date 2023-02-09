package me.rickytheracc.reapernitro.modules.chat;

import me.rickytheracc.reapernitro.modules.ML;
import me.rickytheracc.reapernitro.util.misc.ReaperModule;
import me.rickytheracc.reapernitro.util.player.Interactions;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;

public class ArmorAlert extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> threshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("durability")
        .description("How low an armor piece needs to be to alert you.")
        .defaultValue(2)
        .range(1, 100)
        .sliderRange(1, 100)
        .build()
    );

    public ArmorAlert() {
        super(ML.M, "armor-alert", "Alerts you when your armor pieces are low.");
    }

    private boolean alertedHelm;
    private boolean alertedChest;
    private boolean alertedLegs;
    private boolean alertedBoots;

    @Override
    public void onActivate() {
        alertedHelm = false;
        alertedChest = false;
        alertedLegs = false;
        alertedBoots = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Iterable<ItemStack> armorPieces = mc.player.getArmorItems();
        for (ItemStack armorPiece : armorPieces) {

            if (Interactions.checkThreshold(armorPiece, threshold.get())) {
                if (Interactions.isHelm(armorPiece) && !alertedHelm) {
                    warning("Your helmet is low");
                    alertedHelm = true;
                }
                if (Interactions.isChest(armorPiece) && !alertedChest) {
                    warning("Your chestplate is low");
                    alertedChest = true;
                }
                if (Interactions.isLegs(armorPiece) && !alertedLegs) {
                    warning("Your leggings are low");
                    alertedLegs = true;
                }
                if (Interactions.isBoots(armorPiece) && !alertedBoots) {
                    warning("Your boots are low");
                    alertedBoots = true;
                }
            }
            if (!Interactions.checkThreshold(armorPiece, threshold.get())) {
                if (Interactions.isHelm(armorPiece) && alertedHelm) alertedHelm = false;
                if (Interactions.isChest(armorPiece) && alertedChest) alertedChest = false;
                if (Interactions.isLegs(armorPiece) && alertedLegs) alertedLegs = false;
                if (Interactions.isBoots(armorPiece) && alertedBoots) alertedBoots = false;
            }
        }
    }
}
