package org.jufyer.plugin.stock.moneySystem;

import org.bukkit.entity.Player;
import org.jufyer.plugin.stock.getPrice.FetchFromDataFolder;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jufyer.plugin.stock.Main.portfolio;

public class PortfolioManager {
    private static final List<String> STOCK_NAMES = Arrays.asList(
            "gold", "iron-ore", "copper", "rhodium", "platinum", "indium",
            "cobalt", "silicon", "coal", "natural-gas", "crude-oil", "uranium",
            "wheat", "corn", "coffee", "sugar", "cotton", "palm-oil",
            "orange-juice", "live-cattle", "milk", "sulfur"
    );

    public static void updateStock(Player player, TradeCommodity commodity, Integer newAmount) {
        Map<String, Integer> stockMap = portfolio.get(player.getUniqueId());

        if (stockMap != null) {
            if (newAmount > 0) {
                stockMap.put(commodity.getCommodityName(), newAmount);
                player.sendMessage("§6Stock: " + commodity.getCommodityName() + " updated to " + newAmount + "shares!");
            } else {
                stockMap.remove(commodity.getCommodityName());
                System.out.println("§6Stock: " + commodity.getCommodityName() + " removed from your Portfolio");
            }
        }
    }

    public static int getStockAmount(Player player, TradeCommodity commodity) {
        Map<String, Integer> stockMap = portfolio.get(player.getUniqueId());

        if (stockMap != null) {
            return stockMap.getOrDefault(commodity.getCommodityName(), 0);
        }

        return 0;
    }
}
