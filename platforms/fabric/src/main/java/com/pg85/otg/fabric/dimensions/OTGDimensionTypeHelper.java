package com.pg85.otg.fabric.dimensions;

import java.util.OptionalLong;
import java.util.Random;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

import com.pg85.otg.fabric.biome.OTGBiomeProvider;
import com.pg85.otg.fabric.gen.OTGNoiseChunkGenerator;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.core.config.dimensions.DimensionConfig;
import com.pg85.otg.core.config.dimensions.DimensionConfig.OTGDimension;
import com.pg85.otg.core.config.dimensions.DimensionConfig.OTGOverWorld;
import com.pg85.otg.core.presets.Preset;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.Climate.ParameterList;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class OTGDimensionTypeHelper
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static DimensionType make(OptionalLong fixedTime, boolean hasSkylight, boolean hasCeiling, boolean ultraWarm, boolean natural, double coordinateScale, boolean createDragonFight, boolean piglinSafe, boolean bedWorks, boolean respawnAnchorWorks, boolean hasRaids, int minY, int height, int logicalHeight, TagKey<Block> infiniburn, ResourceLocation effectsLocation, float ambientLight)
    {
        return DimensionType.create(fixedTime, hasSkylight, hasCeiling, ultraWarm, natural, coordinateScale, createDragonFight, piglinSafe, bedWorks, respawnAnchorWorks, hasRaids, minY, height, logicalHeight, infiniburn, effectsLocation, ambientLight);
    }

    // Used for MP when starting the server, with settings from server.properties.
    public static WorldGenSettings createOTGSettings(RegistryAccess dynamicRegistries, long seed, boolean generateFeatures, boolean bonusChest, String generatorSettings)
    {
        WritableRegistry<DimensionType> dimensionTypesRegistry = (WritableRegistry<DimensionType>) dynamicRegistries.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> biomesRegistry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<NoiseGeneratorSettings> dimensionSettingsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        Registry<NormalNoise.NoiseParameters> noiseParamsRegistry = dynamicRegistries.registryOrThrow(Registry.NOISE_REGISTRY);
        Registry<StructureSet> structureSetRegistry = dynamicRegistries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);

        // If there is a dimensionconfig for the generatorsettings, use that. Otherwise find a preset by name.
        DimensionConfig dimConfig = DimensionConfig.fromDisk(generatorSettings);
        MappedRegistry<LevelStem> dimensions = null;
        Preset preset = null;
        String dimConfigName = null;
        if(dimConfig == null)
        {
            // Find the preset defined in generatorSettings, if none use the default preset.
            preset = OTG.getEngine().getPresetLoader().getPresetByShortNameOrFolderName(generatorSettings);
            if(preset == null)
            {
                OTG.getEngine().getLogger().log(LogLevel.FATAL, LogCategory.MAIN, "DimensionConfig or preset name \"" + generatorSettings +"\", provided as generator-settings in server.properties, does not exist.");
                throw new RuntimeException("DimensionConfig or preset name \"" + generatorSettings +"\", provided as generator-settings in server.properties, does not exist.");
            } else {
                dimConfig = new DimensionConfig();
                dimConfig.Overworld = new OTGOverWorld(preset.getFolderName(), seed, null, null);
                dimensions = (MappedRegistry<LevelStem>) DimensionType.defaultDimensions(dynamicRegistries, seed);
            }
        } else {
            // todo: implement dimension configs in fabric (this code below is temporary placeholder)

            dimConfigName = generatorSettings;
            // Non-otg overworld, generatorsettings contains non-otg world type.
            WorldGenSettings existingDimSetting = null;
            Registry<DimensionType> registry2 = dynamicRegistries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
            Registry<Biome> registry = dynamicRegistries.registryOrThrow(Registry.BIOME_REGISTRY);
            MappedRegistry<LevelStem> mappedregistry = (MappedRegistry<LevelStem>) DimensionType.defaultDimensions(dynamicRegistries, seed);
            String nonOTGGeneratorSettings = dimConfig.Overworld.NonOTGGeneratorSettings;

            switch(dimConfig.Overworld.NonOTGWorldType == null ? "" : dimConfig.Overworld.NonOTGWorldType)
            {
                case "flat":
                    JsonObject jsonobject = nonOTGGeneratorSettings != null && !nonOTGGeneratorSettings.isEmpty() ? GsonHelper.parse(nonOTGGeneratorSettings) : new JsonObject();
                    Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, jsonobject);
                    existingDimSetting = new WorldGenSettings(
                            seed,
                            generateFeatures,
                            bonusChest,
                            WorldGenSettings.withOverworld(
                                    registry2,
                                    mappedregistry,
                                    new FlatLevelSource(
                                            structureSetRegistry,
                                            FlatLevelGeneratorSettings.CODEC.parse(dynamic).resultOrPartial(LOGGER::error)
                                                    .orElseGet(
                                                            () -> {
                                                                return FlatLevelGeneratorSettings.getDefault(registry, structureSetRegistry);
                                                            }
                                                    )
                                    )
                            )
                    );
                case "debug_all_block_states":
                    existingDimSetting = new WorldGenSettings(seed, generateFeatures, bonusChest, WorldGenSettings.withOverworld(registry2, mappedregistry, new DebugLevelSource(structureSetRegistry, registry)));
                case "amplified":
                    existingDimSetting = new WorldGenSettings(seed, generateFeatures, bonusChest, WorldGenSettings.withOverworld(registry2, mappedregistry, WorldGenSettings.makeOverworld(dynamicRegistries, seed, NoiseGeneratorSettings.AMPLIFIED)));
                case "largebiomes":
                    existingDimSetting = new WorldGenSettings(seed, generateFeatures, bonusChest, WorldGenSettings.withOverworld(registry2, mappedregistry, WorldGenSettings.makeOverworld(dynamicRegistries, seed, NoiseGeneratorSettings.LARGE_BIOMES)));
                default:
                    existingDimSetting = new WorldGenSettings(seed, generateFeatures, bonusChest, WorldGenSettings.withOverworld(registry2, mappedregistry, WorldGenSettings.makeDefaultOverworld(dynamicRegistries, seed)));
            }
            dimensions = (MappedRegistry<LevelStem>) existingDimSetting.dimensions();
        }

        return OTGDimensionTypeHelper.createOTGDimensionGeneratorSettings(
                dimConfigName,
                dimConfig,
                dimensionTypesRegistry,
                biomesRegistry,
                dimensionSettingsRegistry,
                noiseParamsRegistry,
                structureSetRegistry,
                seed,
                generateFeatures,
                bonusChest,
                dimensions
        );
    }

    // Used for SP and MP
    public static WorldGenSettings createOTGDimensionGeneratorSettings(String dimConfigName, DimensionConfig dimConfig, WritableRegistry<DimensionType> dimensionTypesRegistry, Registry<Biome> biomesRegistry, Registry<NoiseGeneratorSettings> dimensionSettingsRegistry, Registry<NormalNoise.NoiseParameters> noiseParamsRegistry, Registry<StructureSet> structureSetRegistry, long seed, boolean generateFeatures, boolean generateBonusChest, MappedRegistry<LevelStem> defaultDimensions)
    {
        // Create a new registry object and register dimensions to it.
        MappedRegistry<LevelStem> dimensions = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        boolean nonOTGOverWorld = dimConfig.Overworld.PresetFolderName == null;

        // Dummy list
        ParameterList<Supplier<Biome>> paramList = new ParameterList<Supplier<Biome>>(
                ImmutableList.of(
                        Pair.of(
                                (ParameterPoint)Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F),
                                (Supplier<Biome>)() -> { return biomesRegistry.getOrThrow(Biomes.PLAINS); }
                        )
                )
        );

        if(dimConfig.Overworld != null && dimConfig.Overworld.PresetFolderName != null && !nonOTGOverWorld)
        {
            ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
                    dimConfig.Overworld.PresetFolderName,
                    new OTGBiomeProvider(
                            dimConfig.Overworld.PresetFolderName,
                            seed,
                            false,
                            false,
                            biomesRegistry
                    ),
                    structureSetRegistry,
                    noiseParamsRegistry,
                    seed,
                    dimensionSettingsRegistry.getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD) // TODO: Add OTG DimensionSettings?
            );
            addDimension(dimConfig.Overworld.PresetFolderName, dimensions, dimensionTypesRegistry, LevelStem.OVERWORLD, chunkGenerator, DimensionType.OVERWORLD_LOCATION);
        }
        if(dimConfig.Nether != null && dimConfig.Nether.PresetFolderName != null)
        {
            long dimSeed = dimConfig.Nether.Seed != -1l ? dimConfig.Nether.Seed : new Random().nextLong();
            ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
                    dimConfig.Nether.PresetFolderName,
                    new OTGBiomeProvider(
                            dimConfig.Nether.PresetFolderName,
                            seed,
                            false,
                            false,
                            biomesRegistry
                    ),
                    structureSetRegistry,
                    noiseParamsRegistry,
                    dimSeed,
                    dimensionSettingsRegistry.getHolderOrThrow(NoiseGeneratorSettings.NETHER) // TODO: Add OTG DimensionSettings?
            );
            addDimension(dimConfig.Nether.PresetFolderName, dimensions, dimensionTypesRegistry, LevelStem.NETHER, chunkGenerator, DimensionType.NETHER_LOCATION);
        }
        if(dimConfig.End != null && dimConfig.End.PresetFolderName != null)
        {
            long dimSeed = dimConfig.End.Seed != -1l ? dimConfig.End.Seed : new Random().nextLong();
            ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
                    dimConfig.End.PresetFolderName,
                    new OTGBiomeProvider(
                            dimConfig.End.PresetFolderName,
                            seed,
                            false,
                            false,
                            biomesRegistry
                    ),
                    structureSetRegistry,
                    noiseParamsRegistry,
                    dimSeed,
                    dimensionSettingsRegistry.getHolderOrThrow(NoiseGeneratorSettings.END) // TODO: Add OTG DimensionSettings?
            );
            addDimension(dimConfig.End.PresetFolderName, dimensions, dimensionTypesRegistry, LevelStem.END, chunkGenerator, DimensionType.END_LOCATION);
        }
        if(dimConfig.Dimensions != null)
        {
            for(OTGDimension otgDim : dimConfig.Dimensions)
            {
                if(otgDim.PresetFolderName != null)
                {
                    long dimSeed = otgDim.Seed != -1l ? otgDim.Seed : new Random().nextLong();
                    ChunkGenerator chunkGenerator = new OTGNoiseChunkGenerator(
                            otgDim.PresetFolderName,
                            new OTGBiomeProvider(
                                    otgDim.PresetFolderName,
                                    seed,
                                    false,
                                    false,
                                    biomesRegistry
                            ),
                            structureSetRegistry,
                            noiseParamsRegistry,
                            dimSeed,
                            dimensionSettingsRegistry.getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD) // TODO: Add OTG DimensionSettings?
                    );
                    ResourceKey<LevelStem> dimRegistryKey = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, otgDim.PresetFolderName.trim().replace(" ", "_").toLowerCase()));
                    ResourceKey<DimensionType> dimTypeRegistryKey = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, otgDim.PresetFolderName.trim().replace(" ", "_").toLowerCase()));
                    addDimension(otgDim.PresetFolderName, dimensions, dimensionTypesRegistry, dimRegistryKey, chunkGenerator, dimTypeRegistryKey);
                }
            }
        }

        // Register default dimensions (if we're not overriding them with otg dimensions)
        for(Entry<ResourceKey<LevelStem>, LevelStem> entry : defaultDimensions.entrySet())
        {
            ResourceKey<LevelStem> registrykey = entry.getKey();
            if (
                    (dimConfig.Overworld == null || dimConfig.Overworld.PresetFolderName == null || registrykey != LevelStem.OVERWORLD) &&
                            (dimConfig.Nether == null || dimConfig.Nether.PresetFolderName == null || registrykey != LevelStem.NETHER) &&
                            (dimConfig.End == null || dimConfig.End.PresetFolderName == null || registrykey != LevelStem.END)
            )
            {
                dimensions.register(registrykey, entry.getValue(), defaultDimensions.lifecycle(entry.getValue()));
            }
        }

        return new WorldGenSettings(
                seed,
                generateFeatures,
                generateBonusChest,
                dimensions
        );
    }

    private static void addDimension(String presetFolderName, MappedRegistry<LevelStem> dimensions, WritableRegistry<DimensionType> dimensionTypeRegistry, ResourceKey<LevelStem> dimRegistryKey, ChunkGenerator chunkGenerator, ResourceKey<DimensionType> dimTypeRegistryKey)
    {
        Preset preset = OTG.getEngine().getPresetLoader().getPresetByFolderName(presetFolderName);
        IWorldConfig worldConfig = preset.getWorldConfig();

        // Register OTG DimensionType with settings from WorldConfig
        DimensionType otgOverWorld = DimensionType.create(
                worldConfig.getFixedTime(),
                worldConfig.getHasSkyLight(),
                worldConfig.getHasCeiling(),
                worldConfig.getUltraWarm(),
                worldConfig.getNatural(),
                worldConfig.getCoordinateScale(),
                worldConfig.getCreateDragonFight(),
                worldConfig.getPiglinSafe(),
                worldConfig.getBedWorks(),
                worldConfig.getRespawnAnchorWorks(),
                worldConfig.getHasRaids(),
                worldConfig.getWorldMinY(),
                worldConfig.getWorldMaxY()+1,
                worldConfig.getLogicalHeight(),
                TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(worldConfig.getInfiniburn())),
                new ResourceLocation(worldConfig.getEffectsLocation()),
                worldConfig.getAmbientLight()
        );

        //unfreeze registry and then refreeze registry after the statements
        ((MappedRegistry) dimensionTypeRegistry).frozen = false;
        dimensionTypeRegistry.registerOrOverride(OptionalInt.empty(), dimTypeRegistryKey, otgOverWorld, Lifecycle.stable());

        LevelStem dimension = dimensions.get(dimRegistryKey);
        dimensions.register(dimRegistryKey, new LevelStem(dimension == null ? dimensionTypeRegistry.getHolderOrThrow(dimTypeRegistryKey) : dimension.typeHolder(), chunkGenerator), Lifecycle.stable());
        ((MappedRegistry) dimensionTypeRegistry).frozen = true;
    }

    // Writes OTG DimensionTypes to world save folder as datapack json files so they're picked up on world load.
    // Unfortunately there doesn't appear to be a way to persist them via code. Silly, but it works.
    public static void saveDataPackFile(Path datapackFolder, String dimName, IWorldConfig worldConfig, String presetFolderName)
    {
        File folder = new File(datapackFolder + File.separator + Constants.MOD_ID_SHORT + File.separator);
        File file = new File(datapackFolder + File.separator + Constants.MOD_ID_SHORT + File.separator + "pack.mcmeta");
        if(!folder.exists())
        {
            folder.mkdirs();
        }
        String data;
        if(!file.exists())
        {
            data = "{ \"pack\": { \"pack_format\":6, \"description\":\"OTG Dimension settings\" } }";
            try(
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos)
            ) {
                byte[] bytes = data.getBytes();
                bos.write(bytes);
                bos.close();
                fos.close();
            } catch (IOException e) { e.printStackTrace(); }
        }

        if(dimName.equals("overworld") || dimName.equals("the_end") || dimName.equals("the_nether"))
        {
            folder = new File(datapackFolder + File.separator + Constants.MOD_ID_SHORT + File.separator + "data" + File.separator + "minecraft" + File.separator + "dimension_type" + File.separator);
            file = new File(datapackFolder + File.separator + Constants.MOD_ID_SHORT + File.separator + "data" + File.separator + "minecraft" + File.separator + "dimension_type" + File.separator + dimName + ".json");
        } else {
            folder = new File(datapackFolder + File.separator + Constants.MOD_ID_SHORT + File.separator + "data" + File.separator + Constants.MOD_ID_SHORT + File.separator + "dimension_type" + File.separator);
            file = new File(datapackFolder + File.separator + Constants.MOD_ID_SHORT + File.separator + "data" + File.separator + Constants.MOD_ID_SHORT + File.separator + "dimension_type" + File.separator + dimName + ".json");
        }

        if(!folder.exists())
        {
            folder.mkdirs();
        }
        int worldHeight = worldConfig.getWorldMaxY()-worldConfig.getWorldMinY();
        // TODO: Make height/min_y configurable? Add name?
        data = "{ \"name\": \"\", \"height\": "+worldHeight+",\"min_y\": "+worldConfig.getWorldMinY()+", \"ultrawarm\": " + worldConfig.getUltraWarm() + ", \"infiniburn\": \"" + worldConfig.getInfiniburn() + "\", \"logical_height\": " + worldConfig.getLogicalHeight() + ", \"has_raids\": " + worldConfig.getHasRaids() + ", \"respawn_anchor_works\": " + worldConfig.getRespawnAnchorWorks() + ", \"bed_works\": " + worldConfig.getBedWorks() + ", \"piglin_safe\": " + worldConfig.getPiglinSafe() + ", \"natural\": " + worldConfig.getNatural() + ", \"coordinate_scale\": " + worldConfig.getCoordinateScale() + ", \"ambient_light\": " + worldConfig.getAmbientLight() + ", \"has_skylight\": " + worldConfig.getHasSkyLight() + ", \"has_ceiling\": " + worldConfig.getHasCeiling() + ", \"effects\": \"" + worldConfig.getEffectsLocation() + "\"" + (worldConfig.getFixedTime().isPresent() ? ", \"fixed_time\": " + worldConfig.getFixedTime().getAsLong() : "") + " }";
        try(
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos)
        ) {
            byte[] bytes = data.getBytes();
            bos.write(bytes);
            bos.close();
            fos.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
