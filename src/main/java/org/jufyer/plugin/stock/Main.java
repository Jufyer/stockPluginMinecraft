package org.jufyer.plugin.stock;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jufyer.plugin.stock.getPrice.FetchFromGitRepo;
import org.jufyer.plugin.stock.gui.*;
import org.jufyer.plugin.stock.moneySystem.MoneyManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin implements CommandExecutor {
  private static Main instance;
  public static Main getInstance() {
    return instance;
  }

  public static Map<UUID, Double> wallet = new HashMap<>();
  public static Map<UUID, Map<String, Integer>> portfolio = new HashMap<>();

  @Override
  public void onEnable() {
    instance = this;

//    if (FetchPrice.getPrice(TradeCommodity.WHEAT) != 0) {
//      double wheatPrice = FetchPrice.getPrice(TradeCommodity.WHEAT);
//      String wheatUnit = FetchPrice.getUnit(TradeCommodity.WHEAT);
//
//      getLogger().info("Wheat Price is: " + wheatPrice + " " + wheatUnit);
//    }

    FetchFromGitRepo.update();

    MainApp.invSetup();
    getCommand("stocks").setExecutor(new MainApp());
    Bukkit.getPluginManager().registerEvents(new MainApp(), this);

    //getCommand("showPrices").setExecutor(new showPrices());
    //getCommand("graphui").setExecutor(new graphui());

    Bukkit.getPluginManager().registerEvents(new LockPlayer(), this);
    Bukkit.getPluginManager().registerEvents(new graphui(), this);

    //getCommand("getBlockData").setExecutor(this);

    //getCommand("placeblocks").setExecutor(new BlockPlaceCommand());
    //getCommand("buy").setExecutor(new BuyStock());

    SellItemGui.setSellItemMenuInventory();
    //getCommand("sellItems").setExecutor(new SellItemGui());
    Bukkit.getPluginManager().registerEvents(new SellItemGui(), this);

    BuyStockGui.setBuyItemMenuInventory();
    //getCommand("buy").setExecutor(new BuyItemGui());
    Bukkit.getPluginManager().registerEvents(new BuyStockGui(), this);

    VillagerInvTradingWorld.setVillagerInvTradingWorld();
    Bukkit.getPluginManager().registerEvents(new VillagerInvTradingWorld(), this);
    Bukkit.getPluginManager().registerEvents(new WorldManager(), this);

    for (Player player : Bukkit.getOnlinePlayers()) {
          loadWallet(player);
          loadPortfolio(player);
    }

    Bukkit.getPluginManager().registerEvents(new MoneyManager(), this);

    SellStockGui.setBuyItemMenuInventory();
    //getCommand("sellItems").setExecutor(new SellItemGui());
    Bukkit.getPluginManager().registerEvents(new SellStockGui(), this);
  }

    @Override
    public void onDisable() {
        saveAllWallets();
        saveAllPortfolios();
    }

    public void saveAllWallets() {
        File dataFolder = new File(getDataFolder(), "playerData\\wallet");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        for (UUID uuid : wallet.keySet()) {
            File walletFile = new File(dataFolder, uuid.toString() + ".txt");
            try (FileWriter writer = new FileWriter(walletFile)) {
                writer.write(String.valueOf(wallet.get(uuid)));
            } catch (IOException e) {
                getLogger().severe("Error saving wallet for UUID: " + uuid);
                e.printStackTrace();
            }
        }
        getLogger().info("Saved all Wallets.");
    }

    public static void loadWallet(Player player) {
        File dataFolder = new File(Main.getInstance().getDataFolder(), "playerData\\wallet");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        File walletFile = new File(dataFolder, player.getUniqueId().toString() + ".txt");
        if (!walletFile.exists()) {
            wallet.put(player.getUniqueId(), 0.0); // Standard-Wallet = 0
            return;
        }

        try {
            String content = new String(java.nio.file.Files.readAllBytes(walletFile.toPath()));
            wallet.put(player.getUniqueId(), Double.parseDouble(content));
        } catch (IOException | NumberFormatException e) {
            Main.getInstance().getLogger().warning("Error loading wallet by " + player.getName() + ". Setting to 0.");
            wallet.put(player.getUniqueId(), 0.0);
        }
    }

    public void saveAllPortfolios() {
        File dataFolder = new File(getDataFolder(), "playerData/portfolio");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        for (UUID uuid : portfolio.keySet()) {
            File portfolioFile = new File(dataFolder, uuid.toString() + ".txt");
            try (FileWriter writer = new FileWriter(portfolioFile)) {
                Map<String, Integer> playerPortfolio = portfolio.get(uuid);
                for (Map.Entry<String, Integer> entry : playerPortfolio.entrySet()) {
                    writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
                }
            } catch (IOException e) {
                getLogger().severe("Error saving portfolio for UUID: " + uuid);
                e.printStackTrace();
            }
        }
        getLogger().info("Saved all portfolios.");
    }

    public static void loadPortfolio(Player player) {
        File dataFolder = new File(Main.getInstance().getDataFolder(), "playerData/portfolio");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        File portfolioFile = new File(dataFolder, player.getUniqueId().toString() + ".txt");
        if (!portfolioFile.exists()) {
            portfolio.put(player.getUniqueId(), new HashMap<>()); // Leeres Portfolio
            return;
        }

        Map<String, Integer> playerPortfolio = new HashMap<>();
        try {
            List<String> lines = java.nio.file.Files.readAllLines(portfolioFile.toPath());
            for (String line : lines) {
                if (line.isEmpty() || !line.contains(":")) continue;
                String[] parts = line.split(":");
                String stock = parts[0];
                int amount = Integer.parseInt(parts[1]);
                playerPortfolio.put(stock, amount);
            }
            portfolio.put(player.getUniqueId(), playerPortfolio);
        } catch (IOException | NumberFormatException e) {
            Main.getInstance().getLogger().warning("Error loading portfolio for " + player.getName() + ". Setting empty.");
            portfolio.put(player.getUniqueId(), new HashMap<>());
        }
    }

//  @Override
//  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//    Player player = (Player) sender;
//    saveBlockData(player);
//    return false;
//  }
//
//  private void saveBlockData(Player player) {
//    Map<String, String> blockData = new HashMap<>();
//    List<Material> targetMaterials = List.of(
//            Material.DIAMOND_BLOCK,
//            Material.SMOOTH_QUARTZ_STAIRS,
//            Material.POLISHED_BLACKSTONE_BUTTON,
//            Material.OBSIDIAN,
//            Material.WHITE_CARPET,
//            Material.DEEPSLATE_DIAMOND_ORE,
//            Material.SMOOTH_QUARTZ,
//            Material.STONE,
//            Material.BARRIER,
//            Material.END_ROD,
//            Material.DEEPSLATE_GOLD_ORE,
//            Material.GOLD_BLOCK,
//            Material.CRYING_OBSIDIAN,
//            Material.BLACK_BANNER,
//            Material.DIAMOND_BLOCK,
//            Material.SMOOTH_QUARTZ_SLAB,
//            Material.NETHER_GOLD_ORE,
//            Material.RAW_GOLD_BLOCK,
//            Material.GOLD_ORE,
//            Material.CREAKING_HEART,
//            Material.DIAMOND_ORE,
//            Material.BARREL
//    );
//
//    int radius = 50;
//    for (int x = -radius; x <= radius; x++) {
//      for (int y = -radius; y <= radius; y++) {
//        for (int z = -radius; z <= radius; z++) {
//          Block block = player.getLocation().add(x, y, z).getBlock();
//          if (targetMaterials.contains(block.getType())) {
//            String relativeCoordinates = x + ", " + y + ", " + z;
//            String blockType = block.getType().name();
//            String rotation = "NONE";
//
//            BlockData blockDataObj = block.getBlockData();
//
//            if (blockDataObj instanceof Directional) {
//              Directional directional = (Directional) blockDataObj;
//              rotation = directional.getFacing().name();
//            } else if (blockDataObj instanceof Rotatable) {
//              Rotatable rotatable = (Rotatable) blockDataObj;
//              rotation = rotatable.getRotation().name();
//            } else if (blockDataObj instanceof Orientable) {
//              Orientable orientable = (Orientable) blockDataObj;
//              rotation = orientable.getAxis().name();
//            }
//
//            if (blockDataObj instanceof Bisected) {
//              Bisected bisected = (Bisected) blockDataObj;
//              rotation = rotation + "_" + bisected.getHalf().name();
//            }
//
//            if (blockDataObj instanceof Slab) {
//              Slab slab = (Slab) blockDataObj;
//              rotation = rotation + "_" + slab.getType().name();
//            }
//
//            blockData.put(relativeCoordinates, "Type: " + blockType + ", Rotation: " + rotation);
//          }
//        }
//      }
//    }
//
//    blockData.forEach((coordinates, data) -> {
//      getLogger().info("Coordinates: " + coordinates + " | " + data);
//    });
//  }
}