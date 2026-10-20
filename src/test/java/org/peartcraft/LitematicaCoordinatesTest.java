package org.peartcraft;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import net.sandrohc.schematic4j.schematic.LitematicaSchematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlock;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos;

class LitematicaCoordinatesTest {
    @Test void signedSizeUsesMinimumOccupiedCorner() {
        assertEquals(-213, LitematicaCoordinates.regionMin(0, -214));
        assertEquals(3, LitematicaCoordinates.regionMin(3, 119));
    }

    @Test void indexDecodingUsesPositiveDimensions() {
        assertArrayEquals(new int[]{0,0,0}, LitematicaCoordinates.indexToLocal(0, -214, -196, 119));
        assertArrayEquals(new int[]{213,195,118},
            LitematicaCoordinates.indexToLocal(214 * 196 * 119 - 1, -214, -196, 119));
        assertArrayEquals(new int[]{0,1,0},
            LitematicaCoordinates.indexToLocal(214 * 119, -214, -196, 119));
    }

    @Test void selectedBlocksRetainCorrectPositionAndXaeroAltitude() throws Exception {
        LitematicaSchematic schematic = new LitematicaSchematic();
        LitematicaSchematic.Region region = new LitematicaSchematic.Region();
        region.position = new SchematicBlockPos(0, 0, 0);
        region.size = new SchematicBlockPos(-2, -2, 2);
        region.blockStatePalette = new SchematicBlock[]{
            new SchematicBlock("minecraft:air"), new SchematicBlock("minecraft:chest")};
        region.blockStates = new int[]{1,0,0,0,0,0,0,1};
        schematic.regions = new LitematicaSchematic.Region[]{region};
        ConfigData config = new ConfigData(-5613,-55,2850,-5612,-54,2851,
            "overworld","XAERO",Set.of("minecraft:chest"),schematic);
        BlockFinder.BlockSearchResult found = BlockFinder.findBlockPositions(config);
        assertEquals(List.of(new SchematicBlockPos(0,0,0),
                             new SchematicBlockPos(1,1,1)),found.positions());
        List<String> waypoints = new XaeroMapWaypointCreator().createWaypoints(
            found.positions(),found.blockStates(),config);
        assertTrue(waypoints.get(0).contains(":-5613:-55:2850:"));
        assertTrue(waypoints.get(1).contains(":-5612:-54:2851:"));
    }

    @Test void allRegionsAreScannedWithSharedOrigin() throws Exception {
        LitematicaSchematic schematic = new LitematicaSchematic();
        LitematicaSchematic.Region left = new LitematicaSchematic.Region();
        left.position = new SchematicBlockPos(-2,0,0);
        left.size = new SchematicBlockPos(1,1,1);
        left.blockStatePalette = new SchematicBlock[]{new SchematicBlock("minecraft:chest")};
        left.blockStates = new int[]{0};
        LitematicaSchematic.Region right = new LitematicaSchematic.Region();
        right.position = new SchematicBlockPos(3,0,0);
        right.size = new SchematicBlockPos(1,1,1);
        right.blockStatePalette = new SchematicBlock[]{new SchematicBlock("minecraft:chest")};
        right.blockStates = new int[]{0};
        schematic.regions = new LitematicaSchematic.Region[]{left,right};
        ConfigData config = new ConfigData(100,10,200,105,10,200,
            "overworld","XAERO",Set.of("minecraft:chest"),schematic);
        BlockFinder.BlockSearchResult found = BlockFinder.findBlockPositions(config);
        assertEquals(List.of(new SchematicBlockPos(0,0,0),
                             new SchematicBlockPos(5,0,0)),found.positions());
    }
}
