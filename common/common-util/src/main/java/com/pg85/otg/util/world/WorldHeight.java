package com.pg85.otg.util.world;

public class WorldHeight {
    private final int minY;
    private final int maxY;

    public WorldHeight(int minY, int maxY)
    {

        this.minY = minY;
        this.maxY = maxY;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getHeight() {
        return maxY - minY;
    }
}
