package com.pg85.otg.fabric.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pg85.otg.config.biome.BiomeConfigFinder;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.config.dimensions.DimensionConfig;
import com.pg85.otg.core.gen.OTGChunkDecorator;
import com.pg85.otg.core.gen.OTGChunkGenerator;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.customobject.structures.CustomStructureCache;
import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.fabric.biome.FabricBiome;
import com.pg85.otg.fabric.biome.OTGBiomeProvider;
import com.pg85.otg.fabric.presets.FabricPresetLoader;
import com.pg85.otg.interfaces.*;
import com.pg85.otg.util.ChunkCoordinate;
import com.pg85.otg.util.gen.ChunkBuffer;
import com.pg85.otg.util.gen.DecorationArea;
import com.pg85.otg.util.gen.JigsawStructureData;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.minecraft.StructureType;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OTGNoiseChunkGenerator extends ChunkGenerator
{
    // Create a codec to serialise/deserialise OTGNoiseChunkGenerator
    public static final Codec<OTGNoiseChunkGenerator> CODEC = RecordCodecBuilder.create(
            (p_236091_0_) -> p_236091_0_
                    .group(
                            Codec.STRING.fieldOf("preset_folder_name").forGetter(p -> p.getPreset().getFolderName()),
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(p -> p.biomeSource),
                            RegistryOps.retrieveRegistry(Registry.STRUCTURE_SET_REGISTRY).forGetter(p -> p.structureSets),
                            RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(p -> p.noises),
                            Codec.LONG.fieldOf("seed").stable().forGetter(p -> p.worldSeed),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(p -> p.generatorSettingsHolder)
                    ).apply(
                            p_236091_0_,
                            p_236091_0_.stable(OTGNoiseChunkGenerator::new)
                    )
    );

    private final Holder<NoiseGeneratorSettings> generatorSettingsHolder;
    private final long worldSeed;
    private final int noiseHeight;
    protected final BlockState defaultBlock;
    protected final BlockState defaultFluid;

    private final ShadowChunkGenerator shadowChunkGenerator;
    public final OTGChunkGenerator internalGenerator;
    private final OTGChunkDecorator chunkDecorator;
    private final Preset preset;
    private final NoiseRouter router;
    private final HashMap<StructureType, StructurePlacement> structurePlacementSettings;
    //protected final WorldgenRandom random;

    // TODO: Move this to WorldLoader when ready?
    private CustomStructureCache structureCache;

    private final Climate.Sampler sampler;
    private final Registry<NormalNoise.NoiseParameters> noises;
    private ChunkCoordinate fixBiomesForChunk;

    public OTGNoiseChunkGenerator (BiomeSource biomeSource, long seed, Registry<StructureSet> structureSetRegistry, Registry<NormalNoise.NoiseParameters> noiseRegistry, Holder<NoiseGeneratorSettings> generatorSettings)
    {
        this("default", biomeSource, structureSetRegistry, noiseRegistry, seed, generatorSettings);
    }

    public OTGNoiseChunkGenerator (String presetName, BiomeSource biomeSource, Registry<StructureSet> structureSetRegistry, Registry<NormalNoise.NoiseParameters> noiseRegistry, long seed, Holder<NoiseGeneratorSettings> generatorSettings)
    {
        this(presetName, biomeSource, biomeSource, structureSetRegistry, noiseRegistry, seed, generatorSettings);
    }

    // Vanilla has two biome sources, where the first is population and the second is runtime. Don't know the practical difference this makes.
    private OTGNoiseChunkGenerator (String presetFolderName, BiomeSource populationSource, BiomeSource runtimeSource, Registry<StructureSet> structureSetRegistry, Registry<NormalNoise.NoiseParameters> noiseRegistry, long seed, Holder<NoiseGeneratorSettings> generatorSettings)
    {
        super(structureSetRegistry, Optional.of(getEnabledStructures(presetFolderName)), populationSource, runtimeSource, seed);
        if (!(populationSource instanceof ILayerSource)) {
            throw new RuntimeException("OTG has detected an incompatible biome provider- try using otg:otg as the biome source name");
        }

        this.worldSeed = seed;
        NoiseGeneratorSettings genSettings = generatorSettings.value();
        this.generatorSettingsHolder = generatorSettings;
        NoiseSettings noisesettings = genSettings.noiseSettings();
        this.noiseHeight = noisesettings.height();
        this.noises = noiseRegistry;
        this.defaultBlock = genSettings.defaultBlock();
        this.defaultFluid = genSettings.defaultFluid();

        this.preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
        this.structurePlacementSettings = getStructurePlacementMap(preset.getWorldConfig());
        this.internalGenerator = new OTGChunkGenerator(this.preset, seed, (ILayerSource) populationSource, ((FabricPresetLoader) OTG.getEngine().getPresetLoader()).getGlobalIdMapping(presetFolderName), OTG.getEngine().getLogger());
        this.shadowChunkGenerator = new ShadowChunkGenerator(internalGenerator.getMinY(), internalGenerator.getMaxY());
        this.chunkDecorator = new OTGChunkDecorator();

        this.router = genSettings.createNoiseRouter(this.noises, seed);
        this.sampler = new Climate.Sampler(this.router.temperature(), this.router.humidity(), this.router.continents(), this.router.erosion(), this.router.depth(), this.router.ridges(), this.router.spawnTarget());
    }

    // Method to remove structures which have been disabled in the world config
    private static HolderSet<StructureSet> getEnabledStructures(String presetFolderName)
    {
        Preset preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
        IWorldConfig worldConfig = preset.getWorldConfig();
        List<Holder<StructureSet>> holderList = new ArrayList<>();
        HashMap<StructureType, StructurePlacement> placementSettings = getStructurePlacementMap(worldConfig);

        if(worldConfig.getRareBuildingsEnabled())
        {
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.IGLOO, placementSettings.get(StructureType.IGLOO))));
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.SWAMP_HUT, placementSettings.get(StructureType.SWAMP_HUT))));
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.DESERT_PYRAMID, placementSettings.get(StructureType.DESERT_PYRAMID))));
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.JUNGLE_TEMPLE, placementSettings.get(StructureType.JUNGLE_TEMPLE))));
        }

        if(worldConfig.getVillagesEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureSets.VILLAGES.value().structures(), placementSettings.get(StructureType.VILLAGE))));
        if(worldConfig.getPillagerOutpostsEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.PILLAGER_OUTPOST, placementSettings.get(StructureType.PILLLAGER_OUTPOST))));
        if(worldConfig.getStrongholdsEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.STRONGHOLD, placementSettings.get(StructureType.STRONGHOLD))));
        if(worldConfig.getOceanMonumentsEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.OCEAN_MONUMENT, placementSettings.get(StructureType.OCEAN_MONUMENT))));
        if(worldConfig.getEndCitiesEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.END_CITY, placementSettings.get(StructureType.END_CITY))));
        if(worldConfig.getWoodlandMansionsEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.WOODLAND_MANSION, placementSettings.get(StructureType.WOODLAND_MANSION))));
        if(worldConfig.getBuriedTreasureEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.BURIED_TREASURE, placementSettings.get(StructureType.BURIED_TREASURE))));
        if(worldConfig.getMineshaftsEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureSets.MINESHAFTS.value().structures(), placementSettings.get(StructureType.MINESHAFT))));
        if(worldConfig.getRuinedPortalsEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureSets.RUINED_PORTALS.value().structures(), placementSettings.get(StructureType.RUINED_PORTAL))));
        if(worldConfig.getShipWrecksEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureSets.SHIPWRECKS.value().structures(), placementSettings.get(StructureType.SHIPWRECK))));
        if(worldConfig.getOceanRuinsEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureSets.OCEAN_RUINS.value().structures(), placementSettings.get(StructureType.OCEAN_RUINS))));
        if(worldConfig.getBastionRemnantsEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.BASTION_REMNANT, placementSettings.get(StructureType.BASTION_REMNANT))));
        if(worldConfig.getNetherFortressesEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.FORTRESS, placementSettings.get(StructureType.FORTRESS))));
        if(worldConfig.getNetherFossilsEnabled())
            holderList.add(Holder.direct(new StructureSet(StructureFeatures.NETHER_FOSSIL, placementSettings.get(StructureType.NETHER_FOSSIL))));
        return HolderSet.direct(holderList);
    }

    private static HashMap<StructureType, StructurePlacement> getStructurePlacementMap(IWorldConfig worldConfig)
    {
        HashMap<StructureType, StructurePlacement> placementSettings =  new HashMap<>();
        placementSettings.put(StructureType.SWAMP_HUT, new RandomSpreadStructurePlacement(worldConfig.getSwampHutSpacing(), worldConfig.getSwampHutSeparation(), RandomSpreadType.LINEAR, 14357620));
        placementSettings.put(StructureType.IGLOO, new RandomSpreadStructurePlacement(worldConfig.getIglooSpacing(), worldConfig.getIglooSeparation(), RandomSpreadType.LINEAR, 14357618));
        placementSettings.put(StructureType.DESERT_PYRAMID, new RandomSpreadStructurePlacement(worldConfig.getDesertPyramidSpacing(), worldConfig.getDesertPyramidSeparation(), RandomSpreadType.LINEAR, 14357617));
        placementSettings.put(StructureType.JUNGLE_TEMPLE, new RandomSpreadStructurePlacement(worldConfig.getJungleTempleSpacing(), worldConfig.getJungleTempleSeparation(), RandomSpreadType.LINEAR, 14357619));
        placementSettings.put(StructureType.VILLAGE, new RandomSpreadStructurePlacement(worldConfig.getVillageSpacing(), worldConfig.getVillageSeparation(), RandomSpreadType.LINEAR, 10387312));
        placementSettings.put(StructureType.PILLLAGER_OUTPOST, new RandomSpreadStructurePlacement(worldConfig.getPillagerOutpostSpacing(), worldConfig.getPillagerOutpostSeparation(), RandomSpreadType.LINEAR, 165745296));
        placementSettings.put(StructureType.STRONGHOLD, new ConcentricRingsStructurePlacement(worldConfig.getStrongHoldDistance(), worldConfig.getStrongHoldSpread(), worldConfig.getStrongHoldCount()));
        placementSettings.put(StructureType.OCEAN_MONUMENT, new RandomSpreadStructurePlacement(worldConfig.getOceanMonumentSpacing(), worldConfig.getOceanMonumentSeparation(), RandomSpreadType.TRIANGULAR, 10387313));
        placementSettings.put(StructureType.END_CITY, new RandomSpreadStructurePlacement(worldConfig.getEndCitySpacing(), worldConfig.getEndCitySeparation(), RandomSpreadType.TRIANGULAR, 10387313));
        placementSettings.put(StructureType.WOODLAND_MANSION, new RandomSpreadStructurePlacement(worldConfig.getWoodlandMansionSpacing(), worldConfig.getWoodlandMansionSeparation(), RandomSpreadType.TRIANGULAR, 10387319));
        placementSettings.put(StructureType.BURIED_TREASURE, new RandomSpreadStructurePlacement(worldConfig.getBuriedTreasureSpacing(), worldConfig.getBuriedTreasureSeparation(), RandomSpreadType.LINEAR, 0, new Vec3i(9, 0, 9)));
        placementSettings.put(StructureType.MINESHAFT, new RandomSpreadStructurePlacement(worldConfig.getMineshaftSpacing(), worldConfig.getMineshaftSeparation(), RandomSpreadType.LINEAR, 0));
        placementSettings.put(StructureType.RUINED_PORTAL, new RandomSpreadStructurePlacement(worldConfig.getRuinedPortalSpacing(), worldConfig.getRuinedPortalSeparation(), RandomSpreadType.LINEAR, 34222645));
        placementSettings.put(StructureType.SHIPWRECK, new RandomSpreadStructurePlacement(worldConfig.getShipwreckSpacing(), worldConfig.getShipwreckSeparation(), RandomSpreadType.LINEAR, 165745295));
        placementSettings.put(StructureType.OCEAN_RUINS, new RandomSpreadStructurePlacement(worldConfig.getOceanRuinSpacing(), worldConfig.getOceanRuinSeparation(), RandomSpreadType.LINEAR, 14357621));
        placementSettings.put(StructureType.BASTION_REMNANT, new RandomSpreadStructurePlacement(worldConfig.getBastionRemnantSpacing(), worldConfig.getBastionRemnantSeparation(), RandomSpreadType.LINEAR, 30084232));
        placementSettings.put(StructureType.FORTRESS, new RandomSpreadStructurePlacement(worldConfig.getNetherFortressSpacing(), worldConfig.getNetherFortressSeparation(), RandomSpreadType.LINEAR, 30084232));
        placementSettings.put(StructureType.NETHER_FOSSIL, new RandomSpreadStructurePlacement(worldConfig.getNetherFossilSpacing(), worldConfig.getNetherFossilSeparation(), RandomSpreadType.LINEAR, 14357921));
        return placementSettings;
    }


    public ICachedBiomeProvider getCachedBiomeProvider()
    {
        return this.internalGenerator.getCachedBiomeProvider();
    }

    // TODO: This should be called in an onSave method somewhere
    public void saveStructureCache()
    {
        if (this.chunkDecorator.getIsSaveRequired() && this.structureCache != null)
        {
            this.structureCache.saveToDisk(OTG.getEngine().getLogger(), this.chunkDecorator);
        }
    }

    // Base terrain gen

    //buildNoiseSpigot has been moved to fillFromNoise

    public static void findNoiseStructures(ChunkPos pos, ChunkAccess chunk, StructureFeatureManager manager, ObjectList<JigsawStructureData> structures, ObjectList<JigsawStructureData> junctions) {

        int chunkX = pos.x;
        int chunkZ = pos.z;
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        // Iterate through all of the jigsaw structures (villages, pillager outposts, nether fossils)

        // Get all structure starts in this chunk
        List<StructureStart> structureStarts = manager.startsForFeature(
                SectionPos.bottomOf(chunk),
                (configuredStructureFeature) -> configuredStructureFeature.adaptNoise);

        for(StructureStart start : structureStarts) {
            // Iterate through the pieces in the structure
            for(StructurePiece piece : start.getPieces()) {
                // Check if it intersects with this chunk
                if (piece.isCloseToChunk(pos, 12)) {
                    BoundingBox box = piece.getBoundingBox();

                    if (piece instanceof PoolElementStructurePiece villagePiece) {
                        // Add to the list if it's a rigid piece
                        if (villagePiece.getElement().getProjection() == StructureTemplatePool.Projection.RIGID) {
                            structures.add(new JigsawStructureData(box.minX(), box.minY(), box.minZ(),box.maxX(), villagePiece.getGroundLevelDelta(), box.maxZ(), true, 0, 0, 0));
                        }

                        // Get all the junctions in this piece
                        for(JigsawJunction junction : villagePiece.getJunctions()) {
                            int sourceX = junction.getSourceX();
                            int sourceZ = junction.getSourceZ();

                            // If the junction is in this chunk, then add to list
                            if (sourceX > startX - 12 && sourceZ > startZ - 12 && sourceX < startX + 15 + 12 && sourceZ < startZ + 15 + 12) {
                                junctions.add(new JigsawStructureData(0, 0, 0,0, 0, 0, false, junction.getSourceX(), junction.getSourceGroundY(), junction.getSourceZ()));
                            }
                        }
                    } else {
                        structures.add(new JigsawStructureData(box.minX(), box.minY(), box.minZ(),box.maxX(), 0, box.maxZ(),  false, 0, 0, 0));
                    }
                }
            }
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, StructureFeatureManager accessor, ChunkAccess chunk)
    {
        // If we've already generated and cached this
        // chunk while it was unloaded, use cached data.
        ChunkCoordinate chunkCoord = ChunkCoordinate.fromChunkCoords(chunk.getPos().x, chunk.getPos().z);

        // Dummy random, as we can't get the level random right now
        Random random = new Random();

        // When generating the spawn area, Spigot will get the structure and biome info for the first chunk before we can inject
        // Therefore, we need to re-do these calls now, for that one chunk
//		if (fixBiomesForChunk != null && fixBiomesForChunk.equals(chunkCoord))
//		{

        // Should only run when first creating the world, on a single chunk
        // TODO: we need a ServerLevel or similar for this
        //this.createStructures(world.getMinecraftWorld().registryAccess(), world.getMinecraftWorld().structureFeatureManager(), chunk, world.getMinecraftWorld().getStructureManager(), worldSeed);
//			this.createBiomes(chunk.biomeRegistry, executor, blender, accessor, chunk);

        //	fixBiomesForChunk = null;
        //}
        ChunkBuffer buffer = new FabricChunkBuffer(chunk);
        ChunkAccess cachedChunk = this.shadowChunkGenerator.getChunkFromCache(chunkCoord);
        if (cachedChunk != null)
        {
            this.shadowChunkGenerator.fillWorldGenChunkFromShadowChunk(chunkCoord, chunk, cachedChunk);
        } else {
            // Setup jigsaw data
            ObjectList<JigsawStructureData> structures = new ObjectArrayList<>(10);
            ObjectList<JigsawStructureData> junctions = new ObjectArrayList<>(32);

            ChunkPos pos = new ChunkPos(chunkCoord.getChunkX(), chunkCoord.getChunkZ());

            findNoiseStructures(pos, chunk, accessor, structures, junctions);

            this.internalGenerator.populateNoise(random, buffer, buffer.getChunkCoordinate(), structures, junctions);
            this.shadowChunkGenerator.setChunkGenerated(chunkCoord);
        }

        return CompletableFuture.completedFuture(chunk);
    }

    // Generates the base terrain for a chunk.


    // Replaces surface and ground blocks in base terrain and places bedrock.
    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureFeatureManager structures, ChunkAccess chunk)
    {
        // OTG handles surface/ground blocks during base terrain gen. For non-OTG biomes used
    }

    // Carvers: Caves and ravines

    @Override
    public void applyCarvers(WorldGenRegion chunkRegion, long seed, BiomeManager biomeManager, StructureFeatureManager structureAccess, ChunkAccess chunk, GenerationStep.Carving stage)
    {
        // Todo: update
    }

    // Population / decoration

    @Override
    public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunk, StructureFeatureManager manager)
    {
        if(!OTG.getEngine().getPluginConfig().getDecorationEnabled())
        {
            return;
        }

        ChunkPos chunkpos = chunk.getPos();
        if (!SharedConstants.debugVoidTerrain(chunkpos))
        {
            WorldGenRegion worldGenRegion = ((WorldGenRegion)worldGenLevel);
            SectionPos sectionpos = SectionPos.of(chunkpos, worldGenRegion.getMinSection());
            BlockPos blockpos = sectionpos.origin();
            Registry<ConfiguredStructureFeature<?, ?>> structureRegistry = worldGenLevel.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
            Map<Integer, List<ConfiguredStructureFeature<?, ?>>> configuredStructureMap = structureRegistry.stream()
                    .collect(Collectors.groupingBy(p -> p.feature.step().ordinal()));
            List<BiomeSource.StepFeatureData> list = this.biomeSource.featuresPerStep();
            WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
            long decorationSeed = worldgenrandom.setDecorationSeed(worldGenRegion.getSeed(), blockpos.getX(), blockpos.getZ());

            // This section is the only part that diverges from vanilla, but it probably has to stay this way for now
            //
            int worldX = worldGenRegion.getCenter().x * Constants.CHUNK_SIZE;
            int worldZ =worldGenRegion.getCenter().z * Constants.CHUNK_SIZE;
            ChunkCoordinate chunkBeingDecorated = ChunkCoordinate.fromBlockCoords(worldX, worldZ);
            IBiome noiseBiome = this.internalGenerator.getCachedBiomeProvider().getNoiseBiome((worldGenRegion.getCenter().x << 2) + 2, (worldGenRegion.getCenter().z << 2) + 2);
            FabricWorldGenRegion forgeWorldGenRegion = new FabricWorldGenRegion(this.preset.getFolderName(), this.preset.getWorldConfig(), worldGenRegion, this);
            // World save folder name may not be identical to level name, fetch it.
            Path worldSaveFolder = worldGenRegion.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).getParent();
            this.chunkDecorator.decorate(this.preset.getFolderName(), chunkBeingDecorated, forgeWorldGenRegion, noiseBiome.getBiomeConfig(), getStructureCache(worldSaveFolder));

            Set<Biome> set = new ObjectArraySet<>();
            ChunkPos.rangeClosed(sectionpos.chunk(), 1).forEach((pos) ->
            {
                ChunkAccess chunkaccess = worldGenLevel.getChunk(pos.x, pos.z);
                for(LevelChunkSection levelchunksection : chunkaccess.getSections())
                {
                    levelchunksection.getBiomes().getAll((b) -> set.add(b.value()));
                }
            });
            set.retainAll(this.biomeSource.possibleBiomes().stream().map(Holder::value).collect(Collectors.toSet()));

            int length = list.size();

            try {
                Registry<PlacedFeature> placedRegistry = worldGenRegion.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
                int steps = Math.max(GenerationStep.Decoration.values().length, length);

                for(int step = 0; step < steps; ++step)
                {
                    int n = 0;
                    if (manager.shouldGenerateFeatures())
                    {
                        for(ConfiguredStructureFeature<?, ?> feature : configuredStructureMap.getOrDefault(step, Collections.emptyList()))
                        {
                            worldgenrandom.setFeatureSeed(decorationSeed, n, step);
                            Supplier<String> supplier = () -> structureRegistry
                                    .getResourceKey(feature)
                                    .map(Object::toString)
                                    .orElseGet(feature::toString);

                            try {
                                worldGenRegion.setCurrentlyGenerating(supplier);
                                manager.startsForFeature(sectionpos, feature).forEach(
                                        start -> start.placeInChunk(worldGenRegion, manager, this, worldgenrandom, getWritableArea(chunk), chunkpos));
                            } catch (Exception exception) {
                                CrashReport report = CrashReport.forThrowable(exception, "Feature placement");
                                report.addCategory("Feature").setDetail("Description", supplier::get);
                                throw new ReportedException(report);
                            }

                            ++n;
                        }
                    }

                    if (step < length)
                    {
                        IntSet intset = new IntArraySet();

                        for(Biome biome : set)
                        {
                            List<HolderSet<PlacedFeature>> holderList = biome.getGenerationSettings().features();
                            if (step < holderList.size())
                            {
                                HolderSet<PlacedFeature> featureHolder = holderList.get(step);
                                BiomeSource.StepFeatureData data = list.get(step);
                                featureHolder.stream().map(Holder::value).forEach(
                                        f -> intset.add(data.indexMapping().applyAsInt(f)));
                            }
                        }

                        int biomeCount = intset.size();
                        int[] aint = intset.toIntArray();
                        Arrays.sort(aint);
                        BiomeSource.StepFeatureData biomesource$stepfeaturedata = list.get(step);

                        for(int i = 0; i < biomeCount; ++i)
                        {
                            int j = aint[i];
                            PlacedFeature placedfeature = biomesource$stepfeaturedata.features().get(j);
                            Supplier<String> supplier1 = () -> {
                                return placedRegistry.getResourceKey(placedfeature).map(Object::toString).orElseGet(placedfeature::toString);
                            };
                            worldgenrandom.setFeatureSeed(decorationSeed, j, step);

                            try {
                                worldGenRegion.setCurrentlyGenerating(supplier1);
                                placedfeature.placeWithBiomeCheck(worldGenRegion, this, worldgenrandom, blockpos);
                            } catch (Exception exception1) {
                                CrashReport crashreport2 = CrashReport.forThrowable(exception1, "Feature placement");
                                crashreport2.addCategory("Feature").setDetail("Description", supplier1::get);
                                throw new ReportedException(crashreport2);
                            }
                        }
                    }
                }

                worldGenRegion.setCurrentlyGenerating(null);
            } catch (Exception exception2) {
                CrashReport crashreport = CrashReport.forThrowable(exception2, "Biome decoration");
                crashreport.addCategory("Generation").setDetail("CenterX", chunkpos.x).setDetail("CenterZ", chunkpos.z).setDetail("Seed", decorationSeed);
                throw new ReportedException(crashreport);
            }
        }
    }

    private static BoundingBox getWritableArea(ChunkAccess p_187718_)
    {
        ChunkPos chunkpos = p_187718_.getPos();
        int i = chunkpos.getMinBlockX();
        int j = chunkpos.getMinBlockZ();
        LevelHeightAccessor levelheightaccessor = p_187718_.getHeightAccessorForGeneration();
        int k = levelheightaccessor.getMinBuildHeight() + 1;
        int l = levelheightaccessor.getMaxBuildHeight() - 1;
        return new BoundingBox(i, k, j, i + 15, l, j + 15);
    }

    // Mob spawning on initial chunk spawn (animals).
    @Override
    public void spawnOriginalMobs(WorldGenRegion region)
    {
        // We don't respect the mob spawning setting, because we can't access it
        int chunkX = region.getCenter().x;
        int chunkZ = region.getCenter().z;
        IBiome biome = this.internalGenerator.getCachedBiomeProvider().getBiome(chunkX * Constants.CHUNK_SIZE + DecorationArea.DECORATION_OFFSET, chunkZ * Constants.CHUNK_SIZE + DecorationArea.DECORATION_OFFSET);
        WorldgenRandom sharedseedrandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
        sharedseedrandom.setDecorationSeed(region.getSeed(), chunkX << 4, chunkZ << 4);
        NaturalSpawner.spawnMobsForChunkGeneration(region, Holder.direct(((FabricBiome)biome).getBiome()), region.getCenter(), sharedseedrandom);
    }

    // Mob spawning on chunk tick
    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> biome, StructureFeatureManager structureManager, MobCategory entityClassification, BlockPos blockPos)
    {
        return super.getMobsAt(biome, structureManager, entityClassification, blockPos);
    }

    // Noise
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor world)
    {
        NoiseSettings noiseSettings = this.generatorSettingsHolder.value().noiseSettings();
        int minGenY = Math.max(noiseSettings.minY(), world.getMinBuildHeight());
        int maxGenY = Math.min(noiseSettings.minY() + noiseSettings.height(), world.getMaxBuildHeight());
        int cellNoiseMinY = Math.floorDiv(minGenY, noiseSettings.getCellHeight());
        int noiseCellCount = Math.floorDiv(maxGenY - minGenY, noiseSettings.getCellHeight());
        return noiseCellCount <= 0 ?
                world.getMinBuildHeight() :
                this.sampleHeightmap(x, z, null, heightmap.isOpaque(), cellNoiseMinY, noiseCellCount);
    }

    // Provides a sample of the full column for structure generation.
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world)
    {
        NoiseSettings noiseSettings = this.generatorSettingsHolder.value().noiseSettings();
        int minGenY = Math.max(noiseSettings.minY(), world.getMinBuildHeight());
        int maxGenY = Math.min(noiseSettings.minY() + noiseSettings.height(), world.getMaxBuildHeight());
        int cellNoiseMinY = Math.floorDiv(minGenY, noiseSettings.getCellHeight());
        int noiseCellCount = Math.floorDiv(maxGenY - minGenY, noiseSettings.getCellHeight());
        if (noiseCellCount <= 0)
        {
            return new NoiseColumn(minGenY, new BlockState[0]);
        } else {
            BlockState[] blockStates = new BlockState[noiseCellCount * noiseSettings.getCellHeight() - minGenY];
            this.sampleHeightmap(x, z, blockStates, null, cellNoiseMinY, noiseCellCount);
            return new NoiseColumn(0, blockStates);
        }
    }

    @Override
    public void addDebugScreenInfo(List<String> text, BlockPos pos) {
        // TODO: what does this do? -auth
    }

    // Samples the noise at a column and provides a view of the blockstates, or fills a heightmap.
    private int sampleHeightmap (int x, int z, @Nullable BlockState[] blockStates, @Nullable Predicate<BlockState> predicate, int cellNoiseMinY, int noiseCellCount)
    {
        NoiseSettings noisesettings = this.generatorSettingsHolder.value().noiseSettings();
        int cellWidth = noisesettings.getCellWidth();
        // Get all of the coordinate starts and positions
        int xStart = Math.floorDiv(x, cellWidth);
        int zStart = Math.floorDiv(z, cellWidth);
        int xProgress = Math.floorMod(x, cellWidth);
        int zProgress = Math.floorMod(z, cellWidth);
        double xLerp = (double) xProgress / cellWidth;
        double zLerp = (double) zProgress / cellWidth;
        // Create the noise data in a 2 * 2 * 32 grid for interpolation.
        double[][] noiseData = new double[4][this.internalGenerator.getNoiseSizeY() + 1];

        // Initialize noise array.
        for (int i = 0; i < noiseData.length; i++)
        {
            noiseData[i] = new double[this.internalGenerator.getNoiseSizeY() + 1];
        }

        // Sample all 4 nearby columns.
        this.internalGenerator.getNoiseColumn(noiseData[0], xStart, zStart);
        this.internalGenerator.getNoiseColumn(noiseData[1], xStart, zStart + 1);
        this.internalGenerator.getNoiseColumn(noiseData[2], xStart + 1, zStart);
        this.internalGenerator.getNoiseColumn(noiseData[3], xStart + 1, zStart + 1);

        //IBiomeConfig biomeConfig = this.internalGenerator.getCachedBiomeProvider().getBiomeConfig(x, z);

        BlockState state;
        double x0z0y0;
        double x0z1y0;
        double x1z0y0;
        double x1z1y0;
        double x0z0y1;
        double x0z1y1;
        double x1z0y1;
        double x1z1y1;
        double yLerp;
        double density;
        int y;
        // [0, 32] -> noise chunks
        for (int noiseY = this.internalGenerator.getNoiseSizeY() - 1; noiseY >= 0; --noiseY)
        {
            // Gets all the noise in a 2x2x2 cube and interpolates it together.
            // Lower pieces
            x0z0y0 = noiseData[0][noiseY];
            x0z1y0 = noiseData[1][noiseY];
            x1z0y0 = noiseData[2][noiseY];
            x1z1y0 = noiseData[3][noiseY];
            // Upper pieces
            x0z0y1 = noiseData[0][noiseY + 1];
            x0z1y1 = noiseData[1][noiseY + 1];
            x1z0y1 = noiseData[2][noiseY + 1];
            x1z1y1 = noiseData[3][noiseY + 1];

            // [0, 8] -> noise pieces
            for (int pieceY = 7; pieceY >= 0; --pieceY)
            {
                yLerp = (double) pieceY / 8.0;
                // Density at this position given the current y interpolation
                // used to have yLerp and xLerp switched, which seemed wrong? -auth
                density = Mth.lerp3(xLerp, yLerp, zLerp, x0z0y0, x0z0y1, x1z0y0, x1z0y1, x0z1y0, x0z1y1, x1z1y0, x1z1y1);

                // Get the real y position (translate noise chunk and noise piece)
                y = (noiseY * 8) + pieceY;

                //state = this.getBlockState(density, y, biomeConfig);
                state = this.getBlockState(density, y);
                if (blockStates != null)
                {
                    blockStates[y] = state;
                }

                // return y if it fails the check
                if (predicate != null && predicate.test(state))
                {
                    return y + 1;
                }
            }
        }

        return 0;
    }

    private BlockState getBlockState(double density, int y)
    {
        if (density > 0.0D)
        {
            return this.defaultBlock;
        }
        else if (y < this.getSeaLevel())
        {
            return this.defaultFluid;
        } else {
            return Blocks.AIR.defaultBlockState();
        }
    }

    // Getters / misc

    @Override
    public ChunkGenerator withSeed(long seed)
    {
        return new OTGNoiseChunkGenerator(this.biomeSource.withSeed(seed), seed, this.structureSets, this.noises, this.generatorSettingsHolder);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec()
    {
        return CODEC;
    }

    @Override
    public Climate.Sampler climateSampler() {
        return this.sampler;
    }

    @Override
    public int getGenDepth()
    {
        return this.noiseHeight;
    }

    @Override
    public int getSeaLevel ()
    {
        return this.generatorSettingsHolder.value().seaLevel();
    }

    public Preset getPreset()
    {
        return preset;
    }

    @Override
    public int getMinY() {
        return generatorSettingsHolder.value().noiseSettings().minY();
    }

    public CustomStructureCache getStructureCache(Path worldSaveFolder)
    {
        if(this.structureCache == null)
        {
            this.structureCache = OTG.getEngine().createCustomStructureCache(this.preset.getFolderName(), worldSaveFolder, this.worldSeed, this.preset.getWorldConfig().getCustomStructureType() == SettingsEnums.CustomStructureType.BO4);
        }
        return this.structureCache;
    }

    double getBiomeBlocksNoiseValue (int blockX, int blockZ)
    {
        return this.internalGenerator.getBiomeBlocksNoiseValue(blockX, blockZ);
    }

    public void fixBiomes(int chunkX, int chunkZ)
    {
        this.fixBiomesForChunk = ChunkCoordinate.fromChunkCoords(chunkX, chunkZ);
    }

    // Shadowgen

    public Boolean checkHasVanillaStructureWithoutLoading(ServerLevel world, ChunkCoordinate chunkCoord)
    {
        // This method needs updating to 1.18.2 in the right way. For now, has been replaced by this.checkForVanillaStructure()
        return false;
        //return this.shadowChunkGenerator.checkHasVanillaStructureWithoutLoading(world, this, this.biomeSource, this., chunkCoord, this.internalGenerator.getCachedBiomeProvider(), false);
    }

    public int getHighestBlockYInUnloadedChunk(Random worldRandom, int x, int z, boolean findSolid, boolean findLiquid, boolean ignoreLiquid, boolean ignoreSnow, ServerLevel level)
    {
        return this.shadowChunkGenerator.getHighestBlockYInUnloadedChunk(this.internalGenerator, worldRandom, x, z, findSolid, findLiquid, ignoreLiquid, ignoreSnow, level);
    }

    public LocalMaterialData getMaterialInUnloadedChunk(Random worldRandom, int x, int y, int z, ServerLevel level)
    {
        return this.shadowChunkGenerator.getMaterialInUnloadedChunk(this.internalGenerator, worldRandom, x, y, z, level);
    }

    public FabricChunkBuffer getChunkWithoutLoadingOrCaching(Random random, ChunkCoordinate chunkCoord, ServerLevel level)
    {
        return this.shadowChunkGenerator.getChunkWithoutLoadingOrCaching(this.internalGenerator, random, chunkCoord, level);
    }
    // Uses the vanilla method of checking if there is a vanilla structure in range
    // Might be slower than old solution in ShadowChunkGenerator
    public boolean checkForVanillaStructure(ChunkCoordinate chunkCoordinate) {
        int x = chunkCoordinate.getChunkX();
        int z = chunkCoordinate.getChunkZ();
        // Structures with a radius of 4
        FabricBiome biome = (FabricBiome) getCachedBiomeProvider().getNoiseBiome((x << 2) + 2, (z << 2) + 2);
        if (biome.getBiomeConfig().getVillageType() != SettingsEnums.VillageType.disabled)
            if (this.hasFeatureChunkInRange(BuiltinStructureSets.VILLAGES, worldSeed, x, z, 4))
                return true;
        if (biome.getBiomeConfig().getBastionRemnantEnabled())
            if (this.hasFeatureChunkInRange(BuiltinStructureSets.NETHER_COMPLEXES, worldSeed, x, z, 4))
                return true;
        if (biome.getBiomeConfig().getEndCityEnabled())
            if (this.hasFeatureChunkInRange(BuiltinStructureSets.END_CITIES, worldSeed, x, z, 4))
                return true;
        if (biome.getBiomeConfig().getOceanMonumentsEnabled())
            if (this.hasFeatureChunkInRange(BuiltinStructureSets.OCEAN_MONUMENTS, worldSeed, x, z, 4))
                return true;
        if (biome.getBiomeConfig().getWoodlandMansionsEnabled())
            if (this.hasFeatureChunkInRange(BuiltinStructureSets.WOODLAND_MANSIONS, worldSeed, x, z, 4))
                return true;
        switch (biome.getBiomeConfig().getRareBuildingType()) {
            case disabled -> {}
            case desertPyramid -> {
                if (this.hasFeatureChunkInRange(BuiltinStructureSets.DESERT_PYRAMIDS, worldSeed, x, z, 1))
                    return true;
            }
            case jungleTemple -> {
                if (this.hasFeatureChunkInRange(BuiltinStructureSets.JUNGLE_TEMPLES, worldSeed, x, z, 1))
                    return true;
            }
            case swampHut -> {
                if (this.hasFeatureChunkInRange(BuiltinStructureSets.SWAMP_HUTS, worldSeed, x, z, 1))
                    return true;
            }
            case igloo -> {
                if (this.hasFeatureChunkInRange(BuiltinStructureSets.IGLOOS, worldSeed, x, z, 1))
                    return true;
            }
        }
        if (biome.getBiomeConfig().getShipWreckEnabled() || biome.getBiomeConfig().getShipWreckBeachedEnabled())
            if (this.hasFeatureChunkInRange(BuiltinStructureSets.SHIPWRECKS, worldSeed, x, z, 1))
                return true;
        if (biome.getBiomeConfig().getPillagerOutpostEnabled())
            if (this.hasFeatureChunkInRange(BuiltinStructureSets.PILLAGER_OUTPOSTS, worldSeed, x, z, 1))
                return true;
        if (biome.getBiomeConfig().getOceanRuinsType() != SettingsEnums.OceanRuinsType.disabled)
            return this.hasFeatureChunkInRange(BuiltinStructureSets.OCEAN_RUINS, worldSeed, x, z, 1);
        return false;
    }
    public boolean hasFeatureChunkInRange(StructurePlacement structureplacement, long i, int j, int k, int l)
    {
        for (int i1 = j - l; i1 <= j + l; ++i1) {
            for (int j1 = k - l; j1 <= k + l; ++j1) {
                if (structureplacement.isFeatureChunk(this, i, i1, j1)) {
                    return true;
                }
            }
        }
        return false;
    }
}
