package me.ardacraft.dod.door;

import com.google.common.reflect.TypeToken;
import me.dags.motion.trigger.rule.Time;
import me.dags.pitaya.config.Node;
import me.dags.pitaya.util.Translators;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.persistence.InvalidDataException;

public class DoorTranslator implements DataTranslator<Door> {

    public static final DoorTranslator INSTANCE = new DoorTranslator();

    private static final DataQuery NAME = DataQuery.of("name");
    private static final DataQuery WORLD = DataQuery.of("world");
    private static final DataQuery LINK = DataQuery.of("link");
    private static final DataQuery ORIGIN = DataQuery.of("origin");
    private static final DataQuery TIME = DataQuery.of("time");
    private static final DataQuery STATE = DataQuery.of("state");
    private static final DataQuery ACTIVE = DataQuery.of("active");
    private static final DataQuery INACTIVE = DataQuery.of("inactive");

    private static final DataQuery TIME_MIN = DataQuery.of("min");
    private static final DataQuery TIME_MAX = DataQuery.of("max");

    private static final TypeToken<Door> TOKEN = new TypeToken<Door>() {};

    @Override
    public TypeToken<Door> getToken() {
        return TOKEN;
    }

    @Override
    public String getId() {
        return "door";
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public Door translate(DataView view) throws InvalidDataException {
        DoorBuilder builder = new DoorBuilder();
        String name = Translators.getString(view, NAME);
        builder.world = Translators.getString(view, WORLD);
        builder.link = Translators.getString(view, LINK);
        builder.state = Translators.getInt(view, STATE);
        builder.time = time(view.getView(TIME).orElseThrow(() -> new InvalidDataException("Missing 'time'")));
        builder.origin = Translators.get(view, ORIGIN, DataTranslators.VECTOR_3_I);
        builder.active = Translators.get(view, ACTIVE, DataTranslators.SCHEMATIC);
        builder.inactive = Translators.get(view, INACTIVE, DataTranslators.SCHEMATIC);
        return new Door(name, builder);
    }

    @Override
    public DataContainer translate(Door door) throws InvalidDataException {
        return DataContainer.createNew()
                .set(NAME, door.getName())
                .set(WORLD, door.getWorld())
                .set(LINK, door.getLink())
                .set(STATE, door.getState())
                .set(TIME, time(door.getTime()))
                .set(ORIGIN, DataTranslators.VECTOR_3_I.translate(door.getPosition()))
                .set(ACTIVE, DataTranslators.SCHEMATIC.translate(door.getActive()))
                .set(INACTIVE, DataTranslators.SCHEMATIC.translate(door.getInactive()));
    }

    private DataContainer time(Time time) {
        Node node = Node.create();
        time.toNode(node);
        return DataTranslators.CONFIGURATION_NODE.translate(node.backing());
    }

    private Time time(DataView view) throws InvalidDataException {
        long min = view.getLong(TIME_MIN).orElseThrow(() -> new InvalidDataException("Missing 'min'"));
        long max = view.getLong(TIME_MAX).orElseThrow(() -> new InvalidDataException("Missing 'max'"));
        return new Time(min, max);
    }
}
