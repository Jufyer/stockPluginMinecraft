package org.jufyer.plugin.stock;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
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
    public static Main getInstance() { return instance; }

    // Wallet + Portfolio nur benutzt, wenn Vault NICHT aktiv ist
    public static Map<UUID, Double> wallet = new HashMap<>();
    public static Map<UUID, Map<String, Integer>> portfolio = new HashMap<>();

    // Vault Economy
    public static Economy econ = null;
    public static boolean useVault = false;

    @Override
    public void onEnable() {
        instance = this;

        // Config
        saveDefaultConfig();
        useVault = getConfig().getBoolean("use-vault", false);

        if (useVault) {
            if (!setupEconomy()) {
                getLogger().warning("Vault activated, but no Economy-Plugin found! Using intern Money system.");
                useVault = false;
            } else {
                getLogger().info("Vault Economy successfully activated.");
            }
        }

        FetchFromGitRepo.update();
        getCommand("stocks").setExecutor(new MainApp());

        MainApp.invSetup();
        SellItemGui.setSellItemMenuInventory();
        SellStockGui.setSellStockMenuInventory();
        BuyStockGui.setBuyStockMenuInventory();
        VillagerInvTradingWorld.setVillagerInvTradingWorld();

        Bukkit.getPluginManager().registerEvents(new MainApp(), this);
        Bukkit.getPluginManager().registerEvents(new StockGraph(), this);
        Bukkit.getPluginManager().registerEvents(new SellItemGui(), this);
        Bukkit.getPluginManager().registerEvents(new BuyStockGui(), this);
        Bukkit.getPluginManager().registerEvents(new VillagerInvTradingWorld(), this);
        Bukkit.getPluginManager().registerEvents(new WorldManager(), this);
        Bukkit.getPluginManager().registerEvents(new MoneyManager(), this);
        Bukkit.getPluginManager().registerEvents(new SellStockGui(), this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Wallet nur ohne Vault
            if (!useVault) {
                loadWallet(player);
            }

            loadPortfolio(player);
        }

        //getCommand("sellItems").setExecutor(new SellItemGui());
        //getCommand("buy").setExecutor(new BuyItemGui());
        //getCommand("showPrices").setExecutor(new showPrices());
        //getCommand("graphui").setExecutor(new graphui());
        //getCommand("getBlockData").setExecutor(this);
        //getCommand("placeblocks").setExecutor(new BlockPlaceCommand());
        //getCommand("buy").setExecutor(new BuyStock());
        //getCommand("sellItems").setExecutor(new SellItemGui());
    }

    @Override
    public void onDisable() {
        if (!useVault) {
            saveAllWallets();
        }

        saveAllPortfolios();
    }

    // ---------------- VAULT SETUP ----------------

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;

        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) return false;

        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    // ---------------- WALLET SAVE/LOAD ----------------

    public void saveAllWallets() {
        File dataFolder = new File(getDataFolder(), "playerData/wallet");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        for (UUID uuid : wallet.keySet()) {
            File walletFile = new File(dataFolder, uuid + ".txt");
            try (FileWriter writer = new FileWriter(walletFile)) {
                writer.write(String.valueOf(wallet.get(uuid)));
            } catch (IOException e) {
                getLogger().severe("Error saving wallet for: " + uuid);
                e.printStackTrace();
            }
        }
        getLogger().info("Saved all wallets.");
    }

    public static void loadWallet(Player player) {
        File dataFolder = new File(getInstance().getDataFolder(), "playerData/wallet");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        File walletFile = new File(dataFolder, player.getUniqueId() + ".txt");
        if (!walletFile.exists()) {
            wallet.put(player.getUniqueId(), 0.0);
            return;
        }

        try {
            String content = new String(java.nio.file.Files.readAllBytes(walletFile.toPath()));
            wallet.put(player.getUniqueId(), Double.parseDouble(content));
        } catch (Exception e) {
            getInstance().getLogger().warning("Error loading wallet for " + player.getName());
            wallet.put(player.getUniqueId(), 0.0);
        }
    }

    // ---------------- PORTFOLIO SAVE/LOAD ----------------

    public void saveAllPortfolios() {
        File dataFolder = new File(getDataFolder(), "playerData/portfolio");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        for (UUID uuid : portfolio.keySet()) {
            File portfolioFile = new File(dataFolder, uuid + ".txt");
            try (FileWriter writer = new FileWriter(portfolioFile)) {
                Map<String, Integer> data = portfolio.get(uuid);
                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
                }
            } catch (IOException e) {
                getLogger().severe("Error saving portfolio for: " + uuid);
                e.printStackTrace();
            }
        }
        getLogger().info("Saved all portfolios.");
    }

    public static void loadPortfolio(Player player) {
        File dataFolder = new File(getInstance().getDataFolder(), "playerData/portfolio");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        File portfolioFile = new File(dataFolder, player.getUniqueId() + ".txt");

        if (!portfolioFile.exists()) {
            portfolio.put(player.getUniqueId(), new HashMap<>());
            return;
        }

        Map<String, Integer> playerPortfolio = new HashMap<>();
        try {
            List<String> lines = java.nio.file.Files.readAllLines(portfolioFile.toPath());
            for (String line : lines) {
                if (!line.contains(":")) continue;
                String[] parts = line.split(":");
                playerPortfolio.put(parts[0], Integer.parseInt(parts[1]));
            }
            portfolio.put(player.getUniqueId(), playerPortfolio);
        } catch (Exception e) {
            getInstance().getLogger().warning("Error loading portfolio for " + player.getName());
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