package com.pg85.otg.paper.carver;

import com.pg85.otg.paper.gen.OTGNoiseChunkGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.carver.CarvingContext;

public class OTGCarvingContext extends CarvingContext {
    private final OTGNoiseChunkGenerator generator;
    private final RegistryAccess registryAccess;
    private final NoiseChunk noiseChunk;

    public OTGCarvingContext(OTGNoiseChunkGenerator chunkGenerator, RegistryAccess registryManager, LevelHeightAccessor heightLimitView, NoiseChunk chunkNoiseSampler, @org.jetbrains.annotations.Nullable net.minecraft.world.level.Level level) { // Paper
        super(null, registryManager, heightLimitView, chunkNoiseSampler, level);
        this.generator = chunkGenerator;
        this.registryAccess = registryManager;
        this.noiseChunk = chunkNoiseSampler;
    }
}
