package com.pg85.otg.customobject.structures.bo4.smoothing;

class SmoothingAreaBlock {
    enum EnumSmoothingBlockType {
        FILLING,
        CUTTING
    }

    int x = 0;
    short y = -1;
    int z = 0;
    EnumSmoothingBlockType smoothingBlockType = null;

    public SmoothingAreaBlock() {
    }

    public SmoothingAreaBlock(int x, short y, int z, EnumSmoothingBlockType smoothingBlockType) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.smoothingBlockType = smoothingBlockType;
    }
}
