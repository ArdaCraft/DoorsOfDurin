package me.ardacraft.dod.door;

import com.flowpowered.math.vector.Vector3i;
import me.dags.stopmotion.attachment.Attachment;
import me.dags.stopmotion.trigger.rule.Time;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.schematic.Schematic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Door implements Attachment, CatalogType {

    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;
    public static final int NOCHANGE = -1;

    private final String name;
    private final Time time;
    private final String world;
    private final String link;
    private final Vector3i origin;
    private final Schematic active;
    private final Schematic inactive;
    protected final DataView data;

    private transient final AtomicInteger state = new AtomicInteger(INACTIVE);
    private transient final AtomicBoolean lock = new AtomicBoolean(false);

    public Door(String name, DoorBuilder builder, DataView dataView) {
        this.name = name;
        link = builder.link;
        time = builder.time;
        world = builder.world;
        origin = builder.origin;
        active = builder.active;
        inactive = builder.inactive;
        data = dataView;
        state.set(builder.state);
    }

    @Override
    public String getId() {
        return getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean apply() {
        if (lock.get()) {
            return false;
        }

        Optional<Location<World>> origin = getOrigin();
        if (!origin.isPresent()) {
            return false;
        }

        if (time.test(origin.get().getExtent())) {
            applyActive(origin.get());
        } else {
            applyInactive(origin.get());
        }

        return true;
    }

    @Override
    public boolean remove() {
        if (lock.get()) {
            return false;
        }
        return getOrigin().map(this::removeEntities).orElse(false);
    }

    @Override
    public void lock(boolean lock) {
        this.lock.set(lock);
    }

    public int getState() {
        return state.get();
    }

    public String getLink() {
        return link;
    }

    public Time getTime() {
        return time;
    }

    public String getWorld() {
        return world;
    }

    public Vector3i getPosition() {
        return origin;
    }

    public Schematic getActive() {
        return active;
    }

    public Schematic getInactive() {
        return inactive;
    }

    public boolean isLocked() {
        return lock.get();
    }

    public int getStateChange(World world) {
        if (time.test(world)) {
            if (state.get() == INACTIVE) {
                return ACTIVE;
            }
            return NOCHANGE;
        }
        if (state.get() == ACTIVE) {
            return INACTIVE;
        }
        return NOCHANGE;
    }

    public Optional<Location<World>> getOrigin() {
        return Sponge.getServer().getWorld(world)
                .filter(this::isLoaded)
                .map(world -> world.getLocation(origin));
    }

    public void applyActive(Location<World> origin) {
        getActive().apply(origin, BlockChangeFlags.NONE);
        state.set(ACTIVE);
    }

    public void applyInactive(Location<World> origin) {
        removeEntities(origin);
        getInactive().apply(origin, BlockChangeFlags.NONE);
        state.set(INACTIVE);
    }

    private boolean removeEntities(Location<World> origin) {
        Vector3i pos = origin.getBlockPosition();
        Schematic schematic = getActive();
        AABB box = new AABB(pos.add(schematic.getBlockMin()), pos.add(schematic.getBlockMax()));
        for (Entity entity : origin.getExtent().getIntersectingEntities(box)) {
            if (entity instanceof Living) {
                continue;
            }
            entity.remove();
        }
        return true;
    }

    private boolean isLoaded(World world) {
        return world.isLoaded() && world.getChunkAtBlock(origin).isPresent();
    }

    public static Optional<Door> load(Path path) {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            DataContainer container = DataFormats.NBT.readFrom(in);
            Door door = DoorTranslator.INSTANCE.translate(container);
            return Optional.of(door);
        } catch (Throwable t) {
            t.printStackTrace();
            return Optional.empty();
        }
    }

    public static void save(Door door, Path path) {
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE))) {
            DataView view = DoorTranslator.INSTANCE.translate(door);
            DataFormats.NBT.writeTo(out, view);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
