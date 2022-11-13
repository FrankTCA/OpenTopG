package com.pg85.otg.fabric;

import com.pg85.otg.constants.Constants;
import com.pg85.otg.core.OTGEngine;
import com.pg85.otg.fabric.materials.FabricMaterials;
import com.pg85.otg.fabric.presets.FabricPresetLoader;
import com.pg85.otg.fabric.util.FabricLogger;
import com.pg85.otg.fabric.util.FabricModLoadedChecker;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class FabricEngine extends OTGEngine {
    private final OTGPlugin plugin;

    protected FabricEngine(OTGPlugin plugin) {
        super(
                new FabricLogger(),
                Paths.get(FabricLoader.getInstance().getConfigDir().toString(),
                        File.separator + Constants.MOD_ID),
                new FabricModLoadedChecker(),
                new FabricPresetLoader(Paths.get(FabricLoader.getInstance().getConfigDir().toString(),
                        File.separator + Constants.MOD_ID))
        );
        this.plugin = plugin;
    }

    @Override
    public void onStart() {
        FabricMaterials.init();
        super.onStart();
    }

    public OTGPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public File getJarFile() {
        String fileName = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        // URLEncoded string, decode.
        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

        if (fileName != null) {
            File modFile = new File(fileName);
            if (modFile.isFile()) {
                return modFile;
            }
        }
        return null;
    }
}
