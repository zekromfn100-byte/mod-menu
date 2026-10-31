package com.example.spearmacro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class SpearMacroConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("spearmacro.json");

    public boolean macroEnabled = true;
    public boolean totemEnabled = true;

    public static SpearMacroConfig INSTANCE = new SpearMacroConfig();

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                SpearMacroConfig c = GSON.fromJson(Files.readString(FILE), SpearMacroConfig.class);
                if (c != null) INSTANCE = c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(INSTANCE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
