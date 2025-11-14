package org.jufyer.plugin.stock.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jufyer.plugin.stock.Main;

public class VillagerInvTradingWorld implements Listener {
    public static Inventory VillagerInvTradingWorld = Bukkit.createInventory(null, 9*6, "Trading menu");
    //public static final NamespacedKey BUY_ITEM_KEY = new NamespacedKey(Main.getInstance(), "BUY_ITEM");

    public static void setVillagerInvTradingWorld() {
        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta exitItemMeta = exitItem.getItemMeta();
        exitItemMeta.setDisplayName("§cGo back home");
        exitItem.setItemMeta(exitItemMeta);
        VillagerInvTradingWorld.setItem(8, exitItem);

        ItemStack selectStockItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta selectStockItemMeta = selectStockItem.getItemMeta();
        selectStockItemMeta.setDisplayName("§rSelect Stock");
        selectStockItem.setItemMeta(selectStockItemMeta);
        VillagerInvTradingWorld.setItem(10, selectStockItem);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getInventory().equals(VillagerInvTradingWorld)) {
            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta().getDisplayName() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;
            if (event.getInventory().equals(VillagerInvTradingWorld) && event.getCurrentItem().getItemMeta().getDisplayName().equals("§cGo back home")) {
                event.getInventory().close();
                WorldManager.endTrade((Player) event.getWhoClicked());
            } else if (event.getInventory().equals(VillagerInvTradingWorld) && event.getCurrentItem().getItemMeta().getDisplayName().equals("Select Stock")) {
                player.openInventory(MainApp.MainApp);
            }
        }
    }
}
