package me.ardacraft.dod;

import com.flowpowered.math.vector.Vector3i;
import me.dags.stopmotion.libs.pitaya.util.Translators;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SchemHelper {

    private static final DataQuery ENTITIES = DataQuery.of("Entities");
    private static final DataQuery TILEX = DataQuery.of("TileX");
    private static final DataQuery TILEY = DataQuery.of("TileY");
    private static final DataQuery TILEZ = DataQuery.of("TileZ");

    public static Schematic create(Location<World> origin, Vector3i pos1, Vector3i pos2) {
        ArchetypeVolume volume = origin.getExtent().createArchetypeVolume(pos1, pos2, origin.getBlockPosition());
        Schematic schematic = Schematic.builder()
                .volume(volume)
                .blockPalette(PaletteTypes.LOCAL_BLOCKS.create())
                .biomePalette(PaletteTypes.LOCAL_BIOMES.create())
                .build();
        return fix(origin.getBlockPosition(), schematic);
    }

    public static Schematic fix(Vector3i origin, Schematic schematic) {
        DataContainer container = DataTranslators.SCHEMATIC.translate(schematic);

        List<DataView> list = container.getViewList(SchemHelper.ENTITIES).orElse(Collections.emptyList());
        if (!list.isEmpty()) {
            List<DataView> out = new LinkedList<>();
            for (DataView view : list) {
                fixEntity(origin, view);
                out.add(view);
            }
            container.set(SchemHelper.ENTITIES, out);
        }

        return DataTranslators.SCHEMATIC.translate(container);
    }

    private static void fixEntity(Vector3i origin, DataView data) {
        List<Double> pos = data.getDoubleList(Queries.POSITION).orElse(Collections.emptyList());
        if (pos.size() != 3) {
            return;
        }

        try {
            double px = pos.get(0);
            double py = pos.get(1);
            double pz = pos.get(2);

            double pdx = px - (int) px;
            double pdy = py - (int) py;
            double pdz = pz - (int) pz;

            int tx = Translators.getInt(data, TILEX);
            int ty = Translators.getInt(data, TILEY);
            int tz = Translators.getInt(data, TILEZ);

            double x = tx - origin.getX() + pdx;
            double y = ty - origin.getY() + pdy;
            double z = tz - origin.getZ() + pdz;

            data.set(Queries.POSITION, Arrays.asList(x, y, z));
        } catch (InvalidDataException ignored) {

        }
    }
}
