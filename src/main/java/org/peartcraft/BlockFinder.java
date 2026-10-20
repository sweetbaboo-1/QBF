package org.peartcraft;

import net.sandrohc.schematic4j.schematic.LitematicaSchematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos;

import java.util.*;
import java.util.concurrent.*;

/**
 * Finds selected blocks in enabled Litematica regions and returns local
 * coordinates relative to the minimum corner of the COMPLETE schematic.
 * Every position is exported at most once, including overlapping regions.
 */
public class BlockFinder {
    public record BlockSearchResult(List<SchematicBlockPos> positions, List<String> blockStates) {}

    public static BlockSearchResult findBlockPositions(ConfigData data)
            throws InterruptedException, ExecutionException {
        LitematicaSchematic.Region[] regions = data.litematic().regions();
        if (regions.length == 0) {
            return new BlockSearchResult(List.of(), List.of());
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        for (LitematicaSchematic.Region region : regions) {
            minX = Math.min(minX, LitematicaCoordinates.regionMin(region.position.x, region.size.x));
            minY = Math.min(minY, LitematicaCoordinates.regionMin(region.position.y, region.size.y));
            minZ = Math.min(minZ, LitematicaCoordinates.regionMin(region.position.z, region.size.z));
        }
        final int originX = minX, originY = minY, originZ = minZ;

        // LinkedHashMap preserves stable scan order and prevents duplicate coordinates.
        // If selected regions overlap, the first matching block at a position wins.
        Map<SchematicBlockPos, String> uniqueBlocks = new LinkedHashMap<>();
        for (int regionIndex = 0; regionIndex < regions.length; regionIndex++) {
            if (!data.selectedRegionIndices().contains(regionIndex)) continue;
            LitematicaSchematic.Region region = regions[regionIndex];
            Set<Integer> selectedIds = new HashSet<>();
            for (int p = 0; p < region.blockStatePalette.length; p++) {
                if (data.blockSet().contains(region.blockStatePalette[p].block())) {
                    selectedIds.add(p);
                }
            }
            if (selectedIds.isEmpty()) continue;

            int regionX = LitematicaCoordinates.regionMin(region.position.x, region.size.x);
            int regionY = LitematicaCoordinates.regionMin(region.position.y, region.size.y);
            int regionZ = LitematicaCoordinates.regionMin(region.position.z, region.size.z);

            for (int i = 0; i < region.blockStates.length; i++) {
                int paletteId = region.blockStates[i];
                if (!selectedIds.contains(paletteId)) continue;
                int[] local = LitematicaCoordinates.indexToLocal(i,
                        region.size.x, region.size.y, region.size.z);
                SchematicBlockPos pos = new SchematicBlockPos(
                        regionX - originX + local[0],
                        regionY - originY + local[1],
                        regionZ - originZ + local[2]);
                uniqueBlocks.putIfAbsent(pos, region.blockStatePalette[paletteId].block());
            }
        }
        return new BlockSearchResult(new ArrayList<>(uniqueBlocks.keySet()),
                new ArrayList<>(uniqueBlocks.values()));
    }
}
