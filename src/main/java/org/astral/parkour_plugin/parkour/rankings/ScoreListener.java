package org.astral.parkour_plugin.parkour.rankings;

import org.astral.parkour_plugin.Main;
import org.astral.parkour_plugin.parkour.common.Checkpoint;
import org.astral.parkour_plugin.parkour.common.Parkour;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;


import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ScoreListener implements Listener {

    //Instances
    private final static Main Plugin = Main.getInstance();
    private final Parkour parkour = Plugin.getParkour();
    private final Score score = Plugin.getScore();

    private final Set<Player> players = parkour.getPlayerOnParkour();

    @EventHandler
    public void onPlayerMove(final @NotNull PlayerMoveEvent event){
        final Player player = event.getPlayer();
        if (players.contains(player)){
            final String MapName = parkour.getMapNameByPlayer(player);
            final Location location = player.getLocation();
            if (MapName.isEmpty()) return;
            final List<Checkpoint> checkpoints = parkour.getCheckpointsMap().get(MapName);
            if (checkpoints == null) return;
            for (int i = 0; i < checkpoints.size(); i++) {
                final Checkpoint checkpoint = checkpoints.get(i);
                if (checkpoint.isArea()){
                    if (Parkour.isLocationMatch(Objects.requireNonNull(checkpoint.getLocations()), location)){
                        score.updateScore(player, i);
                        break;
                    }
                } else {
                    if (Parkour.isLocationMatch(Objects.requireNonNull(checkpoint.getLocation()), location)){
                        score.updateScore(player, i);
                        break;
                    }
                }
            }
            final double completionPercentage = score.getCompletionPercentage(player, checkpoints);
            double overallCompletionPercentage = score.getOverallCompletionPercentage(player, checkpoints);
            final List<Player> rankings = score.getRankings(checkpoints, true);
        }
    }

}
