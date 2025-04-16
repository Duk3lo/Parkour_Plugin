package org.astral.parkour_plugin.Command;

import org.astral.parkour_plugin.Config.Configuration;
import org.astral.parkour_plugin.Generator.Generator;
import org.astral.parkour_plugin.Gui.Gui;
import org.astral.parkour_plugin.Gui.Tools.DynamicTools;
import org.astral.parkour_plugin.Main;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class GameCommandExecutor implements CommandExecutor, TabCompleter {

    // ------------------------------------------ [ Star Parkour ]
    private static final String go = "go";


    // ------------------------------------------- [ Editable ]
    // ------------------------------------------- [ 0 ]
    private static final String command1 = "generate";
    private static final String command2 = "tools";
    private static final String command3 = "help";

    private static final String command4 = "edit_mode";
    private static final String command5 = "Exit-Edit-Mode";

    // ------------------------------------------- [ 1 ]
    private static final String generation_item = "block_generate";

    // ------------------------------------------- [Instances]
    private static final Main plugin = Main.getInstance();
    private static final Generator generator = plugin.getGenerator();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 0){
            System.out.println("Not Support Yet");
            return false;
        }

        if (!(sender instanceof Player)){
            sender.sendMessage("Only Player Can Use this command");
            return false;
        }

        final Player player = ((Player) sender).getPlayer();

        if (player != null) {
            final Location location = player.getLocation();


            //Generator
            if (args[0].equalsIgnoreCase(command1)) {
                byte count = 3;
                String name = "Generator";
                if (args.length == 2){
                    try {
                        count = Byte.parseByte(args[1]);
                    }catch (Exception e){
                        plugin.getLogger().warning("Args need number: "+ args[1]);
                    }
                }
                if (args.length == 3) name = args[2];
                final String name_map = Configuration.getUniqueFolderName(name);
                Configuration.createMapFolder(name_map);

                generator.generatePlatformFloor(location, count, name_map);
                DynamicTools.SELECTS_MAPS_ITEMS.add(DynamicTools.createItemMap(name_map));
                Gui.refreshAllMaps();
                return true;
            }

            //Tools
            if (args[0].equalsIgnoreCase(command2)){
                if (args.length == 2){
                    if (args[1].equalsIgnoreCase(generation_item)){

                    }
                }
                return true;
            }

            if (args[0].equalsIgnoreCase(command3)){
                int sometime = 0;
                return true;
            }

            //Edit Mode
            if (args[0].equalsIgnoreCase(command4) && !Gui.isInEditMode(player)) {
                Gui.enterEditMode(player);
                return true;
            }
            if (args[0].equalsIgnoreCase(command5) && Gui.isInEditMode(player)) {
                Gui.exitEditMode(player);
                return true;
            }

        }
        return false;
    }

    public @NotNull @Unmodifiable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1){
            String commandOpen = "";
            if ((sender instanceof Player)){
                final Player player = ((Player) sender).getPlayer();
                commandOpen = !Gui.isInEditMode(player)? command4 : command5;
            }
            return filterByPrefix(args[0], Arrays.asList(command1, command2, command3, commandOpen));

        }

        //Generators
        if (args[0].contains(command1)) {
            if (args.length == 2) {
                final int max = 6;
                final int min = 2;
                final String value = String.valueOf(new Random().nextInt((max - min) + 1) + min);
                return filterByPrefix(args[1], Collections.singletonList(value));
            } else if (args.length == 3) {
                return filterByPrefix(args[2], Collections.singletonList("Map_Name"));
            }
        }

        //Items
        if (args[0].contains(command2)){
            if (args.length == 2) return filterByPrefix(args[1], Arrays.asList(command4, generation_item));
            if (args.length == 3) return filterByPrefix(args[1], Configuration.getMaps());
         }

        return Collections.emptyList();
    }

    private List<String> filterByPrefix(final String prefix, final @NotNull List<String> items) {
        return items.stream()
                .filter(item -> item.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}