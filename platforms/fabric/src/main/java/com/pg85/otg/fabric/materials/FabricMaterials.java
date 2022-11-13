package com.pg85.otg.fabric.materials;

import com.pg85.otg.util.materials.LocalMaterials;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FabricMaterials extends LocalMaterials {
    // Default blocks in given tags
    // Tags aren't loaded until datapacks are loaded, on world creation. We mirror the vanilla copy of the tag to solve this.
    private static final Block[] CORAL_BLOCKS_TAG = {Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK};
    private static final Block[] WALL_CORALS_TAG = {Blocks.TUBE_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.HORN_CORAL_WALL_FAN};
    private static final Block[] CORALS_TAG = {Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL, Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN};

    public static final Map<String, Block[]> OTG_BLOCK_TAGS = new HashMap<>();

    public static void init() {
        // Tags used for OTG configs
        // Since Spigot doesn't appear to allow registering custom tags, we have to implement our own tags logic :/.			
        // TODO: We should be including these via datapack and make sure we don't use tags before datapacks are loaded.

        OTG_BLOCK_TAGS.put("stone", new Block[]
                {
                        Blocks.STONE,
                        Blocks.GRANITE,
                        Blocks.DIORITE,
                        Blocks.ANDESITE
                });

        OTG_BLOCK_TAGS.put("dirt", new Block[]
                {
                        Blocks.DIRT,
                        Blocks.COARSE_DIRT,
                        Blocks.PODZOL,
                });

        OTG_BLOCK_TAGS.put("stained_clay", new Block[]
                {
                        Blocks.WHITE_TERRACOTTA,
                        Blocks.ORANGE_TERRACOTTA,
                        Blocks.MAGENTA_TERRACOTTA,
                        Blocks.LIGHT_BLUE_TERRACOTTA,
                        Blocks.YELLOW_TERRACOTTA,
                        Blocks.LIME_TERRACOTTA,
                        Blocks.PINK_TERRACOTTA,
                        Blocks.GRAY_TERRACOTTA,
                        Blocks.LIGHT_GRAY_TERRACOTTA,
                        Blocks.CYAN_TERRACOTTA,
                        Blocks.PURPLE_TERRACOTTA,
                        Blocks.BLUE_TERRACOTTA,
                        Blocks.BROWN_TERRACOTTA,
                        Blocks.GREEN_TERRACOTTA,
                        Blocks.RED_TERRACOTTA,
                        Blocks.BLACK_TERRACOTTA,
                });

        OTG_BLOCK_TAGS.put("log", new Block[]
                {
                        Blocks.DARK_OAK_LOG,
                        Blocks.DARK_OAK_WOOD,
                        Blocks.STRIPPED_DARK_OAK_LOG,
                        Blocks.STRIPPED_DARK_OAK_WOOD,
                        Blocks.OAK_LOG,
                        Blocks.OAK_WOOD,
                        Blocks.STRIPPED_OAK_LOG,
                        Blocks.STRIPPED_OAK_WOOD,
                        Blocks.ACACIA_LOG,
                        Blocks.ACACIA_WOOD,
                        Blocks.STRIPPED_ACACIA_LOG,
                        Blocks.STRIPPED_ACACIA_WOOD,
                        Blocks.BIRCH_LOG,
                        Blocks.BIRCH_WOOD,
                        Blocks.STRIPPED_BIRCH_LOG,
                        Blocks.STRIPPED_BIRCH_WOOD,
                        Blocks.JUNGLE_LOG,
                        Blocks.STRIPPED_JUNGLE_LOG,
                        Blocks.STRIPPED_JUNGLE_WOOD,
                        Blocks.SPRUCE_LOG,
                        Blocks.SPRUCE_WOOD,
                        Blocks.STRIPPED_SPRUCE_LOG,
                        Blocks.STRIPPED_SPRUCE_WOOD,
                        Blocks.CRIMSON_STEM,
                        Blocks.STRIPPED_CRIMSON_STEM,
                        Blocks.CRIMSON_HYPHAE,
                        Blocks.STRIPPED_CRIMSON_HYPHAE,
                        Blocks.WARPED_STEM,
                        Blocks.STRIPPED_WARPED_STEM,
                        Blocks.WARPED_HYPHAE,
                        Blocks.STRIPPED_WARPED_HYPHAE,
                });

        OTG_BLOCK_TAGS.put("air", new Block[]
                {
                        Blocks.AIR,
                        Blocks.CAVE_AIR,
                });

        OTG_BLOCK_TAGS.put("sandstone", new Block[]
                {
                        Blocks.SANDSTONE,
                        Blocks.CHISELED_SANDSTONE,
                        Blocks.SMOOTH_SANDSTONE,
                });

        OTG_BLOCK_TAGS.put("red_sandstone", new Block[]
                {
                        Blocks.RED_SANDSTONE,
                        Blocks.CHISELED_RED_SANDSTONE,
                        Blocks.SMOOTH_RED_SANDSTONE,
                });

        OTG_BLOCK_TAGS.put("long_grass", new Block[]
                {
                        Blocks.DEAD_BUSH,
                        Blocks.TALL_GRASS,
                        Blocks.FERN,
                });

        OTG_BLOCK_TAGS.put("red_flower", new Block[]
                {
                        Blocks.POPPY,
                        Blocks.BLUE_ORCHID,
                        Blocks.ALLIUM,
                        Blocks.AZURE_BLUET,
                        Blocks.RED_TULIP,
                        Blocks.ORANGE_TULIP,
                        Blocks.WHITE_TULIP,
                        Blocks.PINK_TULIP,
                        Blocks.OXEYE_DAISY,
                });

        OTG_BLOCK_TAGS.put("quartz_block", new Block[]
                {
                        Blocks.QUARTZ_BLOCK,
                        Blocks.CHISELED_QUARTZ_BLOCK,
                        Blocks.QUARTZ_PILLAR,
                });

        OTG_BLOCK_TAGS.put("prismarine", new Block[]
                {
                        Blocks.PRISMARINE,
                        Blocks.PRISMARINE_BRICKS,
                        Blocks.DARK_PRISMARINE,
                });

        OTG_BLOCK_TAGS.put("concrete", new Block[]
                {
                        Blocks.WHITE_CONCRETE,
                        Blocks.ORANGE_CONCRETE,
                        Blocks.MAGENTA_CONCRETE,
                        Blocks.LIGHT_BLUE_CONCRETE,
                        Blocks.YELLOW_CONCRETE,
                        Blocks.LIME_CONCRETE,
                        Blocks.PINK_CONCRETE,
                        Blocks.GRAY_CONCRETE,
                        Blocks.LIGHT_GRAY_CONCRETE,
                        Blocks.CYAN_CONCRETE,
                        Blocks.PURPLE_CONCRETE,
                        Blocks.BLUE_CONCRETE,
                        Blocks.BROWN_CONCRETE,
                        Blocks.GREEN_CONCRETE,
                        Blocks.RED_CONCRETE,
                        Blocks.BLACK_CONCRETE,
                });

        // Coral
        CORAL_BLOCKS = Arrays.stream(CORAL_BLOCKS_TAG).map(block -> FabricMaterialData.ofBlockData(block.defaultBlockState())).collect(Collectors.toList());
        WALL_CORALS = Arrays.stream(WALL_CORALS_TAG).map(block -> FabricMaterialData.ofBlockData(block.defaultBlockState())).collect(Collectors.toList());
        CORALS = Arrays.stream(CORALS_TAG).map(block -> FabricMaterialData.ofBlockData(block.defaultBlockState())).collect(Collectors.toList());

        // Blocks used in OTG code

        AIR = FabricMaterialData.ofBlockData(Blocks.AIR.defaultBlockState());
        CAVE_AIR = FabricMaterialData.ofBlockData(Blocks.CAVE_AIR.defaultBlockState());
        STRUCTURE_VOID = FabricMaterialData.ofBlockData(Blocks.STRUCTURE_VOID.defaultBlockState());
        COMMAND_BLOCK = FabricMaterialData.ofBlockData(Blocks.COMMAND_BLOCK.defaultBlockState());
        STRUCTURE_BLOCK = FabricMaterialData.ofBlockData(Blocks.STRUCTURE_BLOCK.defaultBlockState());
        GRASS = FabricMaterialData.ofBlockData(Blocks.GRASS_BLOCK.defaultBlockState());
        DIRT = FabricMaterialData.ofBlockData(Blocks.DIRT.defaultBlockState());
        CLAY = FabricMaterialData.ofBlockData(Blocks.CLAY.defaultBlockState());
        TERRACOTTA = FabricMaterialData.ofBlockData(Blocks.TERRACOTTA.defaultBlockState());
        WHITE_TERRACOTTA = FabricMaterialData.ofBlockData(Blocks.WHITE_TERRACOTTA.defaultBlockState());
        ORANGE_TERRACOTTA = FabricMaterialData.ofBlockData(Blocks.ORANGE_TERRACOTTA.defaultBlockState());
        YELLOW_TERRACOTTA = FabricMaterialData.ofBlockData(Blocks.YELLOW_TERRACOTTA.defaultBlockState());
        BROWN_TERRACOTTA = FabricMaterialData.ofBlockData(Blocks.BROWN_TERRACOTTA.defaultBlockState());
        RED_TERRACOTTA = FabricMaterialData.ofBlockData(Blocks.RED_TERRACOTTA.defaultBlockState());
        SILVER_TERRACOTTA = FabricMaterialData.ofBlockData(Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState());
        STONE = FabricMaterialData.ofBlockData(Blocks.STONE.defaultBlockState());
        DEEPSLATE = FabricMaterialData.ofBlockData(Blocks.DEEPSLATE.defaultBlockState());
        NETHERRACK = FabricMaterialData.ofBlockData(Blocks.NETHERRACK.defaultBlockState());
        END_STONE = FabricMaterialData.ofBlockData(Blocks.END_STONE.defaultBlockState());
        SAND = FabricMaterialData.ofBlockData(Blocks.SAND.defaultBlockState());
        RED_SAND = FabricMaterialData.ofBlockData(Blocks.RED_SAND.defaultBlockState());
        SANDSTONE = FabricMaterialData.ofBlockData(Blocks.SANDSTONE.defaultBlockState());
        RED_SANDSTONE = FabricMaterialData.ofBlockData(Blocks.RED_SANDSTONE.defaultBlockState());
        GRAVEL = FabricMaterialData.ofBlockData(Blocks.GRAVEL.defaultBlockState());
        MOSSY_COBBLESTONE = FabricMaterialData.ofBlockData(Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        SNOW = FabricMaterialData.ofBlockData(Blocks.SNOW.defaultBlockState());
        SNOW_BLOCK = FabricMaterialData.ofBlockData(Blocks.SNOW_BLOCK.defaultBlockState());
        TORCH = FabricMaterialData.ofBlockData(Blocks.TORCH.defaultBlockState());
        BEDROCK = FabricMaterialData.ofBlockData(Blocks.BEDROCK.defaultBlockState());
        MAGMA = FabricMaterialData.ofBlockData(Blocks.MAGMA_BLOCK.defaultBlockState());
        ICE = FabricMaterialData.ofBlockData(Blocks.ICE.defaultBlockState());
        PACKED_ICE = FabricMaterialData.ofBlockData(Blocks.PACKED_ICE.defaultBlockState());
        BLUE_ICE = FabricMaterialData.ofBlockData(Blocks.BLUE_ICE.defaultBlockState());
        FROSTED_ICE = FabricMaterialData.ofBlockData(Blocks.FROSTED_ICE.defaultBlockState());
        GLOWSTONE = FabricMaterialData.ofBlockData(Blocks.GLOWSTONE.defaultBlockState());
        MYCELIUM = FabricMaterialData.ofBlockData(Blocks.MYCELIUM.defaultBlockState());
        STONE_SLAB = FabricMaterialData.ofBlockData(Blocks.STONE_SLAB.defaultBlockState());
        AMETHYST_BLOCK = FabricMaterialData.ofBlockData(Blocks.AMETHYST_BLOCK.defaultBlockState());
        BUDDING_AMETHYST = FabricMaterialData.ofBlockData(Blocks.BUDDING_AMETHYST.defaultBlockState());
        CALCITE = FabricMaterialData.ofBlockData(Blocks.CALCITE.defaultBlockState());
        SMOOTH_BASALT = FabricMaterialData.ofBlockData(Blocks.SMOOTH_BASALT.defaultBlockState());
        SMALL_AMETHYST_BUD = FabricMaterialData.ofBlockData(Blocks.SMALL_AMETHYST_BUD.defaultBlockState());
        MEDIUM_AMETHYST_BUD = FabricMaterialData.ofBlockData(Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState());
        LARGE_AMETHYST_BUD = FabricMaterialData.ofBlockData(Blocks.LARGE_AMETHYST_BUD.defaultBlockState());
        AMETHYST_CLUSTER = FabricMaterialData.ofBlockData(Blocks.AMETHYST_CLUSTER.defaultBlockState());
        GRANITE = FabricMaterialData.ofBlockData(Blocks.GRANITE.defaultBlockState());
        TUFF = FabricMaterialData.ofBlockData(Blocks.TUFF.defaultBlockState());

        // Liquids
        WATER = FabricMaterialData.ofBlockData(Blocks.WATER.defaultBlockState());
        LAVA = FabricMaterialData.ofBlockData(Blocks.LAVA.defaultBlockState());

        // Trees
        ACACIA_LOG = FabricMaterialData.ofBlockData(Blocks.ACACIA_LOG.defaultBlockState());
        BIRCH_LOG = FabricMaterialData.ofBlockData(Blocks.BIRCH_LOG.defaultBlockState());
        DARK_OAK_LOG = FabricMaterialData.ofBlockData(Blocks.DARK_OAK_LOG.defaultBlockState());
        OAK_LOG = FabricMaterialData.ofBlockData(Blocks.OAK_LOG.defaultBlockState());
        SPRUCE_LOG = FabricMaterialData.ofBlockData(Blocks.SPRUCE_LOG.defaultBlockState());
        ACACIA_WOOD = FabricMaterialData.ofBlockData(Blocks.ACACIA_WOOD.defaultBlockState());
        BIRCH_WOOD = FabricMaterialData.ofBlockData(Blocks.BIRCH_WOOD.defaultBlockState());
        DARK_OAK_WOOD = FabricMaterialData.ofBlockData(Blocks.DARK_OAK_WOOD.defaultBlockState());
        OAK_WOOD = FabricMaterialData.ofBlockData(Blocks.OAK_WOOD.defaultBlockState());
        SPRUCE_WOOD = FabricMaterialData.ofBlockData(Blocks.SPRUCE_WOOD.defaultBlockState());
        STRIPPED_ACACIA_LOG = FabricMaterialData.ofBlockData(Blocks.STRIPPED_ACACIA_LOG.defaultBlockState());
        STRIPPED_BIRCH_LOG = FabricMaterialData.ofBlockData(Blocks.STRIPPED_BIRCH_LOG.defaultBlockState());
        STRIPPED_DARK_OAK_LOG = FabricMaterialData.ofBlockData(Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        STRIPPED_JUNGLE_LOG = FabricMaterialData.ofBlockData(Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState());
        STRIPPED_OAK_LOG = FabricMaterialData.ofBlockData(Blocks.STRIPPED_OAK_LOG.defaultBlockState());
        STRIPPED_SPRUCE_LOG = FabricMaterialData.ofBlockData(Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());

        ACACIA_LEAVES = FabricMaterialData.ofBlockData(Blocks.ACACIA_LEAVES.defaultBlockState());
        BIRCH_LEAVES = FabricMaterialData.ofBlockData(Blocks.BIRCH_LEAVES.defaultBlockState());
        DARK_OAK_LEAVES = FabricMaterialData.ofBlockData(Blocks.DARK_OAK_LEAVES.defaultBlockState());
        JUNGLE_LEAVES = FabricMaterialData.ofBlockData(Blocks.JUNGLE_LEAVES.defaultBlockState());
        OAK_LEAVES = FabricMaterialData.ofBlockData(Blocks.OAK_LEAVES.defaultBlockState());
        SPRUCE_LEAVES = FabricMaterialData.ofBlockData(Blocks.SPRUCE_LEAVES.defaultBlockState());

        // Plants
        POPPY = FabricMaterialData.ofBlockData(Blocks.POPPY.defaultBlockState());
        BLUE_ORCHID = FabricMaterialData.ofBlockData(Blocks.BLUE_ORCHID.defaultBlockState());
        ALLIUM = FabricMaterialData.ofBlockData(Blocks.ALLIUM.defaultBlockState());
        AZURE_BLUET = FabricMaterialData.ofBlockData(Blocks.AZURE_BLUET.defaultBlockState());
        RED_TULIP = FabricMaterialData.ofBlockData(Blocks.RED_TULIP.defaultBlockState());
        ORANGE_TULIP = FabricMaterialData.ofBlockData(Blocks.ORANGE_TULIP.defaultBlockState());
        WHITE_TULIP = FabricMaterialData.ofBlockData(Blocks.WHITE_TULIP.defaultBlockState());
        PINK_TULIP = FabricMaterialData.ofBlockData(Blocks.PINK_TULIP.defaultBlockState());
        OXEYE_DAISY = FabricMaterialData.ofBlockData(Blocks.OXEYE_DAISY.defaultBlockState());
        YELLOW_FLOWER = FabricMaterialData.ofBlockData(Blocks.DANDELION.defaultBlockState());
        DEAD_BUSH = FabricMaterialData.ofBlockData(Blocks.DEAD_BUSH.defaultBlockState());
        FERN = FabricMaterialData.ofBlockData(Blocks.FERN.defaultBlockState());
        LONG_GRASS = FabricMaterialData.ofBlockData(Blocks.GRASS.defaultBlockState());

        RED_MUSHROOM_BLOCK = FabricMaterialData.ofBlockData(Blocks.RED_MUSHROOM_BLOCK.defaultBlockState());
        BROWN_MUSHROOM_BLOCK = FabricMaterialData.ofBlockData(Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState());
        RED_MUSHROOM = FabricMaterialData.ofBlockData(Blocks.RED_MUSHROOM.defaultBlockState());
        BROWN_MUSHROOM = FabricMaterialData.ofBlockData(Blocks.BROWN_MUSHROOM.defaultBlockState());

        DOUBLE_TALL_GRASS_LOWER = FabricMaterialData.ofBlockData(Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        DOUBLE_TALL_GRASS_UPPER = FabricMaterialData.ofBlockData(Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        LARGE_FERN_LOWER = FabricMaterialData.ofBlockData(Blocks.LARGE_FERN.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        LARGE_FERN_UPPER = FabricMaterialData.ofBlockData(Blocks.LARGE_FERN.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        LILAC_LOWER = FabricMaterialData.ofBlockData(Blocks.LILAC.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        LILAC_UPPER = FabricMaterialData.ofBlockData(Blocks.LILAC.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        PEONY_LOWER = FabricMaterialData.ofBlockData(Blocks.PEONY.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        PEONY_UPPER = FabricMaterialData.ofBlockData(Blocks.PEONY.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        ROSE_BUSH_LOWER = FabricMaterialData.ofBlockData(Blocks.ROSE_BUSH.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        ROSE_BUSH_UPPER = FabricMaterialData.ofBlockData(Blocks.ROSE_BUSH.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        SUNFLOWER_LOWER = FabricMaterialData.ofBlockData(Blocks.SUNFLOWER.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        SUNFLOWER_UPPER = FabricMaterialData.ofBlockData(Blocks.SUNFLOWER.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));

        ACACIA_SAPLING = FabricMaterialData.ofBlockData(Blocks.ACACIA_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1));
        BAMBOO_SAPLING = FabricMaterialData.ofBlockData(Blocks.BAMBOO_SAPLING.defaultBlockState());
        BIRCH_SAPLING = FabricMaterialData.ofBlockData(Blocks.BIRCH_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1));
        DARK_OAK_SAPLING = FabricMaterialData.ofBlockData(Blocks.DARK_OAK_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1));
        JUNGLE_SAPLING = FabricMaterialData.ofBlockData(Blocks.JUNGLE_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1));
        OAK_SAPLING = FabricMaterialData.ofBlockData(Blocks.OAK_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1));
        SPRUCE_SAPLING = FabricMaterialData.ofBlockData(Blocks.SPRUCE_SAPLING.defaultBlockState().setValue(SaplingBlock.STAGE, 1));

        PUMPKIN = FabricMaterialData.ofBlockData(Blocks.PUMPKIN.defaultBlockState());
        CACTUS = FabricMaterialData.ofBlockData(Blocks.CACTUS.defaultBlockState());
        MELON_BLOCK = FabricMaterialData.ofBlockData(Blocks.MELON.defaultBlockState());
        VINE = FabricMaterialData.ofBlockData(Blocks.VINE.defaultBlockState());
        WATER_LILY = FabricMaterialData.ofBlockData(Blocks.LILY_PAD.defaultBlockState());
        SUGAR_CANE_BLOCK = FabricMaterialData.ofBlockData(Blocks.SUGAR_CANE.defaultBlockState());
        BlockState bambooState = Blocks.BAMBOO.defaultBlockState().setValue(BambooBlock.AGE, 1).setValue(BambooBlock.LEAVES, BambooLeaves.NONE).setValue(BambooBlock.STAGE, 0);
        BAMBOO = FabricMaterialData.ofBlockData(bambooState);
        BAMBOO_SMALL = FabricMaterialData.ofBlockData(bambooState.setValue(BambooBlock.LEAVES, BambooLeaves.SMALL));
        BAMBOO_LARGE = FabricMaterialData.ofBlockData(bambooState.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE));
        BAMBOO_LARGE_GROWING = FabricMaterialData.ofBlockData(bambooState.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE).setValue(BambooBlock.STAGE, 1));
        PODZOL = FabricMaterialData.ofBlockData(Blocks.PODZOL.defaultBlockState());
        SEAGRASS = FabricMaterialData.ofBlockData(Blocks.SEAGRASS.defaultBlockState());
        TALL_SEAGRASS_LOWER = FabricMaterialData.ofBlockData(Blocks.TALL_SEAGRASS.defaultBlockState().setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.LOWER));
        TALL_SEAGRASS_UPPER = FabricMaterialData.ofBlockData(Blocks.TALL_SEAGRASS.defaultBlockState().setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER));
        KELP = FabricMaterialData.ofBlockData(Blocks.KELP.defaultBlockState());
        KELP_PLANT = FabricMaterialData.ofBlockData(Blocks.KELP_PLANT.defaultBlockState());
        VINE_SOUTH = FabricMaterialData.ofBlockData(Blocks.VINE.defaultBlockState().setValue(VineBlock.SOUTH, true));
        VINE_NORTH = FabricMaterialData.ofBlockData(Blocks.VINE.defaultBlockState().setValue(VineBlock.NORTH, true));
        VINE_WEST = FabricMaterialData.ofBlockData(Blocks.VINE.defaultBlockState().setValue(VineBlock.WEST, true));
        VINE_EAST = FabricMaterialData.ofBlockData(Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, true));
        SEA_PICKLE = FabricMaterialData.ofBlockData(Blocks.SEA_PICKLE.defaultBlockState());

        // Ores
        COAL_ORE = FabricMaterialData.ofBlockData(Blocks.COAL_ORE.defaultBlockState());
        DIAMOND_ORE = FabricMaterialData.ofBlockData(Blocks.DIAMOND_ORE.defaultBlockState());
        EMERALD_ORE = FabricMaterialData.ofBlockData(Blocks.EMERALD_ORE.defaultBlockState());
        GOLD_ORE = FabricMaterialData.ofBlockData(Blocks.GOLD_ORE.defaultBlockState());
        IRON_ORE = FabricMaterialData.ofBlockData(Blocks.IRON_ORE.defaultBlockState());
        COPPER_ORE = FabricMaterialData.ofBlockData(Blocks.COPPER_ORE.defaultBlockState());
        LAPIS_ORE = FabricMaterialData.ofBlockData(Blocks.LAPIS_ORE.defaultBlockState());
        QUARTZ_ORE = FabricMaterialData.ofBlockData(Blocks.NETHER_QUARTZ_ORE.defaultBlockState());
        REDSTONE_ORE = FabricMaterialData.ofBlockData(Blocks.REDSTONE_ORE.defaultBlockState());

        // Ore blocks
        GOLD_BLOCK = FabricMaterialData.ofBlockData(Blocks.GOLD_BLOCK.defaultBlockState());
        IRON_BLOCK = FabricMaterialData.ofBlockData(Blocks.IRON_BLOCK.defaultBlockState());
        REDSTONE_BLOCK = FabricMaterialData.ofBlockData(Blocks.REDSTONE_BLOCK.defaultBlockState());
        DIAMOND_BLOCK = FabricMaterialData.ofBlockData(Blocks.DIAMOND_BLOCK.defaultBlockState());
        LAPIS_BLOCK = FabricMaterialData.ofBlockData(Blocks.LAPIS_BLOCK.defaultBlockState());
        COAL_BLOCK = FabricMaterialData.ofBlockData(Blocks.COAL_BLOCK.defaultBlockState());
        QUARTZ_BLOCK = FabricMaterialData.ofBlockData(Blocks.QUARTZ_BLOCK.defaultBlockState());
        EMERALD_BLOCK = FabricMaterialData.ofBlockData(Blocks.EMERALD_BLOCK.defaultBlockState());
        BERRY_BUSH = FabricMaterialData.ofBlockData(Blocks.SWEET_BERRY_BUSH.defaultBlockState());
        RAW_IRON_BLOCK = FabricMaterialData.ofBlockData(Blocks.RAW_IRON_BLOCK.defaultBlockState());
        RAW_COPPER_BLOCK = FabricMaterialData.ofBlockData(Blocks.RAW_COPPER_BLOCK.defaultBlockState());
    }
}
