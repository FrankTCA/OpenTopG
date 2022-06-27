package com.pg85.otg.config.settingType;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.interfaces.IMaterialReader;
import com.pg85.otg.interfaces.IWorldConfig;

/**
 * Same as a Setting, except it also provides a world config when reading. Necessary to fetch the min and max height of a preset.
 */
public abstract class BiomeSetting<T> extends Setting<T>{
    protected BiomeSetting(String name) {
        super(name);
    }

    public abstract T read(String stringValue, IMaterialReader materialReader, IWorldConfig worldConfig) throws InvalidConfigException;
}
