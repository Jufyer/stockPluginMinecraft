package org.jufyer.plugin.stock.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.NotNull;
import org.jufyer.plugin.stock.Main;
import org.jufyer.plugin.stock.getPrice.FetchFromDataFolder;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;
import org.jufyer.plugin.stock.moneySystem.MoneyManager;
import org.jufyer.plugin.stock.util.UnitConverter;

import java.util.Arrays;
import java.util.List;

import static org.jufyer.plugin.stock.util.CreateCustomHeads.createCustomHead;
import static org.jufyer.plugin.stock.util.UtilityMethods.capitalize;
import static org.jufyer.plugin.stock.util.UtilityMethods.decapitalize;

public class SellItemGui implements CommandExecutor, Listener {
    public static Inventory SellItemMenuInventory = Bukkit.createInventory(null, 54, "§6Sell item menu");
    public static Inventory SellItemInventory = Bukkit.createInventory(null, 54, "§4Put the items below!");

    private static final int[] blocked = {1,2,3,5,6,7,9,17,18,26,27,35,36,44,46,47,48,49,50,51,52};

    private static final List<String> STOCK_NAMES = Arrays.asList(
            "gold", "iron-ore", "copper", "rhodium", "platinum", "indium",
            "cobalt", "silicon", "coal", "natural-gas", "crude-oil", "uranium",
            "wheat", "corn", "coffee", "sugar", "cotton", "palm-oil",
            "orange-juice", "live-cattle", "milk", "sulfur"
    );

    public static void setSellItemMenuInventory() {
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("§7Back to overview");
        backItem.setItemMeta(backItemMeta);
        SellItemMenuInventory.setItem(0, backItem);

        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta exitItemMeta = exitItem.getItemMeta();
        exitItemMeta.setDisplayName("§cClose menu");
        exitItem.setItemMeta(exitItemMeta);
        SellItemMenuInventory.setItem(8, exitItem);

//        ItemStack stockItem = new ItemStack(Material.WHEAT);
//        ItemMeta meta = stockItem.getItemMeta();
//        meta.setDisplayName("§e" + "Wheat");
//        meta.setLore(List.of("§7Aktueller Preis: §a"
//                + FetchFromDataFolder.getPrice(TradeCommodity.WHEAT)
//                + " "
//                + FetchFromDataFolder.getUnit(TradeCommodity.WHEAT)));
//        stockItem.setItemMeta(meta);
//        SellItemMenuInventory.setItem(13, stockItem);

        int i = 10;
        for (String itemName : STOCK_NAMES) {

            // Freie Slots überspringen
            while (i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36) i++;

            TradeCommodity commodity = TradeCommodity.fromCommodityName(itemName);
            Material mat = commodity.getMaterial();

            // Platzhalter-Item (damit GUI sofort geladen wird)
            ItemStack placeholder = new ItemStack(mat);
            ItemMeta placeholderMeta = placeholder.getItemMeta();
            placeholderMeta.setDisplayName("§e" + capitalize(itemName));
            placeholderMeta.setLore(Arrays.asList("§7Loading price…"));
            placeholder.setItemMeta(placeholderMeta);

            SellItemMenuInventory.setItem(i, placeholder);

            int slot = i;

            // Async Preis laden
            FetchFromDataFolder.getLatestByName(commodity).thenAccept(json -> {
                if (json == null) return;

                double priceRaw = -1.0;

                String valStr = json.getString("value", null);

                if (valStr != null) {
                    try {
                        priceRaw = Double.parseDouble(valStr.replace(',', '.'));
                    } catch (NumberFormatException ignored) {
                    }
                } else {
                    try {
                        priceRaw = json.getJsonNumber("price").doubleValue();
                    } catch (Exception ignored) {
                    }
                }

                String unitRaw = json.getString("currency", "");

                double pricePerKg = UnitConverter.toUSD(priceRaw, unitRaw, UnitConverter.OutputUnit.KG);

                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    ItemStack itemStack = new ItemStack(mat);
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName("§e" + capitalize(itemName));
                    meta.setLore(Arrays.asList("§7Current price: §a" + String.format("%.2f", pricePerKg) + " $/kg"));
                    itemStack.setItemMeta(meta);
                    SellItemMenuInventory.setItem(slot, itemStack);
                });
            });

            i++;
        }

        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE4MDMxNThjY2VlY2QyMjY4YTBhYTZmNTU2ZjYxMGY1ZjRhMWMzYzI2NDM1NTk3Njg0OGU0YTE5ZjQwMmMzZCJ9fX0=";
        ItemStack skull = createCustomHead(texture);
        ItemMeta skullItemMeta = skull.getItemMeta();
        skullItemMeta.setDisplayName("§6Help");
        skull.setItemMeta(skullItemMeta);
        SellItemMenuInventory.setItem(45, skull);

        // Kaufen-Button
