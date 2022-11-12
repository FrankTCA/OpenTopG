package com.pg85.otg.interfaces;

import com.pg85.otg.util.nbt.NamedBinaryTag;

public interface IEntityFunction {
    double getX();

    int getY();

    double getZ();

    int getGroupSize();

    String getNameTagOrNBTFileName();

    String getResourceLocation();

    String getMetaData();

    NamedBinaryTag getNBTTag();

    String makeString();
}
