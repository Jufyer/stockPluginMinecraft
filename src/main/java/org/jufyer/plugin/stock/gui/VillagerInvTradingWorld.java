package org.jufyer.plugin.stock.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class VillagerInvTradingWorld implements Listener {
    public static Inventory VillagerInvTradingWorld = Bukkit.createInventory(null, 9*4, "Trading menu");
    //public static final NamespacedKey BUY_ITEM_KEY = new NamespacedKey(Main.getInstance(), "BUY_ITEM");

    public static void setVillagerInvTradingWorld() {
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeItemMeta = closeItem.getItemMeta();
        closeItemMeta.setDisplayName("§cClose Menu");
        closeItem.setItemMeta(closeItemMeta);
        VillagerInvTradingWorld.setItem(8, closeItem);

        ItemStack exitItem = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta exitItemMeta = exitItem.getItemMeta();
        exitItemMeta.setDisplayName("§cGo back home");
        exitItem.setItemMeta(exitItemMeta);
        VillagerInvTradingWorld.setItem(9*4-1, exitItem);

        ItemStack selectStockItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta selectStockItemMeta = selectStockItem.getItemMeta();
        selectStockItemMeta.setDisplayName("§rSelect Stock");
        selectStockItem.setItemMeta(selectStockItemMeta);
        VillagerInvTradingWorld.setItem(10, selectStockItem);

        ItemStack sellItemsItem = new ItemStack(Material.EMERALD);
        ItemMeta sellItemsItemMeta = sellItemsItem.getItemMeta();
        sellItemsItemMeta.setDisplayName("§rSell Items");
        sellItemsItem.setItemMeta(sellItemsItemMeta);
        VillagerInvTradingWorld.setItem(13, sellItemsItem);

        ItemStack buyStocksItem = new ItemStack(Material.EMERALD);
        ItemMeta buyStocksItemMeta = buyStocksItem.getItemMeta();
        buyStocksItemMeta.setDisplayName("§rBuy Stocks");
        buyStocksItem.setItemMeta(buyStocksItemMeta);
        VillagerInvTradingWorld.setItem(16, buyStocksItem);
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
            } else if (event.getInventory().equals(VillagerInvTradingWorld) && event.getCurrentItem().getItemMeta().getDisplayName().equals("Sell Items")) {
                player.openInventory(SellItemGui.SellItemMenuInventory);
            } else if (event.getInventory().equals(VillagerInvTradingWorld) && event.getCurrentItem().getItemMeta().getDisplayName().equals("Buy Stocks")) {
                player.openInventory(BuyItemGui.BuyItemMenuInventory);
            } else if (event.getInventory().equals(VillagerInvTradingWorld) && event.getCurrentItem().getItemMeta().getDisplayName().equals("§cClose Menu")) {
                event.getInventory().close();
            }
        }
    }
}
