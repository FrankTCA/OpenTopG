package com.pg85.otg.interfaces;

import com.pg85.otg.util.logging.LogCategory;
import com.pg85.otg.util.logging.LogLevel;

public interface ILogger {
    void init(LogLevel level, boolean logCustomObjects, boolean logStructurePlotting, boolean logConfigs, boolean logPerformance, boolean logBiomeRegistry, boolean logDecoration, boolean logMobs, String logPresets);

    boolean getLogCategoryEnabled(LogCategory category);

    void log(LogLevel level, LogCategory category, String message);

    void printStackTrace(LogLevel marker, LogCategory category, Exception e);

    boolean canLogForPreset(String presetFolderName);
}
