package org.astral.parkour_plugin;

import org.astral.parkour_plugin.commands.GameCommandExecutor;
import org.astral.parkour_plugin.config.Configuration;
import org.astral.parkour_plugin.gui.editor.generator.Generator;
import org.astral.parkour_plugin.gui.Gui;
import org.astral.parkour_plugin.views.tag_name.ArmorStandApi;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Objects;

public final class Main extends JavaPlugin {

    private static Main instance;
    private Configuration configuration;
    private Generator generator;
    private ArmorStandApi armorStandApi;

    @Override
    public void onEnable() {
        //Plugin
        instance = this;

        //Instances
        configuration = new Configuration();
        generator = new Generator();
        armorStandApi = ArmorStandApi._view(instance);

        //Cache
        Utils.loadCacheAndClear(instance);

        //Commands
        final PluginCommand command = instance.getCommand("parkour");
        Objects.requireNonNull(command).setAliases(Collections.singletonList("pk"));
        command.setExecutor(new GameCommandExecutor());
    }

    @Override
    public void onDisable() {
        Utils.clear();
        Bukkit.getOnlinePlayers().forEach(Gui::exitEditMode);
    }

    public static Main getInstance(){ return instance; }
    public Configuration getConfiguration(){ return configuration; }
    public Generator getGenerator(){ return generator; }
    public ArmorStandApi getArmorStandApi(){ return armorStandApi; }

}