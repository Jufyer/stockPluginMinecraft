package org.jufyer.plugin.stock.moneySystem;

import org.bukkit.entity.Player;

import static org.jufyer.plugin.stock.Main.wallet;

public class Money {
    public static void add(Player player, Double value) {
        wallet.putIfAbsent(player.getUniqueId(), 0.0d);
        wallet.compute(player.getUniqueId(), (k, currentMoney) -> currentMoney + value);
    }

    public static boolean remove(Player player, Double value) {
        wallet.putIfAbsent(player.getUniqueId(), 0.0d);
        if (wallet.get(player.getUniqueId()) >= value) {
            wallet.compute(player.getUniqueId(), (k, currentMoney) -> currentMoney - value);
            return true;
        }else return false;
    }

    public static double get(Player player) {
        wallet.putIfAbsent(player.getUniqueId(), 0.0d);
        return wallet.get(player.getUniqueId());
    }
}
