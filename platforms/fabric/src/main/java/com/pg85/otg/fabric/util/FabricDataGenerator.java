package com.pg85.otg.fabric.util;

import com.pg85.otg.fabric.biome.FabricBiomeTags;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;

public class FabricDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator fdg) {
        fdg.addProvider(FabricBiomeTags::new);
    }
}
