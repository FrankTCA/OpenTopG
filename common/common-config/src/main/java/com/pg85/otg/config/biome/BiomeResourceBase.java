package com.pg85.otg.config.biome;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.interfaces.IBiomeConfig;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a BiomeConfig ResourceQueue resource.
 */
public abstract class BiomeResourceBase extends ConfigFunction<IBiomeConfig> {
    static BiomeResourceBase createResource(IBiomeConfig config, ILogger logger, IMaterialReader materialReader, Class<? extends BiomeResourceBase> clazz, Object... args) {
        List<String> stringArgs = new ArrayList<String>(args.length);
        for (Object arg : args) {
            stringArgs.add(arg.toString());
        }

        try {
            return clazz.getConstructor(IBiomeConfig.class, List.class, ILogger.class, IMaterialReader.class).newInstance(config, stringArgs, logger, materialReader);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    // We're using reflection to match constructors for resources, so resource classes must implement this
    // constructor or createResource / com.pg85.otg.config.biome.BiomeResourcesManager.getConfigFunction() will fail.
    public BiomeResourceBase(IBiomeConfig biomeConfig, List<String> args, ILogger logger, IMaterialReader materialReader) {
    }
}
