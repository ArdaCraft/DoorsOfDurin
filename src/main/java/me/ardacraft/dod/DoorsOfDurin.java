package me.ardacraft.dod;

import com.google.inject.Inject;
import me.ardacraft.dod.door.Door;
import me.dags.motion.instance.Instance;
import me.dags.pitaya.command.CommandBus;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Plugin(id = "dod")
public class DoorsOfDurin {

    private final Path dir;
    private final List<Door> backing = new LinkedList<>();
    private final List<Door> doors = Collections.unmodifiableList(backing);

    @Inject
    public DoorsOfDurin(@ConfigDir(sharedRoot = false) Path dir) {
        this.dir = dir;
    }

    @Listener
    public void init(GameInitializationEvent event) {
        reload(null);
        CommandBus.create().register(new DoDCommands(this)).submit();
        Task.builder().execute(new DoorTicker(this)).delayTicks(10).intervalTicks(10).submit(this);
    }

    @Listener
    public void reload(GameReloadEvent event) {
        Task.builder().execute(this::refresh).delayTicks(5).submit(this);
    }

    public void add(Door door) {
        backing.add(door);
        Door.save(door, dir.resolve(door.getName() + ".nbt"));
    }

    public List<Door> getDoors() {
        return doors;
    }

    public void refresh() {
        try {
            List<Door> doors = Files.list(dir).parallel()
                    .map(Door::load)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(DoorsOfDurin::link)
                    .collect(Collectors.toList());
            backing.clear();
            backing.addAll(doors);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static boolean link(Door door) {
        if (door.getLink().isEmpty()) {
            return true;
        }

        Optional<Instance> instance = Sponge.getRegistry().getType(Instance.class, door.getLink());
        if (!instance.isPresent()) {
            return false;
        }

        instance.get().attach(door);

        return true;
    }
}
