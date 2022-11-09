package com.pg85.otg.core;

/**
 * Main entry-point. Used for logging and to access OTGEngine.
 * OTGEngine is implemented and provided by the platform-specific
 * layer and holds any objects and methods used during a session.
 */
public final class OTG {
    private static OTGEngine engine;

    private OTG() {
    }

    // Engine

    public static OTGEngine getEngine() {
        return engine;
    }

    public static void startEngine(OTGEngine engine) {
        if (OTG.engine != null) {
            throw new IllegalStateException("Engine is already set.");
        }

        OTG.engine = engine;
        engine.onStart();
    }

    public static void stopEngine() {
        engine.onShutdown();
        engine = null;
    }
}
