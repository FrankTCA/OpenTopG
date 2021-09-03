package com.pg85.otg.paper.biome;

import java.util.List;
import java.util.Optional;

import com.pg85.otg.OTG;
import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.config.biome.BiomeConfig;
import com.pg85.otg.config.standard.BiomeStandardValues;
import com.pg85.otg.constants.Constants;
import com.pg85.otg.constants.SettingsEnums;
import com.pg85.otg.gen.resource.RegistryResource;
import com.pg85.otg.interfaces.IBiome;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.IWorldConfig;
import com.pg85.otg.util.biome.OTGBiomeResourceLocation;
import com.pg85.otg.util.biome.WeightedMobSpawnGroup;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.TemperatureModifier;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public class PaperBiome implements IBiome
{
	private final Biome biomeBase;
	private final IBiomeConfig biomeConfig;

	public PaperBiome(Biome biomeBase, IBiomeConfig biomeConfig)
	{
		this.biomeBase = biomeBase;
		this.biomeConfig = biomeConfig;
	}

	public Biome getBiome()
	{
		return this.biomeBase;
	}	
	
	@Override
	public IBiomeConfig getBiomeConfig()
	{
		return this.biomeConfig;
	}	
	
	public static Biome createOTGBiome (boolean isOceanBiome, IWorldConfig worldConfig, IBiomeConfig biomeConfig)
	{
		// BiomeSettingsGeneration.a == BiomeGenerationSettings.Builder in forge
		BiomeSettingsGeneration.a biomeGenerationSettingsBuilder = new BiomeSettingsGeneration.a();

		MobSpawnSettings.a mobSpawnInfoBuilder = createMobSpawnInfo(biomeConfig);
	
		// Surface/ground/stone blocks / sagc are done during base terrain gen.
		// a() == withSurfaceBuilder() in forge
		// WorldGenSurfaceComposites.j == ConfiguredSurfaceBuilders.field_244184_p in forge		
		// Spawn point detection checks for surfacebuilder blocks, so using ConfiguredSurfaceBuilders.GRASS.
		// TODO: What if there's no grass around spawn?
		biomeGenerationSettingsBuilder.a(WorldGenSurfaceComposites.j);

		// * Carvers are handled by OTG

		// Register any Registry() resources to the biome, to be handled by MC.
		for (ConfigFunction<IBiomeConfig> res : ((BiomeConfig)biomeConfig).getResourceQueue())
		{
			if (res instanceof RegistryResource)
			{
				RegistryResource registryResource = (RegistryResource)res;
				WorldGenStage.Decoration stage = WorldGenStage.Decoration.valueOf(registryResource.getDecorationStage());
				WorldGenFeatureConfigured<?, ?> registry = RegistryGeneration.e.get(new ResourceLocation(registryResource.getFeatureKey()));
				biomeGenerationSettingsBuilder.a(stage, registry);
			}
		}
		
		// Default structures
		addVanillaStructures(biomeGenerationSettingsBuilder, worldConfig, biomeConfig);

		float safeTemperature = biomeConfig.getBiomeTemperature();
		if (safeTemperature >= 0.1 && safeTemperature <= 0.2)
		{
			// Avoid temperatures between 0.1 and 0.2, Minecraft restriction
			safeTemperature = safeTemperature >= 1.5 ? 0.2f : 0.1f;
		}

		// BiomeFog == BiomeAmbient in forge
		BiomeFog.a biomeAmbienceBuilder =
			new BiomeFog.a() // fog, water, water fog, sky -> a, b, c, d
				.a(biomeConfig.getFogColor() != BiomeStandardValues.FOG_COLOR.getDefaultValue(null) ? biomeConfig.getFogColor() : worldConfig.getFogColor())
				.b(biomeConfig.getWaterColor() != BiomeStandardValues.WATER_COLOR.getDefaultValue() ? biomeConfig.getWaterColor() : 4159204)
				.c(biomeConfig.getWaterFogColor() != BiomeStandardValues.WATER_FOG_COLOR.getDefaultValue() ? biomeConfig.getWaterFogColor() : 329011)
				.d(biomeConfig.getSkyColor() != BiomeStandardValues.SKY_COLOR.getDefaultValue() ? biomeConfig.getSkyColor() : getSkyColorForTemp(safeTemperature))
				//.e() // TODO: Sky color is normally based on temp, make a setting for that?
		;


		Optional<Particle<?>> particleType = Registry.PARTICLE_TYPE.getOptional(new ResourceLocation(biomeConfig.getParticleType()));
		if (particleType.isPresent() && particleType.get() instanceof ParticleParam)
		{
			biomeAmbienceBuilder.a(new BiomeParticles((ParticleParam) particleType.get(), biomeConfig.getParticleProbability()));
		}

		Optional<SoundEvent> music = Registry.SOUND_EVENT.getOptional(new ResourceLocation(biomeConfig.getMusic()));
		music.ifPresent(soundEffect -> 
			biomeAmbienceBuilder.a(
				new Music(
					soundEffect,
					biomeConfig.getMusicMinDelay(),
					biomeConfig.getMusicMaxDelay(),
					biomeConfig.isReplaceCurrentMusic()
				)
			)
		);

		Optional<SoundEvent> ambientSound = Registry.SOUND_EVENT.getOptional(new ResourceLocation(biomeConfig.getAmbientSound()));
		ambientSound.ifPresent(soundEffect -> biomeAmbienceBuilder.a(ambientSound.get()));

		Optional<SoundEvent> moodSound = Registry.SOUND_EVENT.getOptional(new ResourceLocation(biomeConfig.getMoodSound()));
		moodSound.ifPresent(soundEffect -> 
			biomeAmbienceBuilder.a(
				new CaveSoundSettings(
					moodSound.get(),
					biomeConfig.getMoodSoundDelay(),
					biomeConfig.getMoodSearchRange(),
					biomeConfig.getMoodOffset()
				)
			)
		);

		Optional<SoundEvent> additionsSound = Registry.SOUND_EVENT.getOptional(new ResourceLocation(biomeConfig.getAdditionsSound()));
		additionsSound.ifPresent(soundEffect -> biomeAmbienceBuilder.a(new CaveSound(additionsSound.get(), biomeConfig.getAdditionsTickChance())));

		if (biomeConfig.getFoliageColor() != 0xffffff)
		{
			biomeAmbienceBuilder.e(biomeConfig.getFoliageColor());
		}

		if (biomeConfig.getGrassColor() != 0xffffff)
		{
			biomeAmbienceBuilder.f(biomeConfig.getGrassColor());
		}

		switch (biomeConfig.getGrassColorModifier())
		{
			case Swamp:
				biomeAmbienceBuilder.a(BiomeFog.GrassColor.SWAMP);
				break;
			case DarkForest:
				biomeAmbienceBuilder.a(BiomeFog.GrassColor.DARK_FOREST);
				break;
			default:
				break;
		}

		Biome.BiomeBuilder builder = new Biome.BiomeBuilder()
			// Precipitation
			.a(biomeConfig.getBiomeWetness() <= 0.0001 ? Biome.Precipitation.NONE :
				biomeConfig.getBiomeTemperature() > Constants.SNOW_AND_ICE_TEMP ? Biome.Precipitation.RAIN :
				Biome.Precipitation.SNOW)
			// depth
			.a(biomeConfig.getBiomeHeight())
			.b(biomeConfig.getBiomeVolatility())
			.c(safeTemperature)
			.d(biomeConfig.getBiomeWetness())
			// Ambience (colours/sounds)
			.a(biomeAmbienceBuilder.a())
			// Mob spawning
			.a(mobSpawnInfoBuilder.b())
			// All other biome settings...
			.a(biomeGenerationSettingsBuilder.a());

		if(biomeConfig.useFrozenOceanTemperature())
		{
			builder.a(TemperatureModifier.FROZEN);
		}		
		
		Biome.BiomeCategory category = Biome.BiomeCategory.a(biomeConfig.getBiomeCategory());
		builder.a(category != null ? category : isOceanBiome ? Biome.BiomeCategory.OCEAN : Biome.BiomeCategory.NONE);
		if (category == null)
		{
			if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.CONFIGS))
			{
				OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.CONFIGS, "Could not parse biome category "+biomeConfig.getBiomeCategory());
			}
		}

		return builder.a();
	}

	private static int getSkyColorForTemp (float safeTemperature)
	{
		float lvt_1_1_ = safeTemperature / 3.0F;
		// a == clamp
		lvt_1_1_ = MathHelper.a(lvt_1_1_, -1.0F, 1.0F);
		// f == hsvToRGB
		return MathHelper.f(0.62222224F - lvt_1_1_ * 0.05F, 0.5F + lvt_1_1_ * 0.1F, 1.0F);
	}

	private static void addVanillaStructures (BiomeSettingsGeneration.a biomeGenerationSettingsBuilder, IWorldConfig worldConfig, IBiomeConfig biomeConfig)
	{
		// TODO: Currently we can only enable/disable structures per biome and use any configuration options exposed by the vanilla structure 
		// classes (size for villages fe). If we want to be able to customise more, we'll need to implement our own structure classes.
		// TODO: Allow users to create their own jigsaw patterns (for villages, end cities, pillager outposts etc)?
		// TODO: Fossils?
		// TODO: Amethyst Geodes (1.17?)	
		// TODO: Misc structures: These structures generate even when the "Generate structures" world option is disabled, and also cannot be located with the /locate command.
		// - Dungeons
		// - Desert Wells

		// Villages
		// TODO: Allow spawning multiple types in a single biome?
		if (worldConfig.getVillagesEnabled() && biomeConfig.getVillageType() != SettingsEnums.VillageType.disabled)
		{
			int villageSize = biomeConfig.getVillageSize();
			SettingsEnums.VillageType villageType = biomeConfig.getVillageType();
			StructureFeature<WorldGenFeatureVillageConfiguration, ? extends StructureGenerator<WorldGenFeatureVillageConfiguration>> customVillage = register(
				((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("village").toResourceLocationString(),
				StructureGenerator.VILLAGE.a(
					new WorldGenFeatureVillageConfiguration(
						() -> {
							switch (villageType)
							{
								case sandstone:
									return WorldGenFeatureDesertVillage.a;
								case savanna:
									return WorldGenFeatureVillageSavanna.a;
								case taiga:
									return WorldGenFeatureVillageTaiga.a;
								case wood:
									return WorldGenFeatureVillagePlain.a;
								case snowy:
									return WorldGenFeatureVillageSnowy.a;
								case disabled: // Should never happen
									break;
							}
							return WorldGenFeatureVillagePlain.a;
						},
						villageSize
					)
				)
			);
			// a() == withStructure() in forge
			biomeGenerationSettingsBuilder.a(customVillage);
		}

		// Strongholds
		if (worldConfig.getStrongholdsEnabled() && biomeConfig.getStrongholdsEnabled())
		{
			biomeGenerationSettingsBuilder.a(StructureFeatures.k);
		}

		// Ocean Monuments
		if (worldConfig.getOceanMonumentsEnabled() && biomeConfig.getOceanMonumentsEnabled())
		{
			biomeGenerationSettingsBuilder.a(StructureFeatures.l);
		}

		// Rare buildings
		// TODO: Allow spawning multiple types in a single biome?
		if (worldConfig.getRareBuildingsEnabled() && biomeConfig.getRareBuildingType() != SettingsEnums.RareBuildingType.disabled)
		{
			switch (biomeConfig.getRareBuildingType())
			{
				case desertPyramid:
					biomeGenerationSettingsBuilder.a(StructureFeatures.DESERT_PYRAMID);
					break;
				case igloo:
					biomeGenerationSettingsBuilder.a(StructureFeatures.IGLOO);
					break;
				case jungleTemple:
					biomeGenerationSettingsBuilder.a(StructureFeatures.JUNGLE_TEMPLE);
					break;
				case swampHut:
					biomeGenerationSettingsBuilder.a(StructureFeatures.SWAMP_HUT);
					break;
				case disabled:
					break;
			}
		}

		// Woodland Mansions
		if (worldConfig.getWoodlandMansionsEnabled() && biomeConfig.getWoodlandMansionsEnabled())
		{
			biomeGenerationSettingsBuilder.a(StructureFeatures.d);
		}

		// Nether Fortresses
		if (worldConfig.getNetherFortressesEnabled() && biomeConfig.getNetherFortressesEnabled())
		{
			biomeGenerationSettingsBuilder.a(StructureFeatures.o);
		}

		// Mineshafts
		if (worldConfig.getMineshaftsEnabled() && biomeConfig.getMineShaftType() != SettingsEnums.MineshaftType.disabled)
		{
			float mineShaftProbability = biomeConfig.getMineShaftProbability();
			SettingsEnums.MineshaftType mineShaftType = biomeConfig.getMineShaftType();
			StructureFeature<WorldGenMineshaftConfiguration, ? extends StructureGenerator<WorldGenMineshaftConfiguration>> customMineShaft = register(
				((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("mineshaft").toResourceLocationString(),
				StructureGenerator.MINESHAFT.a(
					new WorldGenMineshaftConfiguration(
						mineShaftProbability,
						mineShaftType == SettingsEnums.MineshaftType.mesa ? WorldGenMineshaft.Type.MESA : WorldGenMineshaft.Type.NORMAL
					)
				)
			);
			biomeGenerationSettingsBuilder.a(customMineShaft);
		}

		// Buried Treasure
		if (worldConfig.getBuriedTreasureEnabled() && biomeConfig.getBuriedTreasureEnabled())
		{
			float buriedTreasureProbability = biomeConfig.getBuriedTreasureProbability();
			StructureFeature<WorldGenFeatureConfigurationChance, ? extends StructureGenerator<WorldGenFeatureConfigurationChance>> customBuriedTreasure = register(
				((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("buried_treasure").toResourceLocationString(),
				StructureGenerator.BURIED_TREASURE.a(new WorldGenFeatureConfigurationChance(buriedTreasureProbability))
			);
			biomeGenerationSettingsBuilder.a(customBuriedTreasure);
		}

		// Ocean Ruins
		if (worldConfig.getOceanRuinsEnabled() && biomeConfig.getOceanRuinsType() != SettingsEnums.OceanRuinsType.disabled)
		{
			float oceanRuinsLargeProbability = biomeConfig.getOceanRuinsLargeProbability();
			float oceanRuinsClusterProbability = biomeConfig.getOceanRuinsClusterProbability();
			SettingsEnums.OceanRuinsType oceanRuinsType = biomeConfig.getOceanRuinsType();
			StructureFeature<WorldGenFeatureOceanRuinConfiguration, ? extends StructureGenerator<WorldGenFeatureOceanRuinConfiguration>> customOceanRuins = register(
				((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("ocean_ruin").toResourceLocationString(),
				StructureGenerator.OCEAN_RUIN.a(
					new WorldGenFeatureOceanRuinConfiguration(
						oceanRuinsType == SettingsEnums.OceanRuinsType.cold ? WorldGenFeatureOceanRuin.Temperature.COLD : WorldGenFeatureOceanRuin.Temperature.WARM,
						oceanRuinsLargeProbability,
						oceanRuinsClusterProbability
					)
				)
			);
			biomeGenerationSettingsBuilder.a(customOceanRuins);
		}

		// Shipwrecks
		// TODO: Allowing both types in the same biome, make sure this won't cause problems.
		if (worldConfig.getShipWrecksEnabled())
		{
			if (biomeConfig.getShipWreckEnabled())
			{
				biomeGenerationSettingsBuilder.a(StructureFeatures.h);
			}
			if (biomeConfig.getShipWreckBeachedEnabled())
			{
				biomeGenerationSettingsBuilder.a(StructureFeatures.i);
			}
		}

		// Pillager Outpost
		if (worldConfig.getPillagerOutpostsEnabled() && biomeConfig.getPillagerOutpostEnabled())
		{
			int outpostSize = biomeConfig.getPillagerOutPostSize();
			StructureFeature<WorldGenFeatureVillageConfiguration, ? extends StructureGenerator<WorldGenFeatureVillageConfiguration>> customOutpost = register(
				((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("pillager_outpost").toResourceLocationString(),
				StructureGenerator.PILLAGER_OUTPOST.a(
					new WorldGenFeatureVillageConfiguration(
						() -> WorldGenFeaturePillagerOutpostPieces.a,
						outpostSize
					)
				)
			);
			biomeGenerationSettingsBuilder.a(customOutpost);
		}

		// Bastion Remnants
		if (worldConfig.getBastionRemnantsEnabled() && biomeConfig.getBastionRemnantEnabled())
		{
			int bastionRemnantSize = biomeConfig.getBastionRemnantSize();
			StructureFeature<WorldGenFeatureVillageConfiguration, ? extends StructureGenerator<WorldGenFeatureVillageConfiguration>> customBastionRemnant = register(
				((OTGBiomeResourceLocation)biomeConfig.getRegistryKey()).withBiomeResource("bastion_remnant").toResourceLocationString(),
				StructureGenerator.BASTION_REMNANT.a(
					new WorldGenFeatureVillageConfiguration(
						() -> WorldGenFeatureBastionPieces.a,
						bastionRemnantSize
					)
				)
			);
			biomeGenerationSettingsBuilder.a(customBastionRemnant);
		}

		// Nether Fossils
		if (worldConfig.getNetherFossilsEnabled() && biomeConfig.getNetherFossilEnabled())
		{
			biomeGenerationSettingsBuilder.a(StructureFeatures.NETHER_FOSSIL);
		}

		// End Cities
		if (worldConfig.getEndCitiesEnabled() && biomeConfig.getEndCityEnabled())
		{
			biomeGenerationSettingsBuilder.a(StructureFeatures.END_CITY);
		}

		// Ruined Portals
		if (worldConfig.getRuinedPortalsEnabled() && biomeConfig.getRuinedPortalType() != SettingsEnums.RuinedPortalType.disabled)
		{
			switch (biomeConfig.getRuinedPortalType())
			{
				case normal:
					biomeGenerationSettingsBuilder.a(StructureFeatures.RUINED_PORTAL_STANDARD);
					break;
				case desert:
					biomeGenerationSettingsBuilder.a(StructureFeatures.RUINED_PORTAL_DESERT);
					break;
				case jungle:
					biomeGenerationSettingsBuilder.a(StructureFeatures.RUINED_PORTAL_JUNGLE);
					break;
				case swamp:
					biomeGenerationSettingsBuilder.a(StructureFeatures.RUINED_PORTAL_SWAMP);
					break;
				case mountain:
					biomeGenerationSettingsBuilder.a(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
					break;
				case ocean:
					biomeGenerationSettingsBuilder.a(StructureFeatures.RUINED_PORTAL_OCEAN);
					break;
				case nether:
					biomeGenerationSettingsBuilder.a(StructureFeatures.RUINED_PORTAL_NETHER);
					break;
				case disabled:
					break;
			}
		}
	}

	// StructureFeatures.register()
	private static <FC extends WorldGenFeatureConfiguration, F extends StructureGenerator<FC>> StructureFeature<FC, F> register (String name, StructureFeature<FC, F> structure)
	{
		// RegistryGeneration == WorldGenRegistries
		// a() == register()
		// f == CONFIGURED_STRUCTURE_FEATURE
		return RegistryGeneration.a(RegistryGeneration.f, name, structure);
	}
	
	private static MobSpawnSettings.Builder createMobSpawnInfo (IBiomeConfig biomeConfig)
	{
		MobSpawnSettings.Builder mobSpawnInfoBuilder = new MobSpawnSettings.Builder();
		addMobGroup(MobCategory.MONSTER, mobSpawnInfoBuilder, biomeConfig.getMonsters(), biomeConfig.getName());
		addMobGroup(MobCategory.CREATURE, mobSpawnInfoBuilder, biomeConfig.getCreatures(), biomeConfig.getName());
		addMobGroup(MobCategory.WATER_CREATURE, mobSpawnInfoBuilder, biomeConfig.getWaterCreatures(), biomeConfig.getName());
		addMobGroup(MobCategory.AMBIENT, mobSpawnInfoBuilder, biomeConfig.getAmbientCreatures(), biomeConfig.getName());
		addMobGroup(MobCategory.WATER_AMBIENT, mobSpawnInfoBuilder, biomeConfig.getWaterAmbientCreatures(), biomeConfig.getName());
		addMobGroup(MobCategory.MISC, mobSpawnInfoBuilder, biomeConfig.getMiscCreatures(), biomeConfig.getName());

		// a() == isValidSpawnBiomeForPlayer()
		mobSpawnInfoBuilder.a();
		return mobSpawnInfoBuilder;
	}

	private static void addMobGroup(MobCategory creatureType, MobSpawnSettings.Builder mobSpawnInfoBuilder, List<WeightedMobSpawnGroup> mobSpawnGroupList, String biomeName)
	{
		for (WeightedMobSpawnGroup mobSpawnGroup : mobSpawnGroupList)
		{
			// a() == byKey() in forge
			Optional<EntityType<?>> entityType = EntityType.byString(mobSpawnGroup.getInternalName());
			if (entityType.isPresent())
			{
				// a() == withSpawner() in forge
				mobSpawnInfoBuilder.addSpawn(creatureType, new SpawnerData(entityType.get(), mobSpawnGroup.getWeight(), mobSpawnGroup.getMin(), mobSpawnGroup.getMax()));
			} else {
				if(OTG.getEngine().getLogger().getLogCategoryEnabled(LogCategory.MOBS))
				{
					OTG.getEngine().getLogger().log(LogLevel.ERROR, LogCategory.MOBS, "Could not find entity for mob: " + mobSpawnGroup.getMob() + " in BiomeConfig " + biomeName);
				}
			}
		}
	}

	@Override
	public float getTemperatureAt (int x, int y, int z)
	{
		return this.biomeBase.getTemperature(new BlockPos(x, y, z));
	}
}