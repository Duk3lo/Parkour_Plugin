package org.astral.parkour_plugin.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.astral.parkour_plugin.compatibilizer.adapters.SoundApi;
import org.astral.parkour_plugin.config.Configuration;
import org.astral.parkour_plugin.gui.editor.generator.Generator;
import org.astral.parkour_plugin.gui.Gui;
import org.astral.parkour_plugin.gui.tools.DynamicTools;
import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.parkour.ParkourManager;
import org.astral.parkour_plugin.parkour.Type.ParkourMapStateGlobal;
import org.astral.parkour_plugin.parkour.Type.ParkourMapStateIndividual;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;

public final class GameCommandExecutor implements CommandExecutor, TabCompleter {

    // ------------------------------------------ [ Parkour ]
    private static final String startGlobal = "start_parkour_global";
    private static final String startIndividual = "start_parkour_individual";
    private static final String exit = "exit";
    private static final String finish = "finish";
    private static final String openGlobalLobby = "open_global_lobby";
    private static final String stop = "stop";
    private static final String pause = "pause";
    private static final String resume = "resume";

    // ------------------------------------------- [ Editable ]
    // ------------------------------------------- [ 0 ]
    private static final String generate = "generate";
    private static final String tools = "tools";
    private static final String help = "help";

    private static final String editMode = "edit_mode";
    private static final String exitEditMode = "Exit-Edit-Type";

    // ------------------------------------------- [ 1 ]
    private static final String generation_item = "block_generate";
    private static final String here = "here";

    // ------------------------------------------- [Instances]
    private static final Main plugin = Main.getInstance();
    private static final Generator generator = plugin.getGenerator();

    // ------------------------------------------- [Confirm Dialogue]
    private final Map<UUID, String> pendingJoin = new HashMap<>();

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

        Player player = ((Player) sender).getPlayer();

