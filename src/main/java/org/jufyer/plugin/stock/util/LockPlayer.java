package org.jufyer.plugin.stock.util;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LockPlayer implements Listener {
    private static List<UUID> LOCKED_PLAYERS = new ArrayList<>();

    public static void lock(Player player) {
        LOCKED_PLAYERS.add(player.getUniqueId());
        player.setVelocity(new Vector(0,0,0));
        player.setAllowFlight(true);
    }

    public static void unlock(Player player) {
        LOCKED_PLAYERS.remove(player.getUniqueId());
        player.setInvulnerable(false);
        player.setAllowFlight(false);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (LOCKED_PLAYERS.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);

        }
    }

    @EventHandler
    public void onPrePlayerAttackEntity(PrePlayerAttackEntityEvent event) {
        if (LOCKED_PLAYERS.contains(event.getAttacked().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
