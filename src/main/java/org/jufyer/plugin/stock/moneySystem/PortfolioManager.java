package org.jufyer.plugin.stock.moneySystem;

import org.bukkit.entity.Player;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.jufyer.plugin.stock.Main.portfolio;

public class PortfolioManager {
    private static final List<String> STOCK_NAMES = Arrays.asList(
            "gold", "iron-ore", "copper", "rhodium", "platinum", "indium",
            "cobalt", "silicon", "coal", "natural-gas", "crude-oil", "uranium",
            "wheat", "corn", "coffee", "sugar", "cotton", "palm-oil",
            "orange-juice", "live-cattle", "milk", "sulfur"
    );

    public static void updateStock(Player player, TradeCommodity commodity, Integer newAmount) {
        UUID playerUUID = player.getUniqueId();

        Map<String, Integer> stockMap = portfolio.get(playerUUID);

        if (stockMap == null) {
            stockMap = new HashMap<>();
            portfolio.put(playerUUID, stockMap);
        }

        if (newAmount > 0) {
            stockMap.put(commodity.getCommodityName(), newAmount);
            //player.sendMessage("§6Stock: " + commodity.getCommodityName() + " updated to " + newAmount + " shares!");
        } else {
            if (stockMap.containsKey(commodity.getCommodityName())) {
                stockMap.remove(commodity.getCommodityName());
                player.sendMessage("§6Stock: " + commodity.getCommodityName() + " removed from your Portfolio");
            }
        }
    }

    public static int getStockAmount(Player player, TradeCommodity commodity) {
        Map<String, Integer> stockMap = portfolio.get(player.getUniqueId());
        if (stockMap == null) {
            return 0;
        }
        return stockMap.getOrDefault(commodity.getCommodityName(), 0);
    }
}