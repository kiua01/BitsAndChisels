package io.github.coolmineman.bitsandchisels.buildmode;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Generates block positions for Effortless-style shapes. */
public final class ShapeGenerator {
    private static final int MAX_BLOCKS = 4096; // safety for survival

    private ShapeGenerator() {}

    public static List<BlockPos> generate(BuildMode mode, List<BlockPos> clicks) {
        if (clicks.isEmpty()) return List.of();
        return switch (mode) {
            case DISABLED -> List.of();
            case SINGLE -> List.of(clicks.get(0));
            case LINE -> clicks.size() >= 2 ? line(clicks.get(0), clicks.get(1)) : List.of();
            case WALL -> clicks.size() >= 2 ? wall(clicks.get(0), clicks.get(1)) : List.of();
            case FLOOR -> clicks.size() >= 2 ? floor(clicks.get(0), clicks.get(1)) : List.of();
            case CUBE -> clicks.size() >= 3 ? cube(clicks.get(0), clicks.get(1), clicks.get(2))
                : clicks.size() >= 2 ? cube(clicks.get(0), clicks.get(1), clicks.get(1)) : List.of();
            case DIAGONAL_LINE -> clicks.size() >= 2 ? diagonalLine(clicks.get(0), clicks.get(1)) : List.of();
            case CIRCLE -> clicks.size() >= 2 ? circle(clicks.get(0), clicks.get(1)) : List.of();
            case SPHERE -> clicks.size() >= 2 ? sphere(clicks.get(0), clicks.get(1)) : List.of();
        };
    }

    private static List<BlockPos> line(BlockPos a, BlockPos b) {
        // Axis-aligned line (longest axis)
        int dx = b.getX() - a.getX();
        int dy = b.getY() - a.getY();
        int dz = b.getZ() - a.getZ();
        int adx = Math.abs(dx), ady = Math.abs(dy), adz = Math.abs(dz);
        List<BlockPos> out = new ArrayList<>();
        if (adx >= ady && adx >= adz) {
            int step = dx > 0 ? 1 : -1;
            for (int x = a.getX(); x != b.getX() + step; x += step) {
                out.add(new BlockPos(x, a.getY(), a.getZ()));
                if (out.size() >= MAX_BLOCKS) break;
            }
        } else if (ady >= adx && ady >= adz) {
            int step = dy > 0 ? 1 : -1;
            for (int y = a.getY(); y != b.getY() + step; y += step) {
                out.add(new BlockPos(a.getX(), y, a.getZ()));
                if (out.size() >= MAX_BLOCKS) break;
            }
        } else {
            int step = dz > 0 ? 1 : -1;
            for (int z = a.getZ(); z != b.getZ() + step; z += step) {
                out.add(new BlockPos(a.getX(), a.getY(), z));
                if (out.size() >= MAX_BLOCKS) break;
            }
        }
        return out;
    }

    private static List<BlockPos> diagonalLine(BlockPos a, BlockPos b) {
        // 3D Bresenham-ish
        int x0 = a.getX(), y0 = a.getY(), z0 = a.getZ();
        int x1 = b.getX(), y1 = b.getY(), z1 = b.getZ();
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0), dz = Math.abs(z1 - z0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, sz = z0 < z1 ? 1 : -1;
        int dm = Math.max(dx, Math.max(dy, dz));
        List<BlockPos> out = new ArrayList<>();
        int x = x0, y = y0, z = z0;
        int ex = 0, ey = 0, ez = 0;
        for (int i = 0; i <= dm && out.size() < MAX_BLOCKS; i++) {
            out.add(new BlockPos(x, y, z));
            ex += dx; ey += dy; ez += dz;
            if (ex * 2 >= dm) { x += sx; ex -= dm; }
            if (ey * 2 >= dm) { y += sy; ey -= dm; }
            if (ez * 2 >= dm) { z += sz; ez -= dm; }
        }
        return out;
    }

    private static List<BlockPos> wall(BlockPos a, BlockPos b) {
        int minX = Math.min(a.getX(), b.getX()), maxX = Math.max(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY()), maxY = Math.max(a.getY(), b.getY());
        int minZ = Math.min(a.getZ(), b.getZ()), maxZ = Math.max(a.getZ(), b.getZ());
        // Flatten the thinner horizontal axis
        int dx = maxX - minX, dz = maxZ - minZ;
        List<BlockPos> out = new ArrayList<>();
        if (dx <= dz) {
            int x = a.getX(); // keep start X plane if vertical wall along Z
            // use average plane of thinner axis
            x = (a.getX() + b.getX()) / 2;
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    out.add(new BlockPos(x, y, z));
                    if (out.size() >= MAX_BLOCKS) return out;
                }
            }
        } else {
            int z = (a.getZ() + b.getZ()) / 2;
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    out.add(new BlockPos(x, y, z));
                    if (out.size() >= MAX_BLOCKS) return out;
                }
            }
        }
        return out;
    }

    private static List<BlockPos> floor(BlockPos a, BlockPos b) {
        int minX = Math.min(a.getX(), b.getX()), maxX = Math.max(a.getX(), b.getX());
        int minZ = Math.min(a.getZ(), b.getZ()), maxZ = Math.max(a.getZ(), b.getZ());
        int y = a.getY();
        List<BlockPos> out = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                out.add(new BlockPos(x, y, z));
                if (out.size() >= MAX_BLOCKS) return out;
            }
        }
        return out;
    }

    private static List<BlockPos> cube(BlockPos a, BlockPos b, BlockPos c) {
        int minX = Math.min(a.getX(), Math.min(b.getX(), c.getX()));
        int maxX = Math.max(a.getX(), Math.max(b.getX(), c.getX()));
        int minY = Math.min(a.getY(), Math.min(b.getY(), c.getY()));
        int maxY = Math.max(a.getY(), Math.max(b.getY(), c.getY()));
        int minZ = Math.min(a.getZ(), Math.min(b.getZ(), c.getZ()));
        int maxZ = Math.max(a.getZ(), Math.max(b.getZ(), c.getZ()));
        List<BlockPos> out = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    out.add(new BlockPos(x, y, z));
                    if (out.size() >= MAX_BLOCKS) return out;
                }
            }
        }
        return out;
    }

    private static List<BlockPos> circle(BlockPos center, BlockPos edge) {
        int r = (int) Math.round(Math.sqrt(center.distSqr(edge)));
        if (r < 1) r = 1;
        if (r > 64) r = 64;
        int y = center.getY();
        Set<BlockPos> set = new HashSet<>();
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                if (x * x + z * z <= r * r + r) { // slight fill
                    set.add(new BlockPos(center.getX() + x, y, center.getZ() + z));
                    if (set.size() >= MAX_BLOCKS) return new ArrayList<>(set);
                }
            }
        }
        return new ArrayList<>(set);
    }

    private static List<BlockPos> sphere(BlockPos center, BlockPos edge) {
        int r = (int) Math.round(Math.sqrt(center.distSqr(edge)));
        if (r < 1) r = 1;
        if (r > 32) r = 32;
        Set<BlockPos> set = new HashSet<>();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (x * x + y * y + z * z <= r * r) {
                        set.add(new BlockPos(center.getX() + x, center.getY() + y, center.getZ() + z));
                        if (set.size() >= MAX_BLOCKS) return new ArrayList<>(set);
                    }
                }
            }
        }
        return new ArrayList<>(set);
    }
}
