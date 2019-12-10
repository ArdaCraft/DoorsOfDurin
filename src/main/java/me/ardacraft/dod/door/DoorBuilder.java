package me.ardacraft.dod.door;

import com.flowpowered.math.vector.Vector3i;
import me.dags.stopmotion.libs.pitaya.util.optional.Result;
import me.dags.stopmotion.trigger.rule.Time;
import org.spongepowered.api.world.schematic.Schematic;

public class DoorBuilder {

    public Time time;
    public int state;
    public String link = "";
    public String world;
    public Vector3i origin;
    public Schematic active;
    public Schematic inactive;

    public Result<Door, String> build(String name) {
        if (time == null) {
            return Result.fail("Time not set");
        }
        if (world == null || origin == null) {
            return Result.fail("Origin not set");
        }
        if (active == null) {
            return Result.fail("Active schematic not set");
        }
        if (inactive == null) {
            return Result.fail("Inactive schematic not set");
        }
        return Result.pass(new Door(name, this));
    }
}
