package org.jufyer.plugin.stock.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jufyer.plugin.stock.getPrice.FetchFromDataFolder;
import org.jufyer.plugin.stock.getPrice.FetchPrice;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;

import java.util.Arrays;
import java.util.List;

public class MainApp implements Listener, CommandExecutor{
    public static Inventory MainApp = Bukkit.createInventory(null, 54, "§rStocks");

    private static final List<String> STOCK_NAMES = Arrays.asList(
            "gold", "iron-ore", "copper", "rhodium", "platinum", "indium",
            "cobalt", "silicon", "coal", "natural-gas", "crude-oil", "uranium",
            "wheat", "corn", "coffee", "sugar", "cotton", "palm-oil",
            "orange-juice", "live-cattle", "milk", "sulfur"
    );

    public static void invSetup() {
        //ItemStack wheatIcon = new ItemStack(Material.HAY_BLOCK);

        //ItemMeta meta = wheatIcon.getItemMeta();
        //meta.setDisplayName("§rPrice: " + String.valueOf(FetchPrice.getPrice(TradeCommodity.WHEAT)) + " " + String.valueOf(FetchPrice.getUnit(TradeCommodity.WHEAT)));

        //wheatIcon.setItemMeta(meta);

        //MainApp.addItem(wheatIcon);
        int[] innerSlots = new int[28];
        int index = 0;

        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 7; col++) {
                innerSlots[index++] = row * 9 + col;
            }
        }

        int i = 0;
        for (String stockName : STOCK_NAMES) {
            if (i >= innerSlots.length) break;

            Material mat = TradeCommodity.fromCommodityName(stockName).getMaterial();
            ItemStack icon = new ItemStack(mat);
            ItemMeta meta = icon.getItemMeta();

            TradeCommodity commodity = TradeCommodity.fromCommodityName(stockName);
            double price = FetchFromDataFolder.getPrice(commodity);
            String unit = FetchFromDataFolder.getUnit(commodity);

            meta.setDisplayName("§r" + capitalize(stockName.replace("-", " ")));
            meta.setLore(Arrays.asList("§7Price: §f" + price + "$" + " " + unit));
            icon.setItemMeta(meta);

            MainApp.setItem(innerSlots[i], icon);
            i++;
        }

        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta exitItemMeta = exitItem.getItemMeta();
        exitItemMeta.setDisplayName("§cClose menu");
        exitItem.setItemMeta(exitItemMeta);
        MainApp.setItem(8, exitItem);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("§7Back to overview");
        backItem.setItemMeta(backItemMeta);
        MainApp.setItem(0, backItem);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        Player player = (Player) commandSender;
        player.openInventory(MainApp);

        return false;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(MainApp)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getSource().equals(MainApp)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().equals(MainApp)) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta().getDisplayName() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            if (event.getCurrentItem().getType().equals(Material.BARRIER)) {
                event.getClickedInventory().close();
                return;
            }

            if (event.getWhoClicked().getLocation().getWorld().equals(Bukkit.getWorld("trade_world"))) {
                if (event.getCurrentItem().getType().equals(Material.ARROW)) {
                    event.getWhoClicked().openInventory(VillagerInvTradingWorld.VillagerInvTradingWorld);
                    return;
                }
            }else {
                if (event.getCurrentItem().getType().equals(Material.ARROW)) {
                    return;
                }
            }

            Player player = (Player) event.getWhoClicked();

            ItemStack item = event.getCurrentItem();
            String name = decapitalize(item.getItemMeta().getDisplayName());
            //event.getWhoClicked().sendMessage(name);

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
        if (event.getInventory().equals(MainApp)) {
            event.setCancelled(true);
        }
    }

    private static String decapitalize(String text) {
        if (text == null || text.isEmpty()) return text;

        text = text.replace(" ", "-");

        return text.toLowerCase();
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;

        // ersetze Bindestriche durch Leerzeichen für saubere Anzeige
        text = text.replace("-", " ");

        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) continue;
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }

        return result.toString().trim();
    }
}
