package org.astral.parkour_plugin.config.maps.items;

import org.bukkit.Material;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class DefaultItems {

    private static final Map<ParkourItemType, ParkourItem> DEFAULT_ITEMS = new EnumMap<>(ParkourItemType.class);

    static {
        DEFAULT_ITEMS.put(ParkourItemType.LAST_CHECKPOINT,
                new ParkourItem(Material.CACTUS, "§aÚltimo Checkpoint",
                        Arrays.asList("§7Te lleva al", "§7último checkpoint."),
                        0, 1, true ,0, 0, 0, 0, ParkourItemType.LAST_CHECKPOINT));

        DEFAULT_ITEMS.put(ParkourItemType.BACK_CHECKPOINT,
                new ParkourItem(Material.REDSTONE, "§cCheckpoint Anterior",
                        Collections.singletonList("§7Retrocede un checkpoint."),
                        1, 1, true ,0, 0, 0, 0, ParkourItemType.BACK_CHECKPOINT));

        DEFAULT_ITEMS.put(ParkourItemType.NEXT_CHECKPOINT,
                new ParkourItem(Material.SLIME_BALL, "§aCheckpoint Siguiente",
                        Collections.singletonList("§7Avanza un checkpoint."),
                        2, 1, true ,0, 0, 0, 0, ParkourItemType.NEXT_CHECKPOINT));

        DEFAULT_ITEMS.put(ParkourItemType.GOBACK_CHECKPOINT,
                new ParkourItem(Material.TRIPWIRE_HOOK, "§cReinicia el Checkpoint",
                        Collections.singletonList("§7Reinicia el checkpoint."),
                        3, 1, true,0, 0, 0, 0, ParkourItemType.GOBACK_CHECKPOINT));

        DEFAULT_ITEMS.put(ParkourItemType.RESET,
                new ParkourItem(Material.BARRIER, "§cReiniciar Parkour",
                        Collections.singletonList("§7Vuelve al inicio del mapa."),
                        4, 1, true, 0, 0, 0, 0, ParkourItemType.RESET));

        DEFAULT_ITEMS.put(ParkourItemType.FINISH,
                new ParkourItem(Material.NETHER_STAR, "§eFinalizar Parkour",
                        Arrays.asList("§7Marca tu tiempo y", "§7termina la partida."),
                        5, 1, false ,0, 0, 0, 0, ParkourItemType.FINISH));

        DEFAULT_ITEMS.put(ParkourItemType.FEATHER_JUMP,
                new ParkourItem(Material.FEATHER, "§bSalto Extra",
                        Arrays.asList("§7Te permite hacer", "§7un salto adicional."),
                        6, 5, false ,1, 0, 0, 3, ParkourItemType.FEATHER_JUMP));

        DEFAULT_ITEMS.put(ParkourItemType.ROCKET_BOOST,
                new ParkourItem(Material.FIREWORK, "§dCohete de Impulso",
                        Arrays.asList("§7Te lanza hacia adelante", "§7con gran velocidad."),
                        7, 3, false,0, 2.5, 0.8, 5, ParkourItemType.ROCKET_BOOST));

        DEFAULT_ITEMS.put(ParkourItemType.TELEPORT_LOBBY,
                new ParkourItem(Material.COMPASS, "§6Volver al Lobby",
                        Arrays.asList("§7Regresa al lobby", "§7de parkour."),
                        8, 1, true,0, 0, 0, 0, ParkourItemType.TELEPORT_LOBBY));

        DEFAULT_ITEMS.put(ParkourItemType.HIDE_PLAYERS,
                new ParkourItem(Material.INK_SACK, "§9Ocultar Jugadores",
                        Arrays.asList("§7Activa/desactiva", "§7la visibilidad de jugadores."),
                        9, 1, true ,0, 0, 0, 2, ParkourItemType.HIDE_PLAYERS));

        DEFAULT_ITEMS.put(ParkourItemType.CHECKPOINT_SELECTOR,
                new ParkourItem(Material.BOOK, "§aSelector de Checkpoint",
                        Arrays.asList("§7Elige un checkpoint", "§7para teletransportarte."),
                        10, 1, true ,0, 0, 0, 0, ParkourItemType.CHECKPOINT_SELECTOR));
    }

    private DefaultItems() {}

    @Contract(pure = true)
    public static @NotNull @UnmodifiableView Map<ParkourItemType, ParkourItem> getDefaults() {
        return Collections.unmodifiableMap(DEFAULT_ITEMS);
    }

    public static ParkourItem getDefault(ParkourItemType type) {
        return DEFAULT_ITEMS.get(type);
    }
}
