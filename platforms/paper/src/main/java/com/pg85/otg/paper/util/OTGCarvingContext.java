package com.pg85.otg.paper.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import org.jetbrains.annotations.Nullable;

public class OTGCarvingContext extends CarvingContext {
    public OTGCarvingContext(ChunkGenerator chunkGenerator, RegistryAccess registryManager, LevelHeightAccessor heightLimitView, NoiseChunk chunkNoiseSampler, @Nullable Level level) {
        super((NoiseBasedChunkGenerator)chunkGenerator, registryManager, heightLimitView, chunkNoiseSampler, level);
    }
}
