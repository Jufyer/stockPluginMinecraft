package org.jufyer.plugin.stock.moneySystem;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jufyer.plugin.stock.Main;

import java.text.DecimalFormat;

import static org.jufyer.plugin.stock.Main.wallet;

public class Money implements Listener {

    // Rundet einen double-Wert auf 2 Nachkommastellen
    private static double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static boolean add(Player player, Double value) {
        wallet.putIfAbsent(player.getUniqueId(), 0.0d);
        wallet.compute(player.getUniqueId(), (k, currentMoney) -> roundTwoDecimals(currentMoney + value));
        return true;
    }

    public static boolean remove(Player player, Double value) {
        wallet.putIfAbsent(player.getUniqueId(), 0.0d);
        if (wallet.get(player.getUniqueId()) >= value) {
            wallet.compute(player.getUniqueId(), (k, currentMoney) -> roundTwoDecimals(currentMoney - value));
            return true;
        } else return false;
    }

    public static double get(Player player) {
        wallet.putIfAbsent(player.getUniqueId(), 0.0d);
        return roundTwoDecimals(wallet.get(player.getUniqueId()));
    }

    // Neue Methode für formatierten Wert mit Tsd., Mio., Mrd., Bio.
    public static String getFormatted(Player player) {
        wallet.putIfAbsent(player.getUniqueId(), 0.0d);
        double value = roundTwoDecimals(wallet.get(player.getUniqueId()));

        if (value >= 1_000_000_000_000d) {
            return formatDecimal(value / 1_000_000_000_000d) + " Bio.";
        } else if (value >= 1_000_000_000d) {
            return formatDecimal(value / 1_000_000_000d) + " Mrd.";
        } else if (value >= 1_000_000d) {
            return formatDecimal(value / 1_000_000d) + " Mio.";
        } else if (value >= 1_000d) {
            return formatDecimal(value / 1_000d) + " Tsd.";
        } else {
            return formatDecimal(value);
        }
    }

    private static String formatDecimal(double number) {
        DecimalFormat df = new DecimalFormat("#,##0.##"); // max. 2 Nachkommastellen
        return df.format(number);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Main.loadWallet(event.getPlayer());
        Main.loadPortfolio(event.getPlayer());
    }
}
