package org.jufyer.plugin.stock.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jufyer.plugin.stock.getPrice.FetchFromDataFolder;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;
import org.jufyer.plugin.stock.moneySystem.MoneyManager;
import org.jufyer.plugin.stock.moneySystem.PortfolioManager;
import org.jufyer.plugin.stock.util.UnitConverter;
import org.jufyer.plugin.stock.Main;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.jufyer.plugin.stock.util.CreateCustomHeads.createCustomHead;
import static org.jufyer.plugin.stock.util.UtilityMethods.capitalize;
import static org.jufyer.plugin.stock.util.UtilityMethods.decapitalize;

public class BuyStockGui implements Listener {

    public static final Inventory BuyStockMenuInventory = Bukkit.createInventory(null, 54, "§aBuy stock menu");
    private static final Map<UUID, TradeCommodity> openBuyActionInventories = new HashMap<>();

    private static final List<String> STOCK_NAMES = Arrays.asList(
            "gold", "iron-ore", "copper", "rhodium", "platinum", "indium",
            "cobalt", "silicon", "coal", "natural-gas", "crude-oil", "uranium",
            "wheat", "corn", "coffee", "sugar", "cotton", "palm-oil",
            "orange-juice", "live-cattle", "milk", "sulfur"
    );

    public static void setBuyStockMenuInventory() {
        BuyStockMenuInventory.clear();

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("§7Back to overview");
        backItem.setItemMeta(backItemMeta);
        BuyStockMenuInventory.setItem(0, backItem);

        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta exitItemMeta = exitItem.getItemMeta();
        exitItemMeta.setDisplayName("§cClose menu");
        exitItem.setItemMeta(exitItemMeta);
        BuyStockMenuInventory.setItem(8, exitItem);

        int i = 10;
        for (String itemName : STOCK_NAMES) {
            while (i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36) {
                i++;
            }

            TradeCommodity commodity = TradeCommodity.fromCommodityName(itemName);
            if (commodity == null) {
                i++;
                continue;
            }

            ItemStack placeholder = new ItemStack(commodity.getMaterial());
            ItemMeta meta = placeholder.getItemMeta();
            meta.setDisplayName("§e" + capitalize(itemName));
            meta.setLore(Arrays.asList("§7Loading price...", "§eClick to view buy options"));
            placeholder.setItemMeta(meta);

            final int slotIndex = i;
            BuyStockMenuInventory.setItem(slotIndex, placeholder);

            CompletableFuture<Double> priceFuture = FetchFromDataFolder.getPrice(commodity);
            CompletableFuture<String> unitFuture = FetchFromDataFolder.getUnit(commodity);

            priceFuture.thenCombine(unitFuture, (priceRaw, unitRaw) -> {
                double pricePerKg;
                try {
                    pricePerKg = UnitConverter.toUSD(priceRaw, unitRaw, UnitConverter.OutputUnit.KG);
                } catch (Exception e) {
                    pricePerKg = 0.0;
                }
                return pricePerKg;
            }).whenComplete((pricePerKg, ex) -> {
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    ItemStack itemStack = BuyStockMenuInventory.getItem(slotIndex);
                    if (itemStack == null) return;

                    ItemMeta m = itemStack.getItemMeta();
                    String priceLine;
                    if (pricePerKg <= 0.0) {
                        priceLine = "§7Current price: §cN/A";
                    } else {
                        priceLine = "§7Current price: §a" + String.format("%.2f", pricePerKg) + " $/kg";
                    }

                    List<String> lore = new ArrayList<>();
                    lore.add(priceLine);
                    lore.add("§eClick to view buy options");
                    m.setLore(lore);
                    itemStack.setItemMeta(m);
                    BuyStockMenuInventory.setItem(slotIndex, itemStack);
                });
            });
            i++;
        }

        // Help Head
        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE4MDMxNThjY2VlY2QyMjY4YTBhYTZmNTU2ZjYxMGY1ZjRhMWMzYzI2NDM1NTk3Njg0OGU0YTE5ZjQwMmMzZCJ9fX0=";
        ItemStack skull = createCustomHead(texture);
        ItemMeta skullItemMeta = skull.getItemMeta();
        skullItemMeta.setDisplayName("§6Help");
        skull.setItemMeta(skullItemMeta);
        BuyStockMenuInventory.setItem(45, skull);
    }

    public static void openBuyActionInventory(Player player, TradeCommodity commodity) {
        Inventory buyActionInventory = Bukkit.createInventory(null, 54, "§2Select amount to buy (" + capitalize(commodity.getCommodityName()) + ")");

        openBuyActionInventories.put(player.getUniqueId(), commodity);

        ItemStack infoItem = new ItemStack(commodity.getMaterial());
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e" + capitalize(commodity.getCommodityName()));
        infoMeta.setLore(Arrays.asList("§7Price per share: §aLoading...", "§7Your money: §6" + MoneyManager.getFormatted(player) + "$", "§7Owned shares: §b" + PortfolioManager.getStockAmount(player, commodity)));
        infoItem.setItemMeta(infoMeta);
        buyActionInventory.setItem(4, infoItem);

        // later updated
        buyActionInventory.setItem(20, createBuyButton(0.0, 1));
        buyActionInventory.setItem(22, createBuyButton(0.0, 10));
        buyActionInventory.setItem(24, createBuyButton(0.0, 64));

        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta exitItemMeta = exitItem.getItemMeta();
        exitItemMeta.setDisplayName("§cClose menu");
        exitItem.setItemMeta(exitItemMeta);
        buyActionInventory.setItem(8, exitItem);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("§7Back to overview");
        backItem.setItemMeta(backItemMeta);
        buyActionInventory.setItem(0, backItem);

        ItemStack fillerItem = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerItemMeta = fillerItem.getItemMeta();
        fillerItemMeta.setDisplayName(" ");
        fillerItem.setItemMeta(fillerItemMeta);

        for (int idx = 0; idx < 54; idx++) {
            if (buyActionInventory.getItem(idx) == null) {
                buyActionInventory.setItem(idx, fillerItem);
            }
        }

        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE4MDMxNThjY2VlY2QyMjY4YTBhYTZmNTU2ZjYxMGY1ZjRhMWMzYzI2NDM1NTk3Njg0OGU0YTE5ZjQwMmMzZCJ9fX0=";
        ItemStack skull = createCustomHead(texture);
        ItemMeta skullItemMeta = skull.getItemMeta();
        skullItemMeta.setDisplayName("§6Help");
        skull.setItemMeta(skullItemMeta);
        buyActionInventory.setItem(45, skull);

        player.openInventory(buyActionInventory);

        CompletableFuture<Double> priceFuture = FetchFromDataFolder.getPrice(commodity);
        CompletableFuture<String> unitFuture = FetchFromDataFolder.getUnit(commodity);

        priceFuture.thenCombine(unitFuture, (priceRaw, unitRaw) -> {
            double pricePerKg;
            try {
                pricePerKg = UnitConverter.toUSD(priceRaw, unitRaw, UnitConverter.OutputUnit.KG);
            } catch (Exception e) {
                pricePerKg = 0.0;
            }
            pricePerKg = Math.round(pricePerKg * 100.0) / 100.0;
            return pricePerKg;
        }).whenComplete((pricePerKg, ex) -> {
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                // Das aktuell geöffnete Inventar abrufen
                if (!player.getOpenInventory().getTitle().startsWith("§2Select amount to buy")) return;
                Inventory currentInventory = player.getOpenInventory().getTopInventory();

                // Update info item (slot 4) and buttons
                ItemStack info = currentInventory.getItem(4);
                if (info != null && info.hasItemMeta()) {
                    ItemMeta im = info.getItemMeta();
                    String priceLine = pricePerKg <= 0.0 ? "§7Price per share: §cN/A" : "§7Price per share: §a" + String.format("%.2f", pricePerKg) + " $/kg";
                    im.setLore(Arrays.asList(
                            priceLine,
                            "§7Your money: §6" + MoneyManager.getFormatted(player) + "$",
                            "§7Owned shares: §b" + PortfolioManager.getStockAmount(player, commodity)
                    ));
                    info.setItemMeta(im);
                    currentInventory.setItem(4, info);
                }

                // Update buttons with real price
                currentInventory.setItem(20, createBuyButton(pricePerKg, 1));
                currentInventory.setItem(22, createBuyButton(pricePerKg, 10));
                currentInventory.setItem(24, createBuyButton(pricePerKg, 64));
            });
        });
    }

    private static ItemStack createBuyButton(double price, int amount) {
        ItemStack button = new ItemStack(Material.EMERALD);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName("§aBuy " + amount + " shares");

        String costString;
        if (price <= 0.0) {
            costString = "N/A";
        } else {
            double totalCost = Math.round(price * amount * 100.0) / 100.0;
            costString = String.format("%.2f", totalCost);
        }

        meta.setLore(Arrays.asList(
                "§7Cost: §c" + costString + (price <= 0.0 ? "" : "$"),
                "§eClick to purchase"
        ));
        button.setItemMeta(meta);
        return button;
    }


    public static void openHelpBuyBook(Player player) {
        ItemStack helpBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bm = (BookMeta) helpBook.getItemMeta();

        bm.addPage("§6Buy Stock Help\n\n" +
                "Welcome to the Stock Market!\n\n" +
                "§6Page 1: Overview\n" +
                "§0- Click on a stock icon to view buy options.\n" +
                "- Prices fluctuate over time.\n" +
                "- Your portfolio is saved automatically.");

        bm.addPage("§6Page 2: Buying\n" +
                "§0- Select amounts: 1, 10, or 64.\n" +
                "- Ensure you have enough money in your wallet.\n" +
                "- Bought shares are added to your digital portfolio.");

        bm.setAuthor("StockMarket");
        bm.setTitle("Buy Help");
        helpBook.setItemMeta(bm);
        player.openBook(helpBook);
    }

