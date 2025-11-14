package org.jufyer.plugin.stock.chatImplementation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jufyer.plugin.stock.getPrice.FetchFromDataFolder;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;
import org.jufyer.plugin.stock.moneySystem.PortfolioManager;

import javax.sound.sampled.Port;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        try {
            String stockName = args[0].toLowerCase();
            TradeCommodity commodity = TradeCommodity.valueOf(stockName.toUpperCase());

            player.sendMessage("§6Current value: " + FetchFromDataFolder.getPrice(commodity));
            wallet.putIfAbsent(player.getUniqueId(), 0.0d);
            if (wallet.get(player.getUniqueId()) >= FetchFromDataFolder.getPrice(commodity)) {
                portfolio.putIfAbsent(player.getUniqueId(), new HashMap<>());
                PortfolioManager.updateStock(player, commodity, PortfolioManager.getStockAmount(player, commodity) +1);
            }else {
                player.sendMessage("§4You don't have enough money to buy this stock!");
            }


        } catch (IllegalArgumentException e) {
            player.sendMessage("§4The name you provided does not exist!");
        } catch (Exception e) {
            player.sendMessage("§4An error occurred while buying the stock.");
            e.printStackTrace();
        }
        return false;
    }
}
