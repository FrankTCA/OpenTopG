package com.pg85.otg.interfaces;

import com.pg85.otg.constants.SettingsEnums.*;
import com.pg85.otg.util.biome.ReplaceBlockMatrix;
import com.pg85.otg.util.materials.LocalMaterialData;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

/**
 * WorldConfig.ini classes
 * <p>
 * IWorldConfig defines anything that's used/exposed between projects.
 * WorldConfigBase implements anything needed for IWorldConfig.
 * WorldConfig contains only fields/methods used for io/serialisation/instantiation.
 * <p>
 * WorldConfig should be used only in common-core and platform-specific layers, when reading/writing settings on app start.
 * IWorldConfig should be used wherever settings are used in code.
 */
public interface IWorldConfig {
    // Misc

    ConfigMode getSettingsMode();

    String getShortPresetName();

    int getMajorVersion();

    String getAuthor();

    String getDescription();

    // Visual settings

    int getFogColor();

    // Biome resources

    boolean isDisableOreGen();

    boolean getBedrockDisabled();

    boolean improvedBorderDecoration();

    // Blocks

    boolean getRemoveSurfaceStone();

    LocalMaterialData getWaterBlock();

    LocalMaterialData getBedrockBlockReplaced(ReplaceBlockMatrix replacedBlocks, int y);

    LocalMaterialData getDefaultBedrockBlock();

    LocalMaterialData getCooledLavaBlock();

    LocalMaterialData getIceBlock();

    LocalMaterialData getCarverLavaBlock();

    // Bedrock

    boolean getIsCeilingBedrock();

    boolean getIsFlatBedrock();

    int getCarverLavaBlockHeight();

    // Biome settings

    ArrayList<String> getWorldBiomes();

    List<String> getBlackListedBiomes();

    int getBiomeRarityScale();

    boolean getOldGroupRarity();

    boolean getOldLandRarity();

    int getGenerationDepth();

    int getLandFuzzy();

    int getLandRarity();

    int getLandSize();

    int getOceanBiomeSize();

    String getDefaultOceanBiome();

    String getDefaultWarmOceanBiome();

    String getDefaultLukewarmOceanBiome();

    String getDefaultColdOceanBiome();

    String getDefaultFrozenOceanBiome();

    BiomeMode getBiomeMode();

    double getFrozenOceanTemperature();

    List<String> getIsleBiomes();

    List<String> getBorderBiomes();

    boolean getIsRandomRivers();

    int getRiverRarity();

    int getRiverSize();

    boolean getRiversEnabled();

    boolean getBiomeConfigsHaveReplacement();

    boolean setBiomeConfigsHaveReplacement(boolean biomeConfigsHaveReplacement);

    // Terrain settings

    double getFractureHorizontal();

    double getFractureVertical();

    int getWorldHeightCap();

    int getWorldHeightScale();

    void setMaxSmoothRadius(int smoothRadius);

    int getMaxSmoothRadius();

    boolean isBetterSnowFall();

    int getWaterLevelMax();

    int getWaterLevelMin();

    // FromImageMode

    ImageOrientation getImageOrientation();

    String getImageFile();

    String getImageFillBiome();

    ImageMode getImageMode();

    int getImageZOffset();

    int getImageXOffset();

    // Vanilla structures

    boolean getWoodlandMansionsEnabled();

    boolean getNetherFortressesEnabled();

    boolean getBuriedTreasureEnabled();

    boolean getOceanRuinsEnabled();

    boolean getPillagerOutpostsEnabled();

    boolean getBastionRemnantsEnabled();

    boolean getNetherFossilsEnabled();

    boolean getEndCitiesEnabled();

    boolean getRuinedPortalsEnabled();

    boolean getShipWrecksEnabled();

    boolean getStrongholdsEnabled();

    boolean getVillagesEnabled();

    boolean getMineshaftsEnabled();

    boolean getOceanMonumentsEnabled();

    boolean getRareBuildingsEnabled();

    int getVillageSpacing();

    int getVillageSeparation();

    int getDesertPyramidSpacing();

    int getDesertPyramidSeparation();

    int getIglooSpacing();

    int getIglooSeparation();

    int getJungleTempleSpacing();

    int getJungleTempleSeparation();

    int getSwampHutSpacing();

    int getSwampHutSeparation();

