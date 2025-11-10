package org.jufyer.plugin.stock.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
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
            double price = FetchPrice.getPrice(commodity);
            String unit = FetchPrice.getUnit(commodity);

            meta.setDisplayName("§r" + capitalize(stockName.replace("-", " ")) +
                    "\n§7Price: §f" + price + " " + unit);
            icon.setItemMeta(meta);

            MainApp.setItem(innerSlots[i], icon);
            i++;
        }

    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        Player player = (Player) commandSender;
        player.openInventory(MainApp);

        return false;
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
