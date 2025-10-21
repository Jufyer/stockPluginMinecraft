package org.jufyer.plugin.stock.gui;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

public class Wheat implements Listener {
    public static Inventory wheatInv = Bukkit.createInventory(null, 9*6, "Wheat");

    public void setWheatInv() {

    }
}
