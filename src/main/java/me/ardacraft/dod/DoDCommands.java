package me.ardacraft.dod;

import com.flowpowered.math.vector.Vector3i;
import me.ardacraft.dod.door.DoorBuilder;
import me.dags.motion.instance.Instance;
import me.dags.motion.trigger.rule.Time;
import me.dags.motion.util.recorder.PosRecorder;
import me.dags.pitaya.cache.Cache;
import me.dags.pitaya.command.annotation.Command;
import me.dags.pitaya.command.annotation.Description;
import me.dags.pitaya.command.annotation.Permission;
import me.dags.pitaya.command.annotation.Src;
import me.dags.pitaya.command.fmt.Fmt;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;

import java.util.concurrent.TimeUnit;

public class DoDCommands extends Cache<DoorBuilder> {

    private final DoorsOfDurin plugin;

    public DoDCommands(DoorsOfDurin plugin) {
        super(5, TimeUnit.MINUTES, DoorBuilder::new);
        this.plugin = plugin;
    }

    @Command("dod wand")
    @Permission("dod.command.wand")
    @Description("Bind a selection wand to your held item")
    public void wand(@Src Player player) {
        PosRecorder.create(player).onPass(r -> {
            Fmt.info("Successfully created selection wand").tell(player);
        }).onFail(message -> {
            Fmt.error(message).tell(player);
        });
    }

    @Command("dod active")
    @Permission("dod.command.active")
    @Description("Create the schematic that is used while the door is active")
    public void active(@Src Player player) {
        PosRecorder.getSelection(player).ifPresent((pos1, pos2) -> {
            DoorBuilder builder = must(player);
            Vector3i origin = player.getLocation().getBlockPosition();
            ArchetypeVolume volume = player.getWorld().createArchetypeVolume(pos1, pos2, origin);
            builder.active = Schematic.builder()
                    .volume(volume)
                    .blockPalette(PaletteTypes.LOCAL_BLOCKS.create())
                    .biomePalette(PaletteTypes.LOCAL_BIOMES.create())
                    .build();
            Fmt.info("Copied volume ").stress(pos1).info(" to ").stress(pos2).tell(player);
        }).ifAbsent(() -> {
            Fmt.info("You must select a volume first").tell(player);
        });
    }

    @Command("dod inactive")
    @Permission("dod.command.inactive")
    @Description("Create the schematic that is used while the door is inactive")
    public void inactive(@Src Player player) {
        PosRecorder.getSelection(player).ifPresent((pos1, pos2) -> {
            DoorBuilder builder = must(player);
            Vector3i origin = player.getLocation().getBlockPosition();
            ArchetypeVolume volume = player.getWorld().createArchetypeVolume(pos1, pos2, origin);
            builder.inactive = Schematic.builder()
                    .volume(volume)
                    .blockPalette(PaletteTypes.LOCAL_BLOCKS.create())
                    .biomePalette(PaletteTypes.LOCAL_BIOMES.create())
                    .build();
            Fmt.info("Copied volume ").stress(pos1).info(" to ").stress(pos2).tell(player);
        }).ifAbsent(() -> {
            Fmt.info("You must select a volume first").tell(player);
        });
    }

    @Command("dod origin")
    @Permission("dod.command.origin")
    @Description("Set the paste position for the door")
    public void origin(@Src Player player) {
        DoorBuilder builder = must(player);
        builder.world = player.getWorld().getName();
        builder.origin = player.getLocation().getBlockPosition();
        Fmt.info("Set origin to ").stress(builder.origin).tell(player);
    }

    @Command("dod time <from> <to>")
    @Permission("dod.command.time")
    @Description("Create a time rule for the door")
    public void time(@Src Player player, long from, long to) {
        DoorBuilder builder = must(player);
        builder.time = new Time(from, to);
        Fmt.info("Set the time rule to ").stress(builder.time).tell(player);
    }

    @Command("dod link <animation>")
    @Permission("dod.command.link")
    @Description("Link the door to an animation")
    public void link(@Src Player player, Instance instance) {
        DoorBuilder builder = must(player);
        builder.link = instance.getId();
        Fmt.info("Linked to animation ").stress(builder.link).tell(player);
    }

    @Command("dod save <name>")
    @Permission("dod.command.save")
    @Description("Save the door with the given name")
    public void save(@Src Player player, String name) {
        drain(player, "You are not building a door").flatMap(builder -> builder.build(name)).onPass(door -> {
            if (plugin.add(door)) {
                Fmt.info("Saved door ").stress(door.getName()).tell(player);
            } else {
                Fmt.error("Failed to link door to it's animation").tell(player);
            }
        }).onFail(message -> {
            Fmt.error(message).tell(player);
        });
    }

    @Command("dod clear")
    @Permission("dod.command.clear")
    @Description("Clear your current door builder")
    public void clear(@Src Player player) {
        drain(player, "You are not building a door").onPass(b -> {
            Fmt.info("Cleared your current door builder").tell(player);
        }).onFail(message -> {
            Fmt.error(message).tell(player);
        });
    }

    @Command("dod reload")
    @Permission("dod.command.reload")
    @Description("Reload the DoD plugin")
    public void reload(@Src Player player) {
        plugin.reload(null);
        Fmt.info("Reloaded DoD").tell(player);
    }
}
