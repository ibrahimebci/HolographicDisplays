/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.holographicdisplays.plugin.hologram.base;

import me.filoghost.fcommons.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class HologramPosition {

    private @NotNull ImmutablePosition position;
    private @Nullable World world;
    private int chunkX, chunkZ;
    private @NotNull ChunkLoadState chunkLoadState;

    HologramPosition(@NotNull ImmutablePosition position) {
        Preconditions.notNull(position, "position");
        this.position = position;
        this.world = Bukkit.getWorld(position.getWorldName());
        this.chunkX = getChunkCoordinate(position.getX());
        this.chunkZ = getChunkCoordinate(position.getZ());
        this.chunkLoadState = ChunkLoadState.UNKNOWN;
    }

    final void set(String worldName, double x, double y, double z) {
        boolean worldChanged = !position.isInWorld(worldName);
        int chunkX = getChunkCoordinate(x);
        int chunkZ = getChunkCoordinate(z);

        position = new ImmutablePosition(worldName, x, y, z);

        if (worldChanged || this.chunkX != chunkX || this.chunkZ != chunkZ) {
            if (worldChanged) {
                this.world = Bukkit.getWorld(worldName);
            }
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.chunkLoadState = ChunkLoadState.UNKNOWN;
        }
    }

    private int getChunkCoordinate(double positionCoordinate) {
        return Location.locToBlock(positionCoordinate) >> 4;
    }

    void onWorldLoad(World world) {
        if (position.isInWorld(world)) {
            this.world = world;
            chunkLoadState = ChunkLoadState.UNKNOWN;
        }
    }

    void onWorldUnload(World world) {
        if (position.isInWorld(world)) {
            this.world = null;
            chunkLoadState = ChunkLoadState.NOT_LOADED;
        }
    }

    void onChunkLoad(Chunk chunk) {
        if (isInChunk(chunk)) {
            chunkLoadState = ChunkLoadState.LOADED;
        }
    }

    void onChunkUnload(Chunk chunk) {
        if (isInChunk(chunk)) {
            chunkLoadState = ChunkLoadState.NOT_LOADED;
        }
    }

    private boolean isInChunk(Chunk chunk) {
        return world != null && world == chunk.getWorld() && chunkX == chunk.getX() && chunkZ == chunk.getZ();
    }

    boolean isChunkLoaded() {
        // Compute state if unknown
        if (chunkLoadState == ChunkLoadState.UNKNOWN) {
            if (world != null && world.isChunkLoaded(chunkX, chunkZ)) {
                chunkLoadState = ChunkLoadState.LOADED;
            } else {
                chunkLoadState = ChunkLoadState.NOT_LOADED;
            }
        }

        return chunkLoadState == ChunkLoadState.LOADED;
    }

    @Nullable World getWorldIfLoaded() {
        return world;
    }

    @NotNull ImmutablePosition getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return position.toString();
    }


    private enum ChunkLoadState {

        LOADED,
        NOT_LOADED,
        UNKNOWN

    }

}
