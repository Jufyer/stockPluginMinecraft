package org.jufyer.plugin.stock.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jufyer.plugin.stock.Main;
import org.jufyer.plugin.stock.getPrice.FetchFromDataFolder;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;
import org.jufyer.plugin.stock.moneySystem.MoneyManager;
import org.jufyer.plugin.stock.moneySystem.PortfolioManager;
import org.jufyer.plugin.stock.util.UnitConverter;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.jufyer.plugin.stock.util.CreateCustomHeads.createCustomHead;
import static org.jufyer.plugin.stock.util.UtilityMethods.capitalize;
import static org.jufyer.plugin.stock.util.UtilityMethods.decapitalize;

public class SellStockGui implements Listener {

    public static final Inventory SellStockMenuInventory = Bukkit.createInventory(null, 54, "§aSell stock menu");
    private static final Map<UUID, TradeCommodity> openSellStockInventories = new HashMap<>();

    private static final List<String> STOCK_NAMES = Arrays.asList(
            "gold", "iron-ore", "copper", "rhodium", "platinum", "indium",
            "cobalt", "silicon", "coal", "natural-gas", "crude-oil", "uranium",
            "wheat", "corn", "coffee", "sugar", "cotton", "palm-oil",
            "orange-juice", "live-cattle", "milk", "sulfur"
    );

