package com.pg85.otg.paper.networking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OTGClientSyncManager {
    private static final ConcurrentHashMap<String, BiomeSettingSyncWrapper> syncedData = new ConcurrentHashMap<>();

    public static Map<String, BiomeSettingSyncWrapper> getSyncedData() {
        return syncedData;
    }
}