        if (player != null) {
            final Location location = player.getLocation();
            final UUID uuid = player.getUniqueId();

            if (args[0].equalsIgnoreCase("confirmjoin")) {
                String stored = pendingJoin.remove(uuid);
                if (stored != null) {
                    String[] parts = stored.split(":");
                    String map = parts[0];
                    String type = parts[1];
                    boolean animated = parts.length >= 3 && Boolean.parseBoolean(parts[2]);
                    boolean isHere = parts.length >= 4 && Boolean.parseBoolean(parts[3]);

                    ParkourManager.removePlayerParkour(uuid);

                    if (type.equalsIgnoreCase("Global")) {
                        ParkourManager.startParkourGlobal(player, map, animated);
                    } else {
                        ParkourManager.starParkourIndividual(player, map, animated, isHere);
                    }
                } else {
                    player.sendMessage("§cNo tienes ninguna solicitud pendiente.");
                }
                return true;
            }

            //Generator
            if (args[0].equalsIgnoreCase(generate)) {
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


            if (args[0].equalsIgnoreCase(tools)){
                if (args.length == 2){
                    if (args[1].equalsIgnoreCase(generation_item)){
                        return true;
                    }
                }
                return true;
            }

            if (args[0].equalsIgnoreCase(help)){
                return true;
            }

            //Edit Type
            if (args[0].equalsIgnoreCase(editMode) && !Gui.isInEditMode(player)) {
                Gui.enterEditMode(player);
                return true;
            }
            if (args[0].equalsIgnoreCase(exitEditMode) && Gui.isInEditMode(player)) {
                Gui.exitGui(player);
                return true;
            }

            if (args[0].equalsIgnoreCase(startGlobal) || args[0].equalsIgnoreCase(startIndividual)) {
                if (args.length >= 2) {
                    String nameMap = args[1];
                    if (!Configuration.getMaps().contains(nameMap)) {
                        sender.sendMessage("No existe el mapa: " + nameMap);
                        return true;
                    }
                    boolean animated = args.length >= 3 && args[2].equalsIgnoreCase("title:a");

                    String mapToJoin = args[1];
                        boolean isHere = false;

                    if (args[0].equalsIgnoreCase(startGlobal)) {
                        if (args.length >= 4) {
                            player = Bukkit.getPlayer(args[3]);
                            if (player == null) {
                                sender.sendMessage("No existe jugador: " + args[3]);
                                return true;
                            }
                        }
                    } else {
                        if (args.length >= 4 && args[3].equalsIgnoreCase(here)) {
                            isHere = true;
                            if (args.length >= 5) {
                                player = Bukkit.getPlayer(args[4]);
                                if (player == null) {
                                    sender.sendMessage("No existe jugador: " + args[4]);
                                    return true;
                                }
                            }
                        } else if (args.length >= 4) {
                            player = Bukkit.getPlayer(args[3]);
                            if (player == null) {
                                sender.sendMessage("No existe jugador: " + args[3]);
                                return true;
                            }
                        }
                    }

                    if (Gui.isInEditMode(player)) Gui.exitGui(player);

                    if (args[0].equalsIgnoreCase(startGlobal)) {
                        ParkourMapStateGlobal state = ParkourManager.getMapStateGlobal(nameMap);
                        if (state != null && state.containsPlayer(uuid)) {
                            player.sendMessage("§eYa estás en este Parkour Global");
                            return true;
                        }
                        if (ParkourManager.getMapIfInParkour(uuid).isPresent()) {
                            sendJoinConfirmation(player, nameMap, "Global", animated, isHere);
                            return true;
                        }
                        ParkourManager.startParkourGlobal(player, mapToJoin, animated);
                    } else {
                        ParkourMapStateIndividual stateIndividual = ParkourManager.getMapStateIndividual(uuid);
                        if (stateIndividual != null && stateIndividual.getName().equals(nameMap)) {
                            player.sendMessage("§eYa estás en este Parkour Individual");
                            return true;
                        }
                        if (ParkourManager.getMapIfInParkour(uuid).isPresent()) {
                            sendJoinConfirmation(player, nameMap, "Individual:" + isHere, animated, isHere);
                            return true;
                        }
                        ParkourManager.starParkourIndividual(player, mapToJoin, animated, isHere);
                    }
                }
                return true;
            }

            if (args[0].equalsIgnoreCase(stop) || args[0].equalsIgnoreCase(pause) || args[0].equalsIgnoreCase(resume)) {
                if (args.length >= 2) {
                    String[] parts = args[1].split(":");
                    if (parts.length != 2) {
                        player.sendMessage("§cNo es un dato válido. Usa m:<mapa> o p:<jugador>");
                        return true;
                    }

                    String type = parts[0];
                    String val = parts[1];

                    if (type.equalsIgnoreCase("m")) {
                        if (!ParkourManager.getAllGlobalMaps().contains(val)) {
                            player.sendMessage("§cNo existe el mapa: " + val);
                            return true;
                        }

                        if (args[0].equalsIgnoreCase(stop)) {
                            ParkourManager.stopGlobal(val);
                        } else if (args[0].equalsIgnoreCase(pause)) {
                            ParkourManager.pauseGlobal(val);
                        } else if (args[0].equalsIgnoreCase(resume)) {
                            ParkourManager.resumeGlobal(val);
                        }

                    } else if (type.equalsIgnoreCase("p")) {
                        UUID target = Bukkit.getPlayer(val).getUniqueId();
                        if (target == null) {
                            player.sendMessage("§cNo existe el jugador: " + val);
                            return true;
                        }

                        if (args[0].equalsIgnoreCase(stop)) {
                            ParkourManager.removePlayerParkour(target);
                        } else if (args[0].equalsIgnoreCase(pause)) {
                            ParkourManager.pauseIndividual(target);
                        } else if (args[0].equalsIgnoreCase(resume)) {
                            ParkourManager.resumeIndividual(target);
                        }

                    } else {
                        player.sendMessage("§cNo es un tipo válido. Usa m:<mapa> o p:<jugador>");
                    }

                    return true;
                }
            }

            if (args[0].equalsIgnoreCase(exit)) {
                if (args.length == 2) {
                    player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage("§cNo existe el jugador: §f" + args[1]);
                        return true;
                    }
                }
                final String map = ParkourManager.getMapIfInParkour(player.getUniqueId()).orElse(null);
                if (map == null) {
                    sender.sendMessage("§e" + player.getName() + " no está dentro de ningún mapa de parkour.");
                    return true;
                }
                ParkourManager.removePlayerParkour(player.getUniqueId());
                sender.sendMessage("§a" + player.getName() + " ha salido del parkour §b" + map + "§a.");
                return true;
            }

            if (args[0].equalsIgnoreCase(finish)){
                final String map = ParkourManager.getMapIfInParkour(player.getUniqueId()).orElse(null);
                if (map == null) {
                    sender.sendMessage("§e" + player.getName() + " no está dentro de ningún mapa de parkour.");
                    return true;
                }
                ParkourManager.finish(player);
                return true;
            }

            if (args[0].equalsIgnoreCase(openGlobalLobby)){
                Gui.loadInventorySelectorGlobal(player);
            }

        }
        return false;
    }

    private void sendJoinConfirmation(@NotNull Player player, String newMapName, String type, boolean animated, boolean isHere) {
        pendingJoin.put(player.getUniqueId(), newMapName + ":" + type + ":" + animated + ":" + isHere);

        TextComponent message = new TextComponent("§eYa estás en otro parkour. ¿Quieres salir y unirte al §b"
                + type + " §a" + newMapName + "§e?");
        message.addExtra("\n");

        TextComponent yes = new TextComponent("§a[✔ Sí]");
        yes.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click para salir y unirte a " + type + " " + newMapName
                        + (animated ? " §d(con título animado)" : " §7(sin título animado)")
                        + (isHere ? " §b(aquí)" : "")).create()));
        yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/parkour confirmjoin"));

        TextComponent space = new TextComponent("   ");

        TextComponent no = new TextComponent("§c[✘ No]");
        no.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click para cancelar").create()));
        no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/parkour canceljoin"));

        message.addExtra(yes);
        message.addExtra(space);
        message.addExtra(no);

        player.spigot().sendMessage(message);
        SoundApi.playSound(player, 1.0f, 2.0f, "ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    public @NotNull @Unmodifiable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1){
            String commandEditMode = "";
            if ((sender instanceof Player)){
                final Player player = ((Player) sender).getPlayer();
                commandEditMode = !Gui.isInEditMode(player)? editMode : exitEditMode;
            }
            return filterByPrefix(args[0], Arrays.asList(generate, tools, help, commandEditMode, startGlobal, startIndividual,exit, finish, openGlobalLobby, pause ,stop, resume));

        }

        //Generators
        if (args[0].equalsIgnoreCase(generate)) {
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
        if (args[0].equalsIgnoreCase(tools)){
            if (args.length == 2) return filterByPrefix(args[1], Arrays.asList(editMode, generation_item));
            if (args.length == 3) return filterByPrefix(args[1], Configuration.getMaps());
         }

        if (args[0].equalsIgnoreCase(startGlobal) || args[0].equalsIgnoreCase(startIndividual)) {
            if (args.length == 2) {

                return filterByPrefix(args[1], Configuration.getMaps());
            }
            if (args.length == 3) {

                return filterByPrefix(args[2], Arrays.asList("title:a", "title:n"));
            }
            if (args.length == 4 && args[0].equalsIgnoreCase(startIndividual)) {

                if (here.equalsIgnoreCase(args[3])) {
                    return Collections.emptyList();
                }
                return filterByPrefix(args[3], Collections.singletonList(here));
            }

            if ((args.length == 4 && args[0].equalsIgnoreCase(startGlobal))
                    || (args.length == 5 && args[0].equalsIgnoreCase(startIndividual))) {
                List<String> suggestions = Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
                return filterByPrefix(args[args.length - 1], suggestions);
            }
        }


        if (args[0].equalsIgnoreCase(exit)) {
            if (args.length == 2) {
                return filterByPrefix(args[1], ParkourManager.getAllPlayerNamesInParkour());
            }
        }

        if (args[0].equalsIgnoreCase(stop) || args[0].equalsIgnoreCase(pause) || args[0].equalsIgnoreCase(resume)) {
            if (args.length == 2) {
                List<String> all_maps = ParkourManager.getAllGlobalMaps().stream()
                        .map(name -> "m:" + name)
                        .collect(Collectors.toList());

                List<String> all_players = ParkourManager.getAllPlayersIndividual().stream()
                        .map(name -> "p:" + name)
                        .collect(Collectors.toList());

                List<String> combined = new ArrayList<>();
                combined.addAll(all_maps);
                combined.addAll(all_players);

                return filterByPrefix(args[1], combined);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterByPrefix(final String prefix, final @NotNull List<String> items) {
        return items.stream()
                .filter(item -> item.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}