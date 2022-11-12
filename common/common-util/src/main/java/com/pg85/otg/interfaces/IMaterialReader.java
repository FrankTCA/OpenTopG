package com.pg85.otg.interfaces;

import com.pg85.otg.exceptions.InvalidConfigException;
import com.pg85.otg.util.materials.LocalMaterialData;
import com.pg85.otg.util.materials.LocalMaterialTag;

public interface IMaterialReader {
    LocalMaterialData readMaterial(String material) throws InvalidConfigException;

    LocalMaterialTag readTag(String tag) throws InvalidConfigException;
}