//    @Override
//    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
//        if (commandSender instanceof Player player) {
//            if (strings.length == 1) {
//                TradeCommodity commodity = TradeCommodity.fromCommodityName(strings[0]);
//                if (commodity != null) {
//                    openBuyActionInventory(player, commodity);
//                    return true;
//                } else {
//                    player.sendMessage("§cUnknown commodity. Opening menu...");
//                }
//            }
//            player.openInventory(BuyStockMenuInventory);
//        }
//        return true;
//    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getCurrentItem().hasItemMeta()) return;

        Inventory clickedInv = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();
        String displayName = clickedItem.getItemMeta().getDisplayName();

        if (clickedInv.equals(BuyStockMenuInventory)) {
            event.setCancelled(true);

            if (event.getSlot() == 0) {
                event.setCancelled(true);
                player.openInventory(VillagerInvTradingWorld.VillagerInvTradingWorld);
                return;
            }

            if (displayName.startsWith("§e")) {
                String stockName = decapitalize(displayName.replace("§e", ""));
                TradeCommodity commodity = TradeCommodity.fromCommodityName(stockName);

                if (commodity != null) {
                    //player.sendMessage("§7Selected stock: §a" + capitalize(stockName));
                    openBuyActionInventory(player, commodity);
                }
            } else if (displayName.equals("§cClose menu")) {
                player.closeInventory();
            } else if (displayName.equals("§6Help")) {
                openHelpBuyBook(player);
            }
        }

        if (event.getView().getTitle().startsWith("§2Select amount to buy")) {
            event.setCancelled(true);

            TradeCommodity commodity = openBuyActionInventories.get(player.getUniqueId());
            if (commodity == null) return;

            if (displayName.equals("§cClose menu")) {
                player.closeInventory();
                openBuyActionInventories.remove(player.getUniqueId());
                return;
            }
            if (displayName.equals("§7Back to overview")) {
                player.openInventory(BuyStockMenuInventory);
                openBuyActionInventories.remove(player.getUniqueId());
                return;
            }
            if (displayName.equals("§6Help")) {
                openHelpBuyBook(player);
                return;
            }

            if (displayName.startsWith("§aBuy ")) {
                int amountToBuy = Integer.parseInt(displayName.replace("§aBuy ", "").replace(" shares", ""));

                CompletableFuture<Double> priceFuture = FetchFromDataFolder.getPrice(commodity);
                CompletableFuture<String> unitFuture = FetchFromDataFolder.getUnit(commodity);

                priceFuture.thenCombine(unitFuture, (priceRaw, unitRaw) -> {
                    double pricePerKg;
                    try {
                        pricePerKg = UnitConverter.toUSD(priceRaw, unitRaw, UnitConverter.OutputUnit.KG);
                    } catch (Exception e) {
                        pricePerKg = 0.0;
                    }
                    pricePerKg = Math.round(pricePerKg * 100.0) / 100.0;
                    return pricePerKg;
                }).whenComplete((pricePerKg, ex) -> {
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        if (pricePerKg <= 0.0) {
                            player.sendMessage("§cCannot buy: price unavailable right now.");
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                            return;
                        }

                        double totalCost = Math.round(pricePerKg * amountToBuy * 100.0) / 100.0;

                        if (MoneyManager.get(player) >= totalCost) {
                            if (MoneyManager.remove(player, totalCost)) {
                                int currentStock = PortfolioManager.getStockAmount(player, commodity);
                                PortfolioManager.updateStock(player, commodity, currentStock + amountToBuy);

                                player.sendMessage("§aYou bought §e" + amountToBuy + "§a shares of §6" + capitalize(commodity.getCommodityName()) + "§a for §c" + String.format("%.2f", totalCost) + "$");
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.3f);

                                openBuyActionInventory(player, commodity);
                            } else {
                                player.sendMessage("§cTransaction failed.");
                            }
                        } else {
                            player.sendMessage("§cNot enough money! You need §4" + String.format("%.2f", totalCost) + "$");
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                        }
                    });
                });
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(BuyStockMenuInventory) || event.getView().getTitle().startsWith("§2Select amount to buy")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        boolean sourceIsGui = event.getSource().equals(BuyStockMenuInventory) ||
                event.getSource().getHolder() == null && event.getSource().getSize() == 54;

        boolean destinationIsGui = event.getDestination().equals(BuyStockMenuInventory) ||
                event.getDestination().getHolder() == null && event.getDestination().getSize() == 54;

        if (event.getSource().equals(BuyStockMenuInventory) || event.getDestination().equals(BuyStockMenuInventory)) {
            event.setCancelled(true);
        }

        if (sourceIsGui || destinationIsGui) {
            event.setCancelled(true);
        }

        if (event.getSource().equals(BuyStockMenuInventory) || event.getDestination().equals(BuyStockMenuInventory)) {
            event.setCancelled(true);
            return;
        }

        if ((event.getSource().getHolder() == null && event.getSource().getSize() == 54) ||
                (event.getDestination().getHolder() == null && event.getDestination().getSize() == 54)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        if (event.getInventory().equals(BuyStockMenuInventory) || event.getView().getTitle().startsWith("§2Select amount to buy")) {
            event.setCancelled(true);
        }
    }
}