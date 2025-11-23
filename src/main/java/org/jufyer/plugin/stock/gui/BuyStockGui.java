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

public class BuyStockGui implements CommandExecutor, Listener {

    // Inventare analog zur SellItemGui
    public static Inventory BuyItemMenuInventory = Bukkit.createInventory(null, 54, "§aBuy stock menu");
    public static Inventory BuyActionInventory = Bukkit.createInventory(null, 54, "§2Select amount to buy");

    // Blockierte Slots für das Layout (Rand)
    private static final int[] blocked = {0,1,2,3,4,5,6,7,8, 9,17,18,26,27,35,36,44, 46,47,48,49,50,51,52};

    private static final List<String> STOCK_NAMES = Arrays.asList(
            "gold", "iron-ore", "copper", "rhodium", "platinum", "indium",
            "cobalt", "silicon", "coal", "natural-gas", "crude-oil", "uranium",
            "wheat", "corn", "coffee", "sugar", "cotton", "palm-oil",
            "orange-juice", "live-cattle", "milk", "sulfur"
    );

    public static void setBuyItemMenuInventory() {
        BuyItemMenuInventory.clear();

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("§7Back to overview");
        backItem.setItemMeta(backItemMeta);
        BuyItemMenuInventory.setItem(0, backItem);

        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta exitItemMeta = exitItem.getItemMeta();
        exitItemMeta.setDisplayName("§cClose menu");
        exitItem.setItemMeta(exitItemMeta);
        BuyItemMenuInventory.setItem(8, exitItem);

        int i = 10;
        for (String itemName : STOCK_NAMES) {
            // Freie Slots suchen – überspringe Ränder
            while (i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36) {
                i++;
            }

            TradeCommodity commodity = TradeCommodity.fromCommodityName(itemName);
            if (commodity == null) {
                i++;
                continue;
            }

            // Erstelle Platzhalter-Item (so sieht's nicht leer aus)
            ItemStack placeholder = new ItemStack(commodity.getMaterial());
            ItemMeta meta = placeholder.getItemMeta();
            meta.setDisplayName("§e" + capitalize(itemName));
            meta.setLore(Arrays.asList("§7Loading price...", "§eClick to view buy options"));
            placeholder.setItemMeta(meta);

            final int slotIndex = i; // capture current slot
            BuyItemMenuInventory.setItem(slotIndex, placeholder);

            // Asynchron Preis und Unit laden und dann Item aktualisieren (on main thread)
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
                    ItemStack itemStack = BuyItemMenuInventory.getItem(slotIndex);
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
                    BuyItemMenuInventory.setItem(slotIndex, itemStack);
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
        BuyItemMenuInventory.setItem(45, skull);
    }

    public static void openBuyActionInventory(Player player, TradeCommodity commodity) {
        BuyActionInventory.clear(); // Reset vor dem Öffnen

        // Info Item (Platzhalter)
        ItemStack infoItem = new ItemStack(commodity.getMaterial());
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e" + capitalize(commodity.getCommodityName()));
        infoMeta.setLore(Arrays.asList("§7Price per share: §aLoading...", "§7Your money: §6" + MoneyManager.getFormatted(player) + "$", "§7Owned shares: §b" + PortfolioManager.getStockAmount(player, commodity)));
        infoItem.setItemMeta(infoMeta);
        BuyActionInventory.setItem(4, infoItem);

        // Buttons als Platzhalter mit price=0 (werden später aktualisiert)
        BuyActionInventory.setItem(20, createBuyButton(0.0, 1));
        BuyActionInventory.setItem(22, createBuyButton(0.0, 10));
        BuyActionInventory.setItem(24, createBuyButton(0.0, 64));

        // 3. Standard Navigation
        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta exitItemMeta = exitItem.getItemMeta();
        exitItemMeta.setDisplayName("§cClose menu");
        exitItem.setItemMeta(exitItemMeta);
        BuyActionInventory.setItem(8, exitItem);

        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("§7Back to overview");
        backItem.setItemMeta(backItemMeta);
        BuyActionInventory.setItem(0, backItem); // Slot 0 für Zurück

        // 4. Filler Glass Panes
        ItemStack fillerItem = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerItemMeta = fillerItem.getItemMeta();
        fillerItemMeta.setDisplayName(" ");
        fillerItem.setItemMeta(fillerItemMeta);

        for (int idx = 0; idx < 54; idx++) {
            if (BuyActionInventory.getItem(idx) == null) {
                BuyActionInventory.setItem(idx, fillerItem);
            }
        }

        // 5. Help Head
        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE4MDMxNThjY2VlY2QyMjY4YTBhYTZmNTU2ZjYxMGY1ZjRhMWMzYzI2NDM1NTk3Njg0OGU0YTE5ZjQwMmMzZCJ9fX0=";
        ItemStack skull = createCustomHead(texture);
        ItemMeta skullItemMeta = skull.getItemMeta();
        skullItemMeta.setDisplayName("§6Help");
        skull.setItemMeta(skullItemMeta);
        BuyActionInventory.setItem(45, skull);

        // Open inventory immediately
        player.openInventory(BuyActionInventory);

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
            // round to 2 decimals for display
            pricePerKg = Math.round(pricePerKg * 100.0) / 100.0;
            return pricePerKg;
        }).whenComplete((pricePerKg, ex) -> {
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                // Update info item (slot 4) and buttons
                ItemStack info = BuyActionInventory.getItem(4);
                if (info != null && info.hasItemMeta()) {
                    ItemMeta im = info.getItemMeta();
                    String priceLine = pricePerKg <= 0.0 ? "§7Price per share: §cN/A" : "§7Price per share: §a" + String.format("%.2f", pricePerKg) + " $/kg";
                    im.setLore(Arrays.asList(
                            priceLine,
                            "§7Your money: §6" + MoneyManager.getFormatted(player) + "$",
                            "§7Owned shares: §b" + PortfolioManager.getStockAmount(player, commodity)
                    ));
                    info.setItemMeta(im);
                    BuyActionInventory.setItem(4, info);
                }

                // Update buttons with real price
                BuyActionInventory.setItem(20, createBuyButton(pricePerKg, 1));
                BuyActionInventory.setItem(22, createBuyButton(pricePerKg, 10));
                BuyActionInventory.setItem(24, createBuyButton(pricePerKg, 64));
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
            double totalCost = Math.round(price * amount * 100.0) / 100.0; // Preis * Menge auf 2 Nachkommastellen runden
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

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (commandSender instanceof Player player) {
            // Falls Parameter übergeben wurden (z.B. /buy gold) direkt öffnen, sonst Menü
            if (strings.length == 1) {
                TradeCommodity commodity = TradeCommodity.fromCommodityName(strings[0]);
                if (commodity != null) {
                    openBuyActionInventory(player, commodity);
                    return true;
                } else {
                    player.sendMessage("§cUnknown commodity. Opening menu...");
                }
            }
            player.openInventory(BuyItemMenuInventory);
        }
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getCurrentItem().hasItemMeta()) return;

        Inventory clickedInv = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();
        String displayName = clickedItem.getItemMeta().getDisplayName();

        // --- Hauptmenü Logik ---
        if (clickedInv.equals(BuyItemMenuInventory)) {
            event.setCancelled(true); // Nichts herausnehmen

            if (displayName.startsWith("§e")) {
                String stockName = decapitalize(displayName.replace("§e", ""));
                TradeCommodity commodity = TradeCommodity.fromCommodityName(stockName);

                if (commodity != null) {
                    player.sendMessage("§7Selected stock: §a" + capitalize(stockName));
                    openBuyActionInventory(player, commodity);
                }
            } else if (displayName.equals("§cClose menu")) {
                player.closeInventory();
            } else if (displayName.equals("§6Help")) {
                openHelpBuyBook(player);
            }
        }

        // --- Kauf Menü Logik ---
        if (clickedInv.equals(BuyActionInventory)) {
            event.setCancelled(true); // Nichts herausnehmen oder verschieben

            if (displayName.equals("§cClose menu")) {
                player.closeInventory();
                return;
            }
            if (displayName.equals("§7Back to overview")) {
                player.openInventory(BuyItemMenuInventory);
                return;
            }
            if (displayName.equals("§6Help")) {
                openHelpBuyBook(player);
                return;
            }

            // --- Kauf-Logik im InventoryClickEvent ---
            if (displayName.startsWith("§aBuy ")) {
                int amountToBuy = Integer.parseInt(displayName.replace("§aBuy ", "").replace(" shares", ""));
                ItemStack infoItem = clickedInv.getItem(4);
                if (infoItem == null || !infoItem.hasItemMeta()) return;
                String commodityName = decapitalize(infoItem.getItemMeta().getDisplayName().replace("§e", ""));
                TradeCommodity commodity = TradeCommodity.fromCommodityName(commodityName);
                if (commodity == null) return;

                // Asynchron Preis+Unit laden und dann erst die Transaction synchron ausführen
                CompletableFuture<Double> priceFuture = FetchFromDataFolder.getPrice(commodity);
                CompletableFuture<String> unitFuture = FetchFromDataFolder.getUnit(commodity);

                priceFuture.thenCombine(unitFuture, (priceRaw, unitRaw) -> {
                    double pricePerKg;
                    try {
                        pricePerKg = UnitConverter.toUSD(priceRaw, unitRaw, UnitConverter.OutputUnit.KG);
                    } catch (Exception e) {
                        pricePerKg = 0.0;
                    }
                    // round to 2 decimals for calculation/display
                    pricePerKg = Math.round(pricePerKg * 100.0) / 100.0;
                    return pricePerKg;
                }).whenComplete((pricePerKg, ex) -> {
                    // run transaction on main thread
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        if (pricePerKg <= 0.0) {
                            player.sendMessage("§cCannot buy: price unavailable right now.");
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                            return;
                        }

                        double totalCost = Math.round(pricePerKg * amountToBuy * 100.0) / 100.0;

                        // Geld prüfen und abziehen
                        if (MoneyManager.get(player) >= totalCost) {
                            if (MoneyManager.remove(player, totalCost)) {
                                int currentStock = PortfolioManager.getStockAmount(player, commodity);
                                PortfolioManager.updateStock(player, commodity, currentStock + amountToBuy);

                                player.sendMessage("§aYou bought §e" + amountToBuy + "§a shares of §6" + capitalize(commodityName) + "§a for §c" + String.format("%.2f", totalCost) + "$");
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.3f);

                                openBuyActionInventory(player, commodity); // GUI neu laden
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
        if (event.getInventory().equals(BuyItemMenuInventory) || event.getInventory().equals(BuyActionInventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getSource().equals(BuyItemMenuInventory) || event.getSource().equals(BuyActionInventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        if (event.getInventory().equals(BuyItemMenuInventory) || event.getInventory().equals(BuyActionInventory)) {
            event.setCancelled(true);
        }
    }
}
