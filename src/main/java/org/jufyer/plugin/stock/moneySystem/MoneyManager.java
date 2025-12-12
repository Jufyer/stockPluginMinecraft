package org.jufyer.plugin.stock.moneySystem;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jufyer.plugin.stock.Main;

import java.text.DecimalFormat;

import static org.jufyer.plugin.stock.Main.wallet;

public class MoneyManager implements Listener {

    private static double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // ------------------------------
    // ADD MONEY
    // ------------------------------
    public static boolean add(Player player, double value) {

        // Vault?
        if (Main.useVault && Main.getEconomy() != null) {
            EconomyResponse r = Main.getEconomy().depositPlayer(player, value);
            return r.transactionSuccess();
        }

        // eigenes System
        wallet.putIfAbsent(player.getUniqueId(), 0.0d);
        wallet.compute(player.getUniqueId(),
                (k, currentMoney) -> roundTwoDecimals(currentMoney + value));
        return true;
    }

    // ------------------------------
    // REMOVE MONEY
    // ------------------------------
    public static boolean remove(Player player, double value) {

        // Vault?
        if (Main.useVault && Main.getEconomy() != null) {
            EconomyResponse r = Main.getEconomy().withdrawPlayer(player, value);
            return r.transactionSuccess();
        }

        // eigenes System
        wallet.putIfAbsent(player.getUniqueId(), 0.0d);
        if (wallet.get(player.getUniqueId()) >= value) {
            wallet.compute(player.getUniqueId(),
                    (k, currentMoney) -> roundTwoDecimals(currentMoney - value));
            return true;
        }
        return false;
    }

    // ------------------------------
    // GET BALANCE
    // ------------------------------
    public static double get(Player player) {

        // Vault?
        if (Main.useVault && Main.getEconomy() != null) {
            return Main.getEconomy().getBalance(player);
        }

        // eigenes System
        wallet.putIfAbsent(player.getUniqueId(), 0.0d);
        return roundTwoDecimals(wallet.get(player.getUniqueId()));
    }

    // ------------------------------
    // GET FORMATTED BALANCE
    // ------------------------------
    public static String getFormatted(Player player) {
        double value = get(player); // nutzt automatisch Vault oder Wallet

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

    private static String formatDecimal(double value) {
        DecimalFormat df = new DecimalFormat("#,##0.##");
        return df.format(value);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Nur laden, wenn NICHT Vault benutzt wird
        if (!Main.useVault) {
            Main.loadWallet(event.getPlayer());
            Main.loadPortfolio(event.getPlayer());
        }
    }
}
