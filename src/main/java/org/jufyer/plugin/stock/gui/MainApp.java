package org.jufyer.plugin.stock.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jufyer.plugin.stock.Main;
import org.jufyer.plugin.stock.getPrice.FetchFromDataFolder;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;
import org.jufyer.plugin.stock.util.UnitConverter;

import java.util.Arrays;
import java.util.List;

import static org.jufyer.plugin.stock.util.UtilityMethods.capitalize;
import static org.jufyer.plugin.stock.util.UtilityMethods.decapitalize;

public class MainApp implements Listener, CommandExecutor {

    public static Inventory MainApp = Bukkit.createInventory(null, 54, "§rStocks");

    private static final List<String> STOCK_NAMES = Arrays.asList(
            "gold", "iron-ore", "copper", "rhodium", "platinum", "indium",
            "cobalt", "silicon", "coal", "natural-gas", "crude-oil", "uranium",
            "wheat", "corn", "coffee", "sugar", "cotton", "palm-oil",
            "orange-juice", "live-cattle", "milk", "sulfur"
    );

    private static final int[] INNER_SLOTS = new int[28];

    static {
        int index = 0;
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                INNER_SLOTS[index++] = row * 9 + col;
            }
        }
    }

    public static void invSetup() {

        int i = 0;
        for (String stockName : STOCK_NAMES) {

            if (i >= INNER_SLOTS.length) break;

            TradeCommodity commodity = TradeCommodity.fromCommodityName(stockName);
            Material mat = commodity.getMaterial();

            ItemStack loading = new ItemStack(mat);
            ItemMeta lm = loading.getItemMeta();
            lm.setDisplayName("§7" + capitalize(stockName));
            lm.setLore(Arrays.asList("§8Loading price…"));
            loading.setItemMeta(lm);

            MainApp.setItem(INNER_SLOTS[i], loading);
            i++;

            int slot = INNER_SLOTS[i - 1];
            FetchFromDataFolder.getLatestByName(commodity).thenAccept(json -> {

                if (json == null) return;

                double priceRaw = -1.0;

                String valStr = json.getString("value", null);

                if (valStr != null) {
                    try {
                        priceRaw = Double.parseDouble(valStr.replace(',', '.'));
                    } catch (NumberFormatException ignored) {
                    }
                } else {
                    try {
                        priceRaw = json.getJsonNumber("price").doubleValue();
                    } catch (Exception ignored) {
                    }
                }

                final double price = priceRaw;

                String unit = json.getString("currency", "");

                double perKg = UnitConverter.toUSD(price, unit, UnitConverter.OutputUnit.KG);

                Bukkit.getScheduler().runTask(
                        Main.getInstance(),
                        () -> {
                            ItemStack icon = new ItemStack(mat);
                            ItemMeta im = icon.getItemMeta();
                            im.setDisplayName("§r" + capitalize(stockName));
                            im.setLore(Arrays.asList(
                                    "§7Price: §f" + String.format("%.2f", perKg) + " $/kg"
                            ));
                            icon.setItemMeta(im);
                            MainApp.setItem(slot, icon);
                        }
                );

            });
        }

        // Exit Item
        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exitItem.getItemMeta();
        exitMeta.setDisplayName("§cClose menu");
        exitItem.setItemMeta(exitMeta);
        MainApp.setItem(8, exitItem);

        // Back Item
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName("§7Back to overview");
        backItem.setItemMeta(backMeta);
        MainApp.setItem(0, backItem);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) return true;

        player.openInventory(MainApp);
        return true;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getInventory().equals(MainApp)) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent e) {
        if (e.getSource().equals(MainApp)) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent e) {
        if (e.getInventory().equals(MainApp)) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if (!e.getInventory().equals(MainApp)) return;
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        Player player = (Player) e.getWhoClicked();

        // Exit
        if (item.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Back
        if (item.getType() == Material.ARROW) {
            if (player.getWorld().equals(Bukkit.getWorld("trade_world"))) {
                player.openInventory(VillagerInvTradingWorld.VillagerInvTradingWorld);
            }
            return;
        }

        // Select commodity
        String displayName = item.getItemMeta().getDisplayName();
        String commodityName = decapitalize(displayName);

        player.closeInventory();
        player.setVelocity(new Vector(0, 0, 0));

        if (WorldManager.activeTraders.containsKey(player.getUniqueId())) {
            StockGraph.displayStockGraph(player, commodityName);
        } else {
            WorldManager.setupWorld(player, commodityName);
        }
    }
}
