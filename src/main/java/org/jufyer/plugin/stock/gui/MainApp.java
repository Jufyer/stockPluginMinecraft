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

public class MainApp implements Listener, CommandExecutor{
    public static Inventory MainApp = Bukkit.createInventory(null, 54, "§r\uF001Stocks");

    public static void invSetup() {
        ItemStack wheatIcon = new ItemStack(Material.HAY_BLOCK);

        ItemMeta meta = wheatIcon.getItemMeta();
        meta.setDisplayName("§rPrice: " + String.valueOf(FetchPrice.getPrice(TradeCommodity.WHEAT)) + String.valueOf(FetchPrice.getUnit(TradeCommodity.WHEAT)));

        wheatIcon.setItemMeta(meta);

        MainApp.addItem(wheatIcon);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        Player player = (Player) commandSender;
        player.openInventory(MainApp);

        return false;
    }
}
