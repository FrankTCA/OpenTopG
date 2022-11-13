package com.pg85.otg.fabric.mixins;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTG;
import com.pg85.otg.fabric.dimensions.OTGDimensionTypeHelper;
import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.dedicated.DedicatedServerProperties.WorldGenProperties;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalLong;
import java.util.Random;

@Mixin(WorldGenSettings.class)
public abstract class WorldGenSettingsMixin {
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void injectWorldCreation(RegistryAccess ra, WorldGenProperties wgp, CallbackInfoReturnable<WorldGenSettings> cir)
    {
        String levelType = wgp.levelType().trim().toLowerCase();

        //for the otg level type
        if(levelType.equals(Constants.MOD_ID_SHORT)) {
            //does this work this early?
            OTG.getEngine().getLogger().log(LogLevel.INFO, LogCategory.MAIN, "Loading OTG world...");

            //parse the seed
            OptionalLong parsedInputtedSeed = WorldGenSettings.parseSeed(wgp.levelSeed());
            long seed = 0;
            if(parsedInputtedSeed.isEmpty()) {
                seed = (new Random()).nextLong(); //gen random seed if none given
            }
            else {
                seed = parsedInputtedSeed.getAsLong(); //otherwise use the inputted seed
            }
            try {
                wgp.generatorSettings().get("Preset").getAsString();
                cir.setReturnValue(OTGDimensionTypeHelper.createOTGSettings(ra, seed, true, false, wgp.generatorSettings().get("Preset").getAsString()));
            } catch (ClassCastException | IllegalStateException ex) {
                OTG.getEngine().getLogger().log(LogLevel.WARN, LogCategory.MAIN, "[OTG] >> If you are on a server, please use {\"Preset\": \"PresetFolderName\"} in your `generator-settings` in server.properties.");
                OTG.getEngine().getLogger().log(LogLevel.WARN, LogCategory.MAIN, "[OTG] >> Defaulting to Default preset.");
                cir.setReturnValue(OTGDimensionTypeHelper.createOTGSettings(ra, seed, true, false, "Default"));
            }

        }
    }

}
