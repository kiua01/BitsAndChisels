package io.github.coolmineman.bitsandchisels.buildmode;

/** Effortless-style build modes available from the Bits radial menu. */
public enum BuildMode {
    DISABLED("Disabled", 0),
    SINGLE("Single", 1),
    LINE("Line", 2),
    WALL("Wall", 2),
    FLOOR("Floor", 2),
    CUBE("Cube", 3),
    DIAGONAL_LINE("Diagonal Line", 2),
    CIRCLE("Circle", 2),
    SPHERE("Sphere", 2);

    public final String label;
    /** Clicks needed to finish a placement (0 = vanilla). */
    public final int clicks;

    BuildMode(String label, int clicks) {
        this.label = label;
        this.clicks = clicks;
    }
}