    public static void setSellStockMenuInventory() {
        SellStockMenuInventory.clear();

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("§7Back to overview");
        backItem.setItemMeta(backItemMeta);
        SellStockMenuInventory.setItem(0, backItem);

        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta exitItemMeta = exitItem.getItemMeta();
        exitItemMeta.setDisplayName("§cClose menu");
        exitItem.setItemMeta(exitItemMeta);
        SellStockMenuInventory.setItem(8, exitItem);

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
            meta.setLore(Arrays.asList("§7Loading price...", "§eClick to view selling options"));
            placeholder.setItemMeta(meta);

            final int slotIndex = i;
            SellStockMenuInventory.setItem(slotIndex, placeholder);

            CompletableFuture<Double> priceFuture = FetchFromDataFolder.getPrice(commodity);
            CompletableFuture<String> unitFuture = FetchFromDataFolder.getUnit(commodity);

            priceFuture.thenCombine(unitFuture, (priceRaw, unitRaw) -> {
                // compute price per KG (safe defaults)
                double pricePerKg;
                try {
                    pricePerKg = UnitConverter.toUSD(priceRaw, unitRaw, UnitConverter.OutputUnit.KG);
                } catch (Exception e) {
                    pricePerKg = 0.0;
                }
                return pricePerKg;
            }).whenComplete((pricePerKg, ex) -> {
                // update inventory synchronously
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    // check inventory still exists and slot hasn't been changed
                    ItemStack itemStack = SellStockMenuInventory.getItem(slotIndex);
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
                    lore.add("§eClick to view selling options");
                    m.setLore(lore);
                    itemStack.setItemMeta(m);
                    SellStockMenuInventory.setItem(slotIndex, itemStack);
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
        SellStockMenuInventory.setItem(45, skull);
    }

    public static void openSellActionInventory(Player player, TradeCommodity commodity) {
        Inventory sellStockInventory = Bukkit.createInventory(null, 54, "§2Select amount to sell (" + capitalize(commodity.getCommodityName()) + ")");

        openSellStockInventories.put(player.getUniqueId(), commodity);

        ItemStack infoItem = new ItemStack(commodity.getMaterial());
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e" + capitalize(commodity.getCommodityName()));
        infoMeta.setLore(Arrays.asList("§7Price per share: §aLoading...", "§7Your money: §6" + MoneyManager.getFormatted(player) + "$", "§7Owned shares: §b" + PortfolioManager.getStockAmount(player, commodity)));
        infoItem.setItemMeta(infoMeta);
        sellStockInventory.setItem(4, infoItem);

        sellStockInventory.setItem(20, createSellButton(0.0, 1));
        sellStockInventory.setItem(22, createSellButton(0.0, 10));
        sellStockInventory.setItem(24, createSellButton(0.0, 64));

        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta exitItemMeta = exitItem.getItemMeta();
        exitItemMeta.setDisplayName("§cClose menu");
        exitItem.setItemMeta(exitItemMeta);
        sellStockInventory.setItem(8, exitItem);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("§7Back to overview");
        backItem.setItemMeta(backItemMeta);
        sellStockInventory.setItem(0, backItem);

        ItemStack fillerItem = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerItemMeta = fillerItem.getItemMeta();
        fillerItemMeta.setDisplayName(" ");
        fillerItem.setItemMeta(fillerItemMeta);

        for (int idx = 0; idx < 54; idx++) {
            if (sellStockInventory.getItem(idx) == null) {
                sellStockInventory.setItem(idx, fillerItem);
            }
        }

        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE4MDMxNThjY2VlY2QyMjY4YTBhYTZmNTU2ZjYxMGY1ZjRhMWMzYzI2NDM1NTk3Njg0OGU0YTE5ZjQwMmMzZCJ9fX0=";
        ItemStack skull = createCustomHead(texture);
        ItemMeta skullItemMeta = skull.getItemMeta();
        skullItemMeta.setDisplayName("§6Help");
        skull.setItemMeta(skullItemMeta);
        sellStockInventory.setItem(45, skull);

        player.openInventory(sellStockInventory);

        // Asynchron load price & unit, then update GUI (safely on main thread)
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
                if (!player.getOpenInventory().getTitle().startsWith("§2Select amount to sell")) return;
                Inventory currentInventory = player.getOpenInventory().getTopInventory();

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
                currentInventory.setItem(20, createSellButton(pricePerKg, 1));
                currentInventory.setItem(22, createSellButton(pricePerKg, 10));
                currentInventory.setItem(24, createSellButton(pricePerKg, 64));
            });
        });
    }

    private static ItemStack createSellButton(double price, int amount) {
        ItemStack button = new ItemStack(Material.EMERALD);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName("§aSell " + amount + " shares");

        String costString;
        if (price <= 0.0) {
            costString = "N/A";
        } else {
            double totalCost = Math.round(price * amount * 100.0) / 100.0;
            costString = String.format("%.2f", totalCost);
        }

        meta.setLore(Arrays.asList(
                "§7Total: §a" + costString + (price <= 0.0 ? "" : "$"),
                "§eClick to sell"
        ));
        button.setItemMeta(meta);
        return button;
    }


    public static void openHelpSellBook(Player player) {
        ItemStack helpBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bm = (BookMeta) helpBook.getItemMeta();

        // Page 1: General Overview
        bm.addPage("§6Sell Stock Help\n\n" +
                "Welcome to the Stock Market!\n\n" +
                "§6Page 1: Overview\n" +
                "§0- Click on a stock icon to view selling options.\n" +
                "- You can only sell stocks you currently own.\n" +
                "- Earnings are added to your balance.");

        // Page 2: Selling Mechanics
        bm.addPage("§6Page 2: Selling\n" +
                "§0- Select amounts: 1, 10, or 64.\n" +
                "- Ensure you have enough shares in your portfolio.\n" +
                "- Sold shares are removed immediately.");

        bm.setAuthor("StockMarket");
        bm.setTitle("Sell Help");
        helpBook.setItemMeta(bm);
        player.openBook(helpBook);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getCurrentItem().hasItemMeta()) return;

        Inventory clickedInv = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();
        String displayName = clickedItem.getItemMeta().getDisplayName();

        if (clickedInv.equals(SellStockMenuInventory)) {
            if (event.getCurrentItem().getType().equals(Material.ARROW)) {
                player.openInventory(VillagerInvTradingWorld.VillagerInvTradingWorld);
                event.setCancelled(true);
            }

            event.setCancelled(true);

            if (displayName.startsWith("§e")) {
                String stockName = decapitalize(displayName.replace("§e", ""));
                TradeCommodity commodity = TradeCommodity.fromCommodityName(stockName);

                if (commodity != null) {
                    //player.sendMessage("§7Selected stock: §a" + capitalize(stockName));
                    openSellActionInventory(player, commodity);
                }
            } else if (displayName.equals("§cClose menu")) {
                player.closeInventory();
            } else if (displayName.equals("§6Help")) {
                openHelpSellBook(player);
            }
        }

        if (event.getView().getTitle().startsWith("§2Select amount to sell")) {
            event.setCancelled(true);

            TradeCommodity commodity = openSellStockInventories.get(player.getUniqueId());
            if (commodity == null) return;

            if (displayName.equals("§cClose menu")) {
                player.closeInventory();
                openSellStockInventories.remove(player.getUniqueId());
                return;
            }
            if (displayName.equals("§7Back to overview")) {
                player.openInventory(SellStockMenuInventory);
                openSellStockInventories.remove(player.getUniqueId());
                return;
            }
            if (displayName.equals("§6Help")) {
                openHelpSellBook(player);
                return;
            }

            if (displayName.startsWith("§aSell ")) {
                int amountToSell = Integer.parseInt(displayName.replace("§aSell ", "").replace(" shares", ""));

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
                    // run transaction on main thread
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        if (pricePerKg <= 0.0) {
                            player.sendMessage("§cCannot sell: price unavailable right now.");
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                            return;
                        }

                        double totalCost = Math.round(pricePerKg * amountToSell * 100.0) / 100.0;

                        if (PortfolioManager.getStockAmount(player, commodity) >= amountToSell){
                            if (MoneyManager.add(player, totalCost)) {
                                int currentStock = PortfolioManager.getStockAmount(player, commodity);
                                PortfolioManager.updateStock(player, commodity, currentStock - amountToSell);

                                player.sendMessage("§aYou sold §e" + amountToSell + "§a shares of §6" + capitalize(commodity.getCommodityName()) + "§a for §c" + String.format("%.2f", totalCost) + "$");
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.3f);

                                openSellActionInventory(player, commodity);
                            } else {
                                player.sendMessage("§cTransaction failed.");
                            }
                        } else {
                            player.sendMessage("§cYou don't have enough stocks to sell.");
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                        }
                    });
                });
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().startsWith("§2Select amount to sell")) {
            openSellStockInventories.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(SellStockMenuInventory) || event.getView().getTitle().startsWith("§2Select amount to sell")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getSource().equals(SellStockMenuInventory) || event.getDestination().equals(SellStockMenuInventory)) {
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
        if (event.getInventory().equals(SellStockMenuInventory) || event.getView().getTitle().startsWith("§2Select amount to sell")) {
            event.setCancelled(true);
        }
    }
}