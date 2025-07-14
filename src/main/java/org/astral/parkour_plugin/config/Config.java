package org.astral.parkour_plugin.config;

import org.astral.parkour_plugin.Main;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.FileNotFoundException;
import java.io.IOException;

public final class Config {

    // Instances
    private static final Main plugin = Main.getInstance();
    private static final Configuration configuration = plugin.getConfiguration();

    // Files
    private static final String CONFIG = Configuration.CONFIG_YML;


    // Configuration
    private static final YamlConfiguration yamlConfiguration;

    //GENERAL SETTINGS

    //Paths
    public final static String modeDebug = "DEBUG_MODE";


    //EDIT MODE
    //Names
    public final static String romperBloques = "Destruir Bloques";
    public final static String copiarBloque = "Copiar Bloque (slots vacios)";
    public static final String bloquesFlotantes = "Bloques Flotantes al Interactuar";
    public static final String distaciaBloqueAire = "Distancia del bloque";

    //Paths
    private final static String base = "EDIT_MODE.";
    private final static String break_blocks = base+"Break_Bocks";
    private final static String copyBlocks = base+"Copy_Blocks";
    private final static String floatingBlocks = base+"Floating_Blocks";
    private final static String distanceBlockAir = base+"Distance_Block_Air";
    private final static String commands = base+"Commands";


    static {
        try {
            yamlConfiguration = configuration.getYamlConfiguration(CONFIG);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to load YAML configuration", e);
        }
    }

    //------------------------------------------------------Setters
    public static void setCopyBlocks(final boolean b){
        yamlConfiguration.set(break_blocks, b);
        saveConfiguration();
    }

    public static void setBreakBlocksEditMode(final boolean b){
        yamlConfiguration.set(copyBlocks, b);
        saveConfiguration();
    }

    public static void setFloatingBlocks(final boolean b){
        yamlConfiguration.set(floatingBlocks, b);
        saveConfiguration();
    }

    public static void setDistanceBlockAir(final int v){
        yamlConfiguration.set(distanceBlockAir, v);
        saveConfiguration();
    }


    //--------------------------------------------------------Getters
    //--------------------------------------------------------General
    public static boolean isDebugMode(){
        return yamlConfiguration.getBoolean(modeDebug, false);
    }

    //--------------------------------------------------------Edit_Mode
    public static boolean getBreakBlocksEditMode(){
        return yamlConfiguration.getBoolean(break_blocks, false);
    }

    public static boolean getCopyBlocks(){
        return yamlConfiguration.getBoolean(copyBlocks, false);
    }

    public static boolean getFloatingBlocks(){
        return yamlConfiguration.getBoolean(floatingBlocks, false);
    }

    public static int getDistanceBlockAir(){
        return yamlConfiguration.getInt(distanceBlockAir, 1);
    }

    public static boolean getCommands(){
        return yamlConfiguration.getBoolean(commands, true);
    }


    private static void saveConfiguration() {
        try {
            configuration.saveConfiguration(yamlConfiguration, CONFIG);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        }
    }
}
