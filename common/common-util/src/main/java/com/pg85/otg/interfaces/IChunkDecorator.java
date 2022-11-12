package com.pg85.otg.interfaces;

public interface IChunkDecorator {
    Object getLockingObject();

    boolean isDecorating();

    void beginSave();

    void endSave();

    boolean getIsSaveRequired();
}
