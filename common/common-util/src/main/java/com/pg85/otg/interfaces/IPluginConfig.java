package com.pg85.otg.interfaces;

import com.pg85.otg.constants.SettingsEnums.ConfigMode;
import com.pg85.otg.constants.SettingsEnums.LogLevels;

/**
 * OTG.ini / PluginConfig classes
 * <p>
 * IPluginConfig defines anything that's used/exposed between projects.
 * PluginConfigBase implements anything needed for IWorldConfig.
 * PluginConfig contains only fields/methods used for io/serialisation/instantiation.
 * <p>
 * PluginConfig should be used only in common-core and platform-specific layers, when reading/writing settings on app start.
 * IPluginConfig should be used wherever settings are used in code.
 */
public interface IPluginConfig {
    LogLevels getLogLevel();

    int getMaxWorkerThreads();

    boolean getDeveloperModeEnabled();

    boolean logCustomObjects();

    boolean logStructurePlotting();

    boolean logConfigs();

    boolean logPerformance();

    boolean logDecoration();

    boolean logBiomeRegistry();

    boolean getDecorationEnabled();

    boolean logMobs();

    String logPresets();

    ConfigMode getSettingsMode();
}
