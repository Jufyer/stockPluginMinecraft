package org.jufyer.plugin.stock.chatImplementation.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jufyer.plugin.stock.getPrice.FetchFromDataFolder;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;
import org.jufyer.plugin.stock.moneySystem.PortfolioManager;

import java.util.*;

import static org.jufyer.plugin.stock.Main.portfolio;
import static org.jufyer.plugin.stock.Main.wallet;

public class BuyStock implements CommandExecutor {

    private static final List<String> STOCK_NAMES = Arrays.asList(
            "gold", "iron-ore", "copper", "rhodium", "platinum", "indium",
            "cobalt", "silicon", "coal", "natural-gas", "crude-oil", "uranium",
            "wheat", "corn", "coffee", "sugar", "cotton", "palm-oil",
            "orange-juice", "live-cattle", "milk", "sulfur"
    );

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            player.sendMessage("§cPlease name a stock: §e/buy <stock>");
            player.sendMessage("§7Available stocks: " + String.join(", ", STOCK_NAMES));
            return true;
        }

        String stockName = args[0].toLowerCase();

        // --- Validate commodity enum ---
        TradeCommodity commodity;
        try {
            commodity = TradeCommodity.valueOf(stockName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§4The name you provided does not exist!");
            return true;
        }

        // --- ASYNC Preis holen ---
        FetchFromDataFolder.getPrice(commodity).thenAccept(price -> {

            if (price == null) {
                player.sendMessage("§4Could not load price for: §c" + commodity.getCommodityName());
                return;
            }

            // Switch back to main thread to modify player data safely:
            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager().getPlugin("YOUR_PLUGIN_NAME"),
                    () -> handleBuy(player, commodity, price)
            );

        }).exceptionally(ex -> {
            ex.printStackTrace();
            Bukkit.getScheduler().runTask(
                    Bukkit.getPluginManager().getPlugin("YOUR_PLUGIN_NAME"),
                    () -> player.sendMessage("§4Error while loading price data.")
            );
            return null;
        });

        // tell player it is loading
        player.sendMessage("§7Fetching current market value… ⏳");
        return true;
    }

    private void handleBuy(Player player, TradeCommodity commodity, double price) {

        player.sendMessage("§6Current value: §e" + price);

        wallet.putIfAbsent(player.getUniqueId(), 0.0);
        double balance = wallet.get(player.getUniqueId());

        if (balance >= price) {

            // Money abziehen
            wallet.put(player.getUniqueId(), balance - price);

            // Portfolio erhöhen
            portfolio.putIfAbsent(player.getUniqueId(), new HashMap<>());
            PortfolioManager.updateStock(player, commodity,
                    PortfolioManager.getStockAmount(player, commodity) + 1
            );

            player.sendMessage("§aYou successfully bought §e1 " + commodity.getCommodityName());
        } else {
            player.sendMessage("§4You don't have enough money to buy this stock!");
        }
    }
}
