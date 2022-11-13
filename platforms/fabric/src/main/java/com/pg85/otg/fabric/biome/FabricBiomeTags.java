package com.pg85.otg.fabric.biome;

import com.pg85.otg.constants.Constants;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

//Biome tag manager
public class FabricBiomeTags extends FabricTagProvider<Biome> {

    private static final String TARGET_FOLDER = "biomes";
    private static final String TAG_MAN_NAME = "FabricBiomeTags";

    private static final TagKey<Biome> BIOME_TAG_KEYS =
            // TODO: BuiltinRegistries.BIOME.key() or Registries.BIOME_REGISTRY?
            TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(Constants.MOD_ID_SHORT, "biomes"));

    public FabricBiomeTags(FabricDataGenerator fdg) {
        super(fdg, BuiltinRegistries.BIOME, TARGET_FOLDER, TAG_MAN_NAME);
    }

    @Override
    protected void generateTags() {
        return;
    }

    public void addBiomeTag(Biome b) {
        getOrCreateTagBuilder(BIOME_TAG_KEYS).add(b);
    }
}