    int getPillagerOutpostSpacing();

    int getPillagerOutpostSeparation();

    int getStrongholdSpacing();

    int getStrongholdSeparation();

    int getStrongHoldDistance();

    int getStrongHoldSpread();

    int getStrongHoldCount();

    int getOceanMonumentSpacing();

    int getOceanMonumentSeparation();

    int getWoodlandMansionSpacing();

    int getWoodlandMansionSeparation();

    int getBuriedTreasureSpacing();

    int getBuriedTreasureSeparation();

    int getMineshaftSpacing();

    int getMineshaftSeparation();

    int getRuinedPortalSpacing();

    int getRuinedPortalSeparation();

    int getShipwreckSpacing();

    int getShipwreckSeparation();

    int getOceanRuinSpacing();

    int getOceanRuinSeparation();

    int getEndCitySpacing();

    int getEndCitySeparation();

    int getBastionRemnantSpacing();

    int getBastionRemnantSeparation();

    int getNetherFortressSpacing();

    int getNetherFortressSeparation();

    int getNetherFossilSpacing();

    int getNetherFossilSeparation();

    // OTG Custom structures

    String getBO3AtSpawn();

    CustomStructureType getCustomStructureType();

    boolean getUseOldBO3StructureRarity();

    // TODO: Reimplement this, or forbid any spawning outside of decoration for 1.16.
    boolean doPopulationBoundsCheck();

    int getMaximumCustomStructureRadius();

    // Caves & Ravines

    boolean getCavesEnabled();

    int getCaveFrequency();

    int getCaveRarity();

    boolean isEvenCaveDistribution();

    int getCaveMinAltitude();

    int getCaveMaxAltitude();

    int getCaveSystemFrequency();

    int getIndividualCaveRarity();

    int getCaveSystemPocketMinSize();

    int getCaveSystemPocketChance();

    int getCaveSystemPocketMaxSize();

    boolean getRavinesEnabled();

    int getRavineRarity();

    int getRavineMinLength();

    int getRavineMaxLength();

    double getRavineDepth();

    int getRavineMinAltitude();

    int getRavineMaxAltitude();

    // Dimension settings

    OptionalLong getFixedTime();

    boolean getHasSkyLight();

    boolean getHasCeiling();

    boolean getUltraWarm();

    boolean getNatural();

    double getCoordinateScale();

    boolean getCreateDragonFight();

    boolean getPiglinSafe();

    boolean getBedWorks();

    boolean getRespawnAnchorWorks();

    boolean getHasRaids();

    int getLogicalHeight();

    String getInfiniburn();

    String getEffectsLocation();

    float getAmbientLight();

    // Portal settings

    List<LocalMaterialData> getPortalBlocks();

    String getPortalColor();

    String getPortalMob();

    String getPortalIgnitionSource();

    // Spawn point settings

    boolean getSpawnPointSet();

    int getSpawnPointX();

    int getSpawnPointY();

    int getSpawnPointZ();

    float getSpawnPointAngle();

    // Game rules

    boolean getOverrideGameRules();

    boolean getDoFireTick();

    boolean getMobGriefing();

    boolean getKeepInventory();

    boolean getDoMobSpawning();

    boolean getDoMobLoot();

    boolean getDoTileDrops();

    boolean getDoEntityDrops();

    boolean getCommandBlockOutput();

    boolean getNaturalRegeneration();

    boolean getDoDaylightCycle();

    boolean getLogAdminCommands();

    boolean getShowDeathMessages();

    int getRandomTickSpeed();

    boolean getSendCommandFeedback();

    boolean getSpectatorsGenerateChunks();

    int getSpawnRadius();

    boolean getDisableElytraMovementCheck();

    int getMaxEntityCramming();

    boolean getDoWeatherCycle();

    boolean getDoLimitedCrafting();

    int getMaxCommandChainLength();

    boolean getAnnounceAdvancements();

    boolean getDisableRaids();

    boolean getDoInsomnia();

    boolean getDrowningDamage();

    boolean getFallDamage();

    boolean getFireDamage();

    boolean getDoPatrolSpawning();

    boolean getDoTraderSpawning();

    boolean getForgiveDeadPlayers();

    boolean getUniversalAnger();

    boolean getForceLandAtSpawn();

    boolean getLargeOreVeins();
}
