package org.peartcraft;

/**
 * Converts Litematica block indexes and signed region sizes to local coordinates.
 *
 * <p>BlockStates are packed with X changing fastest, then Z, then Y.
 * A negative Size only changes the region's starting corner, not the
 * indexing order of BlockStates.
 */
public final class LitematicaCoordinates {
    private LitematicaCoordinates() {}

    /** Lowest occupied coordinate on one axis of a region. */
    public static int regionMin(int position, int signedSize) {
        if (signedSize == 0 || signedSize == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Invalid region size: " + signedSize);
        }
        return signedSize < 0 ? Math.addExact(position, signedSize + 1) : position;
    }

    /** Returns X, Y, Z offsets measured from a region's minimum corner. */
    public static int[] indexToLocal(int index, int signedX, int signedY, int signedZ) {
        if (signedX == 0 || signedY == 0 || signedZ == 0 ||
                signedX == Integer.MIN_VALUE || signedY == Integer.MIN_VALUE || signedZ == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Invalid region dimensions");
        }

        int sizeX = Math.abs(signedX);
        int sizeY = Math.abs(signedY);
        int sizeZ = Math.abs(signedZ);
        long layerSize = (long) sizeX * sizeZ;
        long volume = layerSize * sizeY;
        if (index < 0 || index >= volume) {
            throw new IndexOutOfBoundsException("Block index outside region: " + index);
        }

        int x = index % sizeX;
        int z = (index / sizeX) % sizeZ;
        int y = (int) (index / layerSize);
        return new int[]{x, y, z};
    }
}
