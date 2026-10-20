package org.peartcraft;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import net.sandrohc.schematic4j.schematic.LitematicaSchematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlock;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos;

class RegionSelectionTest {
    private static LitematicaSchematic.Region region(int x, String[] palette, int... blockIds) {
        LitematicaSchematic.Region result = new LitematicaSchematic.Region();
        result.position = new SchematicBlockPos(x, 0, 0);
        result.size = new SchematicBlockPos(blockIds.length, 1, 1);
        result.blockStatePalette = new SchematicBlock[palette.length];
        for (int i = 0; i < palette.length; i++) {
            result.blockStatePalette[i] = new SchematicBlock(palette[i]);
        }
        result.blockStates = blockIds;
        return result;
    }

    private static LitematicaSchematic testSchematic() {
        LitematicaSchematic schematic = new LitematicaSchematic();
        // The first region establishes the global minimum X even if excluded.
        // Regions 2 and 3 overlap exactly, including a conflicting block name.
        schematic.regions = new LitematicaSchematic.Region[]{
            region(-5, new String[]{"minecraft:chest"}, 0),
            region(0, new String[]{"minecraft:chest"}, 0, 0),
            region(0, new String[]{"minecraft:barrel", "minecraft:chest"}, 0, 1)
        };
        return schematic;
    }

    private static ConfigData config(Set<Integer> selected) {
        return new ConfigData(100, 10, 200, 106, 10, 200,
            "overworld", "XAERO", Set.of("minecraft:chest", "minecraft:barrel"),
            testSchematic(), selected);
    }

    @Test void overlappingRegionsProduceOneWaypointPerBlockPosition() throws Exception {
        ConfigData data = config(Set.of(0, 1, 2));
        BlockFinder.BlockSearchResult result = BlockFinder.findBlockPositions(data);
        assertEquals(List.of(new SchematicBlockPos(0,0,0),
                new SchematicBlockPos(5,0,0), new SchematicBlockPos(6,0,0)),
                result.positions());
        assertEquals(List.of("minecraft:chest","minecraft:chest","minecraft:chest"),
                result.blockStates()); // first selected region wins in overlaps
        assertEquals(3, new XaeroMapWaypointCreator().createWaypoints(
            result.positions(), result.blockStates(), data).size());
        assertEquals(3, new VoxelMapWaypointCreator().createWaypoints(
            result.positions(), result.blockStates(), data).size());
    }

    @Test void deselectingRegionDoesNotShiftWorldCoordinates() throws Exception {
        ConfigData data = config(Set.of(2));
        BlockFinder.BlockSearchResult result = BlockFinder.findBlockPositions(data);
        assertEquals(List.of(new SchematicBlockPos(5,0,0), new SchematicBlockPos(6,0,0)),
                result.positions());
        assertEquals(List.of("minecraft:barrel", "minecraft:chest"),
                result.blockStates());
        List<String> xaero = new XaeroMapWaypointCreator().createWaypoints(
            result.positions(), result.blockStates(), data);
        assertTrue(xaero.get(0).contains(":105:10:200:"));
        assertTrue(xaero.get(1).contains(":106:10:200:"));
    }

    @Test void allOrNoRegionsSelected() throws Exception {
        // Legacy constructor continues to select all regions.
        ConfigData defaultConfig = new ConfigData(100,10,200,106,10,200,
                "overworld","XAERO", Set.of("minecraft:chest", "minecraft:barrel"),
                testSchematic());
        assertEquals(3, BlockFinder.findBlockPositions(defaultConfig).positions().size());
        assertTrue(BlockFinder.findBlockPositions(config(Set.of())).positions().isEmpty());
    }

    @Test void onlyOneOfTwoOverlappingRegionsCanBeSelected() throws Exception {
        ConfigData one = config(Set.of(1));
        BlockFinder.BlockSearchResult found = BlockFinder.findBlockPositions(one);
        assertEquals(2, found.positions().size());
        assertEquals(List.of("minecraft:chest", "minecraft:chest"), found.blockStates());
    }
}
