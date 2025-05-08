package org.astral.parkour_plugin;

import org.astral.parkour_plugin.command.GameCommandExecutor;
import org.astral.parkour_plugin.config.Configuration;
import org.astral.parkour_plugin.generator.Generator;
import org.astral.parkour_plugin.parkour.Parkour;
import org.astral.parkour_plugin.parkour.ParkourListener;
import org.astral.parkour_plugin.rankings.Score;
import org.astral.parkour_plugin.rankings.ScoreListener;
import org.astral.parkour_plugin.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Objects;

public final class Main extends JavaPlugin {

    private static Main instance;
    private Configuration configuration;
    private Parkour parkour;
    private Score score;
    private Generator generator;

    @Override
    public void onEnable() {
        //Plugin
        instance = this;

        //Cache
        Utils.loadCacheAndClear(instance);

        //Instances
        configuration = new Configuration();
        parkour = new Parkour();
        score = new Score();
        generator = new Generator();

        //Loaders
        parkour.loadCheckpoints();

        //Commands
        final PluginCommand command = instance.getCommand("parkour");
        Objects.requireNonNull(command).setAliases(Collections.singletonList("pk"));
        command.setExecutor(new GameCommandExecutor());

        //Events Registers
        instance.getServer().getPluginManager().registerEvents(new ParkourListener(), instance);
        instance.getServer().getPluginManager().registerEvents(new ScoreListener(), instance);
    }

    @Override
    public void onDisable() {
        Utils.clear();
        Bukkit.getOnlinePlayers().forEach(Gui::exitEditMode);
    }

    public static Main getInstance(){ return instance; }
    public Configuration getConfiguration(){ return configuration; }
    public Parkour getParkour(){ return parkour; }
    public Score getScore(){ return score; }
    public Generator getGenerator(){ return generator; }



}