package me.rickytheracc.reapernitro.modules.chat;

import me.rickytheracc.reapernitro.Reaper;
import me.rickytheracc.reapernitro.modules.combat.ReaperSelfTrap;
import me.rickytheracc.reapernitro.util.misc.ReaperModule;
import me.rickytheracc.reapernitro.util.player.Interactions;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;

import static meteordevelopment.orbit.EventPriority.HIGHEST;

public class BedAlerts extends ReaperModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("check-range")
        .description("How far away to check players using beds.")
        .defaultValue(8)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Don't notify you if your friends use beds.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> countCrafting = sgGeneral.add(new BoolSetting.Builder()
        .name("count-crafting")
        .description("Mark people as bed users if they hold crafting tables.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> smartTrap = sgGeneral.add(new BoolSetting.Builder()
        .name("smart-trap")
        .description("automatically self-trap when a bed user is nearby.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> smartRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("smart-range")
        .description("How close a bed user needs to be to trigger smart trap.")
        .defaultValue(2)
        .min(0)
        .sliderMax(10)
        .visible(smartTrap::get)
        .build()
    );

    private final Setting<Boolean> requireHole = sgGeneral.add(new BoolSetting.Builder()
        .name("require-hole")
        .description("automatically self-trap when a bed user is nearby.")
        .defaultValue(true)
        .visible(smartTrap::get)
        .build()
    );

    public BedAlerts() {
        super(Reaper.C, "bed-alerts", "Alerts you about nearby players with beds in their inventory.");
    }

    private final ArrayList<PlayerEntity> bedFags = new ArrayList<>();

    @Override
    public void onActivate() {
        bedFags.clear();
    }

    @EventHandler(priority = HIGHEST)
    private void onCope(TickEvent.Pre event) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (bedFags.contains(player)) continue;
            if (mc.player.squaredDistanceTo(player) > range.get() * range.get()) continue;
            if (Friends.get().isFriend(player) && ignoreFriends.get()) continue;

            Item mainItem = player.getMainHandStack().getItem();
            if (mainItem instanceof BedItem || mainItem == Items.CRAFTING_TABLE && countCrafting.get()) {
                warning(player.getEntityName() + " is using beds!");
                bedFags.add(player);
                continue;
            }

            Item offItem = player.getOffHandStack().getItem();
            if (offItem instanceof BedItem || offItem == Items.CRAFTING_TABLE && countCrafting.get()) {
                warning(player.getEntityName() + " is using beds!");
                bedFags.add(player);
            }
        }

        if (!smartTrap.get()) return;
        if (requireHole.get() && !Interactions.isInHole()) return;

        for (PlayerEntity player : bedFags) {
            if (player.squaredDistanceTo(mc.player) <= smartRange.get() * smartRange.get()) {
                ReaperSelfTrap reaperSelfTrap = Modules.get().get(ReaperSelfTrap.class);
                if (!reaperSelfTrap.isActive()) reaperSelfTrap.toggle();
                break;
            }
        }
    }
}
