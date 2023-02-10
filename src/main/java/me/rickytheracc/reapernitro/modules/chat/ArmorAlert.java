package me.rickytheracc.reapernitro.modules.chat;

import me.rickytheracc.reapernitro.modules.ML;
import me.rickytheracc.reapernitro.util.misc.ReaperModule;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ArmorAlert extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<Double> threshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("durability")
        .description("How low an armor piece needs to be to alert you.")
        .defaultValue(2)
        .range(1, 100)
        .sliderRange(1, 100)
        .build()
    );

    public ArmorAlert() {
        super(ML.C, "armor-alert", "Alerts you when your armor pieces are low.");
    }

    List<Integer> alertedSlots = new ArrayList<>();

    @Override
    public void onActivate() {
        alertedSlots.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        for (int i = 0; i < 4; i++) {
            ItemStack item = mc.player.getInventory().getArmorStack(i);

            if (item.isEmpty()) {
                if (alertedSlots.contains(i)) alertedSlots.remove(i);
                return;
            }

            boolean low = (float) (item.getMaxDamage() - item.getDamage()) / item.getMaxDamage() * 100 < threshold.get();

            if (low) {
                String itemMessage = getMessage(i, item);
                warning("Your " + itemMessage + "low!");
                alertedSlots.add(i);
            } else if (alertedSlots.contains(i)) {
                alertedSlots.remove(i);
            }
        }
    }

    private String getMessage(int slot, ItemStack stack) {
        String name;

        if (stack.getItem() == Items.ELYTRA) name = "elytra is ";
        else switch (slot) {
            case 0 -> name = "boots are ";
            case 1 -> name = "leggings are ";
            case 2 -> name = "chestplate is ";
            case 3 -> name = "helmet is ";
            default -> throw new IllegalArgumentException("Invalid slot provided: " + slot);
        }

        return name;
    }

}
