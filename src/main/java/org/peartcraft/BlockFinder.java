package org.peartcraft;

import net.sandrohc.schematic4j.schematic.LitematicaSchematic;
import net.sandrohc.schematic4j.schematic.types.SchematicBlockPos;

import java.util.*;
import java.util.concurrent.*;

public class BlockFinder {

    public static List<SchematicBlockPos> findBlockPositions(ConfigData data) throws InterruptedException, ExecutionException {

        LitematicaSchematic.Region region = data.litematic().regions()[0];

        Set<Integer> blackListedPalletIds = new HashSet<>();
        for (int i = 0; i < region.blockStatePalette.length; i++) {
            if (data.blockSet().contains(region.blockStatePalette[i].block())) {
                blackListedPalletIds.add(i);
            }
        }

        int length = region.blockStates.length;
        int numThreads = Runtime.getRuntime().availableProcessors();
        int chunkSize = (int) Math.ceil(length / (double) numThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<List<SchematicBlockPos>>> futures = new ArrayList<>();

        for (int t = 0; t < numThreads; t++) {
            final int start = t * chunkSize;
            final int end = Math.min(start + chunkSize, length);

            futures.add(executor.submit(() -> {
                List<SchematicBlockPos> partial = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    if (blackListedPalletIds.contains(region.blockStates[i])) {
                        partial.add(region.indexToPos(i));
                    }
                }
                return partial;
            }));
        }

        List<SchematicBlockPos> blockPositions = new ArrayList<>();
        for (Future<List<SchematicBlockPos>> future : futures) {
            blockPositions.addAll(future.get());
        }

        executor.shutdown();

        return blockPositions;
    }
}

