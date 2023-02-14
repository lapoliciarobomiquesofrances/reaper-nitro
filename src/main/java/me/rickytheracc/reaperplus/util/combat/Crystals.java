package me.rickytheracc.reaperplus.util.combat;

import me.rickytheracc.reaperplus.enums.SwingMode;
import me.rickytheracc.reaperplus.mixininterface.ICrystalAura;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static meteordevelopment.orbit.EventPriority.HIGHEST;

public class Crystals {
    @PostInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(Crystals.class);
        ticks = 0;
        attacksThisTick = 0;
    }

    private static int ticks;
    public static int attacksThisTick;
    public static boolean brokenThisTick;

    @EventHandler
    private static void onGameJoined(GameJoinedEvent event) {
        ticks = 0;
        attacksThisTick = 0;
    }

    // Needs to be the absolute first thing run so the boolean
    // is always false before other modules break crystals
    @EventHandler(priority = HIGHEST + 99999)
    public static void onTick(TickEvent.Pre event) {
        brokenThisTick = false;

        if (ticks > 0) {
            ticks--;
            return;
        }

        attacksThisTick = 0;
        ticks = 20;
    }

    public static boolean attackCrystal(Entity entity, SwingMode swingMode) {
        int maxFrequency = ((ICrystalAura) Modules.get().get(CrystalAura.class)).getFrequency();
        if (attacksThisTick >= maxFrequency || entity == null|| brokenThisTick) return false;

        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));

        Hand hand = InvUtils.findInHotbar(Items.END_CRYSTAL).getHand();
        if (hand == null) hand = Hand.MAIN_HAND;

        if (swingMode.client()) mc.player.swingHand(hand, false);
        if (swingMode.packet()) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));

        brokenThisTick = true;
        attacksThisTick++;
        return true;
    }
}