//        ItemStack buy = new ItemStack(Material.LIME_CONCRETE);
//        ItemMeta buyMeta = buy.getItemMeta();
//        buyMeta.setDisplayName("§aBuy");
//        buyMeta.setLore(List.of("§7Buy for current price"));
//        buy.setItemMeta(buyMeta);
//        SellItemMenuInventory.setItem(24, buy);

        // Verkaufen-Button
//        ItemStack sell = new ItemStack(Material.RED_CONCRETE);
//        ItemMeta sellMeta = sell.getItemMeta();
//        sellMeta.setDisplayName("§cSell");
//        sellMeta.setLore(List.of("§7Sell your " + "Wheat"));
//        sell.setItemMeta(sellMeta);
//        SellItemMenuInventory.setItem(20, sell);

        // Info (aktueller Kontostand)
//        ItemStack info = new ItemStack(Material.PAPER);
//        ItemMeta infoMeta = info.getItemMeta();
//        infoMeta.setDisplayName("§bDein Kontostand");
//        infoMeta.setLore(List.of("§7" + wallet.get()));
//        info.setItemMeta(infoMeta);
//        SellItemMenuInventory.setItem(22, info);
    }

    public static void openSellItemInventory(Player player, TradeCommodity commodity) {
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backItemMeta = backItem.getItemMeta();
        backItemMeta.setDisplayName("§7Back to overview");
        backItem.setItemMeta(backItemMeta);
        SellItemInventory.setItem(0, backItem);

        ItemStack currentPriceItem = new ItemStack(commodity.getMaterial());
        ItemMeta currentPriceItemMeta = currentPriceItem.getItemMeta();
        currentPriceItemMeta.setDisplayName("§r§e" + capitalize(commodity.getCommodityName()));

        FetchFromDataFolder.getLatestByName(commodity).thenAccept(json -> {
                    if (json == null) return;

                    double priceRaw = -1.0;

                    String valStr = json.getString("value", null);

                    if (valStr != null) {
                        try {
                            priceRaw = Double.parseDouble(valStr.replace(',', '.'));
                        } catch (NumberFormatException ignored) {
                        }
                    } else {
                        try {
                            priceRaw = json.getJsonNumber("price").doubleValue();
                        } catch (Exception ignored) {
                        }
                    }

                    String unitRaw = json.getString("currency", "");

                    double pricePerKilo = UnitConverter.toUSD(priceRaw, unitRaw, UnitConverter.OutputUnit.KG);

                    currentPriceItemMeta.setLore(Arrays.asList("§eCurrent price: " + String.format("%.2f", pricePerKilo) + " $/kg"));
                    currentPriceItem.setItemMeta(currentPriceItemMeta);
                    SellItemInventory.setItem(4, currentPriceItem);
        });

        ItemStack exitItem = new ItemStack(Material.BARRIER);
        ItemMeta exitItemMeta = exitItem.getItemMeta();
        exitItemMeta.setDisplayName("§cClose menu");
        exitItem.setItemMeta(exitItemMeta);
        SellItemInventory.setItem(8, exitItem);

        ItemStack sellItem = new ItemStack(Material.LIME_DYE);
        ItemMeta sellItemMeta = sellItem.getItemMeta();
        sellItemMeta.setDisplayName("§aSell items");
        sellItem.setItemMeta(sellItemMeta);
        SellItemInventory.setItem(53, sellItem);

        for (int i = 0; i < 54; i++) {
            boolean match = false;
            for (int b : blocked) {
                if (i == b) {
                    match = true;
                    break;
                }
            }

            if (match) {
                ItemStack fillerItem = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                ItemMeta fillerItemMeta = fillerItem.getItemMeta();
                fillerItemMeta.setDisplayName(" ");
                fillerItem.setItemMeta(fillerItemMeta);
                SellItemInventory.setItem(i, fillerItem);
            }
        }

        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE4MDMxNThjY2VlY2QyMjY4YTBhYTZmNTU2ZjYxMGY1ZjRhMWMzYzI2NDM1NTk3Njg0OGU0YTE5ZjQwMmMzZCJ9fX0=";
        ItemStack skull = createCustomHead(texture);
        ItemMeta skullItemMeta = skull.getItemMeta();
        skullItemMeta.setDisplayName("§6Help");
        skull.setItemMeta(skullItemMeta);
        SellItemInventory.setItem(45, skull);

        player.openInventory(SellItemInventory);
    }

    public static void openHelpSellBook(Player player) {
        ItemStack HelpSellBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bm = (BookMeta) HelpSellBook.getItemMeta();

        bm.addPage("§6Sell Item Help\n\n" +
                "Welcome to the Sell Item menu!\n\n" +
                "§6Page 1: Overview\n" +
                "§0- Put your items in the designated slots.\n" +
                "- Prices are shown per item type.\n" +
                "- Some slots are blocked and cannot be used.");

        bm.addPage("§6Page 2: Selling\n" +
                "§0- Click on 'Sell items' to sell your selected stock.\n" +
                "- Make sure the item type matches the stock you selected.\n" +
                "- Bundles and ShulkerBoxes containing your stock can also be sold.\n" +
                "   - You will get them back after selling.");

        bm.addPage("§6Page 3: Navigation\n" +
                "§0- Click the BARRIER to close the menu.\n" +
                "- Click on an item in the main menu to open its sell menu.\n");

        bm.setAuthor("");
        bm.setTitle("Help book");
        HelpSellBook.setItemMeta(bm);
        player.openBook(HelpSellBook);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (commandSender instanceof Player player) {
            player.openInventory(SellItemMenuInventory);
        }

        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;

        if (event.getInventory().equals(SellItemMenuInventory) && event.getCurrentItem().getItemMeta().getDisplayName().startsWith("§e")) {
            ItemStack item = event.getCurrentItem();

            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta().getDisplayName() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            String stockName = decapitalize(event.getCurrentItem().getItemMeta().getDisplayName().replace("§e" , ""));

            FetchFromDataFolder.getLatestByName(TradeCommodity.fromCommodityName(stockName)).thenAccept(json -> {
                        if (json == null) return;

                        double priceRaw = -1.0;

                        String valStr = json.getString("value", null);

                        if (valStr != null) {
                            try {
                                priceRaw = Double.parseDouble(valStr.replace(',', '.'));
                            } catch (NumberFormatException ignored) {
                            }
                        } else {
                            try {
                                priceRaw = json.getJsonNumber("price").doubleValue();
                            } catch (Exception ignored) {
                            }
                        }

                        String unitRaw = json.getString("currency", "");

                        double pricePerKilo = UnitConverter.toUSD(priceRaw, unitRaw, UnitConverter.OutputUnit.KG);

                        player.sendMessage("Price of "
                                + item.getItemMeta().getDisplayName()
                                + "§r is "
                                + String.format("%.2f", pricePerKilo) + " $/kg");

            });

            openSellItemInventory(player, TradeCommodity.fromCommodityName(stockName));

            event.setCancelled(true);
        } else if (event.getInventory().equals(SellItemMenuInventory) && event.getCurrentItem().getItemMeta().getDisplayName().equals("§cClose menu")) {
            event.getClickedInventory().close();
        } else if (event.getInventory().equals(SellItemMenuInventory) && event.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {
            openHelpSellBook(player);
        }else if (event.getCurrentItem().getType().equals(Material.ARROW)) {
            player.openInventory(VillagerInvTradingWorld.VillagerInvTradingWorld);
        }
        if (event.getInventory().equals(SellItemMenuInventory)) event.setCancelled(true);

        if (event.getInventory().equals(SellItemInventory)) {
            ItemStack item = event.getCurrentItem();

            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta().getDisplayName() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            if (event.getCurrentItem().getType().equals(Material.ARROW)) {
                player.openInventory(SellItemMenuInventory);
            }

            if (event.getInventory().equals(SellItemInventory) && event.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {
                openHelpSellBook(player);
            }

            if (event.getInventory().equals(SellItemInventory) && event.getCurrentItem().getType().equals(Material.BARRIER)) {
                event.getClickedInventory().close();
            }

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.LIME_DYE) {
                Material sellingMaterial = event.getInventory().getItem(4).getType();

                int itemCount = 0;
                int shulkerCount = 0;
                int bundleCount = 0;
                for (int i = 0; i <= 53; i++) {
                    boolean isBlocked = false;
                    for (int b : blocked) {
                        if (i == b) {
                            isBlocked = true;
                            break;
                        }
                    }
                    if (isBlocked) continue;

                    ItemStack currentItem = SellItemInventory.getItem(i);
                    if (currentItem != null) {
                        if (currentItem.getType().equals(event.getInventory().getItem(4).getType())) {
                            itemCount += currentItem.getAmount();
                        }else if (currentItem.getType().name().endsWith("SHULKER_BOX")) {
                            shulkerCount += 1;
                            if (!(currentItem.getItemMeta() instanceof BlockStateMeta meta)) {
                                event.setCancelled(true);
                                return;
                            }

                            if (!(meta.getBlockState() instanceof ShulkerBox box)) {
                                event.setCancelled(true);
                                return;
                            }

                            if (box.getInventory().contains(event.getInventory().getItem(4).getType())) {
                                for (ItemStack shulkerBoxItemStack : box.getInventory().getContents()) {
                                    if (shulkerBoxItemStack != null) {
                                        if (shulkerBoxItemStack.getType().equals(event.getInventory().getItem(4).getType())) {
                                            itemCount += shulkerBoxItemStack.getAmount();
                                        }
                                    }
                                }
                            } else {
                                event.setCancelled(true);
                            }
                        } else if (currentItem.getType() == Material.BUNDLE) {
                            bundleCount += 1;
                            BundleMeta meta = (BundleMeta) currentItem.getItemMeta();

                            Material slot0Item = event.getInventory().getItem(4).getType();
                            for (ItemStack bundleItemStack : meta.getItems()) {
                                if (bundleItemStack != null && bundleItemStack.getType() == slot0Item) {
                                    itemCount += bundleItemStack.getAmount();
                                }
                            }
                        }
                    }
                }
                itemCount = itemCount -1;

                player.sendMessage("Items: " + String.valueOf(itemCount));
                player.sendMessage("Shulker: " + String.valueOf(shulkerCount));
                player.sendMessage("Bundle: " + String.valueOf(bundleCount));

                SellItemInventory.clear();
                SellItemInventory.close();

                int finalItemCount = itemCount;
                FetchFromDataFolder.getLatestByName(TradeCommodity.fromMaterial(sellingMaterial)).thenAccept(json -> {

                    double pricePerUnit;
                    try {
                        if (json == null) return;

                        double priceRaw = -1.0;

                        String valStr = json.getString("value", null);

                        if (valStr != null) {
                            try {
                                priceRaw = Double.parseDouble(valStr.replace(',', '.'));
                            } catch (NumberFormatException ignored) {
                            }
                        } else {
                            try {
                                priceRaw = json.getJsonNumber("price").doubleValue();
                            } catch (Exception ignored) {
                            }
                        }

                        String unitRaw = json.getString("currency", "");

                        double price = UnitConverter.toUSD(priceRaw, unitRaw, UnitConverter.OutputUnit.KG);

                        pricePerUnit = price;

                    } catch (Exception e) {
                        Main.getInstance().getLogger().warning("Error converting the price for: " + sellingMaterial);
                        e.printStackTrace();
                        pricePerUnit = 0;
                    }

                double wholePrice = pricePerUnit * finalItemCount;
                MoneyManager.add(player, wholePrice);
                player.sendMessage(String.format("Added %.2f USD to your wallet. Your new balance is: " +  MoneyManager.getFormatted(player) +  " USD!", wholePrice));
                });
                event.setCancelled(true);
            }

            if (item.getType().equals(Material.LIGHT_GRAY_STAINED_GLASS_PANE) || item.getType().equals(Material.NAME_TAG)
                    || item.getType().equals(Material.LIME_DYE) || (item.getItemMeta().getDisplayName().startsWith("§e") && event.getSlot() == 45)) {
                event.setCancelled(true);
                return;
            }

            if (item.getType().name().endsWith("SHULKER_BOX") || item.getType().equals(Material.BUNDLE) || item.getType().equals(event.getInventory().getItem(4).getType())) {
                if (item.getType().name().endsWith("SHULKER_BOX")) {
                    if (!(item.getItemMeta() instanceof BlockStateMeta meta)) {
                        event.setCancelled(true);
                        return;
                    }

                    if (!(meta.getBlockState() instanceof ShulkerBox box)) {
                        event.setCancelled(true);
                        return;
                    }

                    if (box.getInventory().contains(event.getInventory().getItem(4).getType())) {

                    } else {
                        event.setCancelled(true);
                    }
                } else if (item.getType() == Material.BUNDLE) {
                    BundleMeta meta = (BundleMeta) item.getItemMeta();

                    boolean contains = meta.getItems().stream().anyMatch(i -> i.getType() == event.getInventory().getItem(4).getType());

                    if (contains) {
                        return;
                    } else {
                        event.setCancelled(true);
                        return;
                    }
                }
            }else {
                event.setCancelled(true);
            }
        } else if (event.getInventory().equals(SellItemInventory) && event.getCurrentItem().getItemMeta().getDisplayName().equals("§cClose menu")) {
            event.getClickedInventory().close();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(SellItemMenuInventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getSource().equals(SellItemMenuInventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryInteractEvent event) {
        if (event.getInventory().equals(SellItemMenuInventory)) {
            event.setCancelled(true);
        }
    }
}