package me.rickytheracc.reaperplus.util.misc;

import me.rickytheracc.reaperplus.modules.combat.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.LongJump;
import meteordevelopment.meteorclient.systems.modules.movement.Step;
import meteordevelopment.meteorclient.systems.modules.movement.speed.Speed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleHelper {
    public static List<Module> movementModules = new ArrayList<>(Arrays.asList(
       Modules.get().get(Speed.class),
       Modules.get().get(Step.class),
       Modules.get().get(LongJump.class),
       Modules.get().get(TickShift.class)
    ));

    public static List<Module> combatModules = new ArrayList<>(Arrays.asList(
        Modules.get().get(AnchorGod.class),
        Modules.get().get(BedGod.class),
        Modules.get().get(QuickMend.class),
        Modules.get().get(ReaperSurround.class),
        Modules.get().get(ReaperSelfTrap.class),
        Modules.get().get(SmartHoleFill.class)
    ));

    public static void disableMovement(Module parent) {
        for (Module m : movementModules) {
            if (m.equals(parent)) continue;
            if (m.isActive()) m.toggle();
        }
    }

    public static void disableCombat(Module parent) {
        for (Module m : combatModules) {
            if (m.equals(parent)) continue;
            if (m.isActive()) m.toggle();
        }
    }

}
