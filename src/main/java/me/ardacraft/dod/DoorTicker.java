package me.ardacraft.dod;

import me.ardacraft.dod.door.Door;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class DoorTicker implements Consumer<Task> {

    private final DoorsOfDurin plugin;
    private final Map<UUID, Long> worlds = new HashMap<>();

    public DoorTicker(DoorsOfDurin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(Task task) {
        for (Door door : plugin.getDoors()) {
            if (door.isLocked()) {
                continue;
            }

            Optional<Location<World>> origin = door.getOrigin();
            if (!origin.isPresent()) {
                continue;
            }

            if (!hasTimeChanged(origin.get().getExtent())) {
                continue;
            }

            int change = door.getStateChange(origin.get().getExtent());
            if (change == Door.ACTIVE) {
                door.applyActive(origin.get());
            } else if (change == Door.INACTIVE) {
                door.applyInactive(origin.get());
            }
        }
    }

    private boolean hasTimeChanged(World world) {
        long now = world.getProperties().getWorldTime();
        long last = worlds.getOrDefault(world.getUniqueId(), -999L);
        if (now != last) {
            worlds.put(world.getUniqueId(), now);
            return true;
        }
        return false;
    }
}
