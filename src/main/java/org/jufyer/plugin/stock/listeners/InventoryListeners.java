package org.jufyer.plugin.stock.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jufyer.plugin.stock.Main;
import org.jufyer.plugin.stock.gui.MainApp;
import org.jufyer.plugin.stock.gui.WorldManager;
import org.jufyer.plugin.stock.gui.graphui;
import org.jufyer.plugin.stock.util.LockPlayer;

public class InventoryListeners implements Listener {
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(MainApp.MainApp)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getSource().equals(MainApp.MainApp)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().equals(MainApp.MainApp)) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();

            ItemStack item = event.getCurrentItem();
            String name = decapitalize(item.getItemMeta().getDisplayName());
            event.getWhoClicked().sendMessage(name);

            event.getInventory().close();

            //player.setRotation(0,0);
            player.setVelocity(new Vector(0,0,0));

//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    graphui.executeStuff((Player) event.getWhoClicked(), name);
//                }
//            }.runTaskLater(Main.getInstance(), 10);

            if (WorldManager.activeTraders.containsKey(player.getUniqueId())) {
                graphui.executeStuff(player, name);
            }else {
                WorldManager.setupWorld(player, name);
            }

        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        if (event.getInventory().equals(MainApp.MainApp)) {
            event.setCancelled(true);
        }
    }

    private static String decapitalize(String text) {
        if (text == null || text.isEmpty()) return text;

        text = text.replace(" ", "-");

        return text.toLowerCase();
    }
}
