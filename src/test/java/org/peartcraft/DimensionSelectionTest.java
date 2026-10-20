package org.peartcraft;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import net.sandrohc.schematic4j.schematic.LitematicaSchematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos;
import org.junit.jupiter.api.Test;

class DimensionSelectionTest {
    private static ConfigData config(String dimension) {
        // A two-corner placement, one known target block at local (1,2,3).
        return new ConfigData(-16,64,32,-14,66,36,
                dimension,"VOXEL",Set.of("minecraft:chest"),
                new LitematicaSchematic(),Set.of());
    }

    @Test void guiDimensionIdsAreRecognized() {
        assertEquals(Dimension.OVERWORLD, Dimension.fromDisplayName("overworld"));
        assertEquals(Dimension.NETHER, Dimension.fromDisplayName("the_nether"));
        assertEquals(Dimension.END, Dimension.fromDisplayName("the_end"));
    }

    @Test void legacyDimensionNamesContinueToWork() {
        assertEquals(Dimension.OVERWORLD, Dimension.fromDisplayName("Overworld"));
        assertEquals(Dimension.NETHER, Dimension.fromDisplayName("Nether"));
        assertEquals(Dimension.END, Dimension.fromDisplayName("End"));
        assertEquals(Dimension.NETHER, Dimension.fromDisplayName("minecraft:the_nether"));
    }

    @Test void unknownDimensionsMustNotSilentlyBecomeOverworld() {
        assertThrows(IllegalArgumentException.class, () -> Dimension.fromDisplayName("the_void"));
        assertThrows(IllegalArgumentException.class, () -> Dimension.fromDisplayName(null));
    }

    @Test void voxelMapNetherUsesOverworldScaleAndCorrectDimension() {
        String output = new VoxelMapWaypointCreator().createWaypoints(
                List.of(new SchematicBlockPos(1,2,3)),
                List.of("minecraft:chest"), config("the_nether")).get(0);
        assertTrue(output.contains("x:-120,z:280,y:66,"));
        assertTrue(output.endsWith("dimensions:the_nether#"));
    }

    @Test void voxelMapEndAndOverworldKeepActualCoordinates() {
        for (String dimension : List.of("the_end","overworld")) {
            String output = new VoxelMapWaypointCreator().createWaypoints(
                    List.of(new SchematicBlockPos(1,2,3)),
                    List.of("minecraft:chest"), config(dimension)).get(0);
            assertTrue(output.contains("x:-15,z:35,y:66,"), output);
            assertTrue(output.endsWith("dimensions:" + dimension + "#"), output);
        }
    }

    @Test void xaeroCoordinatesRemainDimensionLocal() {
        for (String dimension : List.of("overworld","the_nether","the_end")) {
            String output = new XaeroMapWaypointCreator().createWaypoints(
                    List.of(new SchematicBlockPos(1,2,3)),
                    List.of("minecraft:chest"), config(dimension)).get(0);
            assertTrue(output.contains(":-15:66:35:"), output);
        }
    }
}
