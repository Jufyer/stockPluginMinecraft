package org.jufyer.plugin.stock;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jufyer.plugin.stock.chatImplementation.commands.showPrices;
import org.jufyer.plugin.stock.getPrice.FetchPrice;
import org.jufyer.plugin.stock.getPrice.FetchFromGitRepo;
import org.jufyer.plugin.stock.gui.MainApp;
import org.jufyer.plugin.stock.listeners.InventoryListeners;

public final class Main extends JavaPlugin{
  private static Main instance;
  public static Main getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    instance = this;

    if (FetchPrice.getPrice("wheat") != 0) {
      double wheatPrice = FetchPrice.getPrice("wheat");
      String wheatUnit = FetchPrice.getUnit("wheat");

      getLogger().info("Wheat Price is: " + wheatPrice + " " + wheatUnit);
    }

    FetchFromGitRepo.update();

    MainApp.invSetup();
    getCommand("stocks").setExecutor(new MainApp());

    getCommand("showPrices").setExecutor(new showPrices());

    Bukkit.getPluginManager().registerEvents(new InventoryListeners(), this);
  }

  @Override
  public void onDisable(){

  }
}
