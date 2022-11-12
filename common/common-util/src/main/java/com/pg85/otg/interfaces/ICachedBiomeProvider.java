package com.pg85.otg.interfaces;

import com.pg85.otg.util.ChunkCoordinate;

public interface ICachedBiomeProvider {
    IBiomeConfig[] getBiomeConfigsForChunk(ChunkCoordinate chunkCoordinate);

    IBiome[] getBiomesForChunk(ChunkCoordinate chunkCoordinate);

    IBiome[] getBiomesForChunks(ChunkCoordinate chunkCoord, int widthHeightInChunks);

    IBiomeConfig getBiomeConfig(int x, int z, boolean cacheChunk);

    IBiomeConfig getBiomeConfig(int x, int z);

    IBiome getBiome(int x, int z);

    IBiomeConfig[] getNoiseBiomeConfigsForRegion(int noiseStartX, int noiseStartZ, int widthHeight);

    IBiomeConfig getNoiseBiomeConfig(int x, int z, boolean cacheChunk);

    IBiome getNoiseBiome(int x, int z);
}
