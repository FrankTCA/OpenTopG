package com.pg85.otg.paper.carver;

import com.pg85.otg.paper.gen.OTGNoiseChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

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
