package org.peartcraft;

import java.util.Locale;

public enum Dimension {
    OVERWORLD("overworld", 1),
    NETHER("the_nether", 8),
    END("the_end", 1);

    private final String id;
    private final int multiplier;

    Dimension(String id, int multiplier) {
        this.id = id;
        this.multiplier = multiplier;
    }

    public String getId() {
        return id;
    }

    /**
     * VoxelMap stores X/Z in overworld-equivalent coordinates. Nether
     * waypoints therefore use a factor of eight in its .points file.
     */
    public int getMultiplier() {
        return multiplier;
    }

    /**
     * Resolve the dimension IDs used by ConfigGui as well as old display
     * labels from previous versions. Unknown values must not silently
     * fall back to overworld.
     */
    public static Dimension fromDisplayName(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Dimension cannot be null");
        }

        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "overworld", "minecraft:overworld" -> OVERWORLD;
            case "nether", "the_nether", "minecraft:the_nether" -> NETHER;
            case "end", "the_end", "minecraft:the_end" -> END;
            default -> throw new IllegalArgumentException("Unknown dimension: " + value);
        };
    }
}
