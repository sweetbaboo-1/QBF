package org.peartcraft;

import net.sandrohc.schematic4j.schematic.LitematicaSchematic;

import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

public record ConfigData(
        int x1, int y1, int z1,
        int x2, int y2, int z2,
        String dimension,
        String creator,
        Set<String> blockSet,
        LitematicaSchematic litematic,
        Set<Integer> selectedRegionIndices
) {
    public ConfigData {
        selectedRegionIndices = Set.copyOf(Objects.requireNonNull(selectedRegionIndices));
    }

    // Backwards-compatible constructor: selecting all regions is the default.
    public ConfigData(int x1, int y1, int z1, int x2, int y2, int z2,
            String dimension, String creator, Set<String> blockSet,
            LitematicaSchematic litematic) {
        this(x1, y1, z1, x2, y2, z2, dimension, creator, blockSet, litematic,
                IntStream.range(0, litematic.regions().length).boxed()
                        .collect(Collectors.toUnmodifiableSet()));
    }
}
