package org.jufyer.plugin.stock;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jufyer.plugin.stock.getPrice.FetchPrice;
import org.jufyer.plugin.stock.getPrice.UpdateGitRepo;
import org.jufyer.plugin.stock.gui.MainApp;
import org.jufyer.plugin.stock.listeners.InventoryListeners;

public final class Main extends JavaPlugin{
  private static Main instance;
  public static Main getInstance() {
    return instance;
  }

  /*Prices*/
  public static double wheatPrice = 0;

  @Override
  public void onEnable() {
    instance = this;

    if (FetchPrice.wheat() != 0) {
      wheatPrice = FetchPrice.wheat();
      getLogger().info("Wheat Price is: " + wheatPrice);
    }

    UpdateGitRepo.update();

    MainApp.invSetup();
    getCommand("stocks").setExecutor(new MainApp());

    Bukkit.getPluginManager().registerEvents(new InventoryListeners(), this);
  }

  @Override
  public void onDisable(){

  }
}
