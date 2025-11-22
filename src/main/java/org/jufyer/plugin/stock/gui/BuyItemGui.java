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
import org.jufyer.plugin.stock.moneySystem.Money;
import org.jufyer.plugin.stock.moneySystem.PortfolioManager;
import org.jufyer.plugin.stock.util.UnitConverter;

import java.util.Arrays;
import java.util.List;

import static org.jufyer.plugin.stock.util.CreateCustomHeads.createCustomHead;

public class BuyItemGui implements CommandExecutor, Listener {

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

    /**
     * Initialisiert das Hauptmenü mit allen verfügbaren Aktien.
     * Diese Methode sollte beim Plugin-Start einmal aufgerufen werden.
     */
    public static void setBuyItemMenuInventory() {
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
            if (commodity == null) continue;

            ItemStack itemStack = new ItemStack(commodity.getMaterial());
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName("§e" + capitalize(itemName));

            double priceRaw = FetchFromDataFolder.getPrice(commodity);       // Originalpreis
            String unitRaw = FetchFromDataFolder.getUnit(commodity);         // Originalunit

            double pricePerKilo = UnitConverter.toUSD(priceRaw, unitRaw, UnitConverter.OutputUnit.KG);

            meta.setLore(List.of(
                    "§7Current price: §a" + String.format("%.2f", pricePerKilo) + " $/kg",
                    "§eClick to view buy options"
            ));
            itemStack.setItemMeta(meta);

            BuyItemMenuInventory.setItem(i, itemStack);
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

    /**
     * Öffnet das spezifische Kauf-Inventar für eine Aktie.
     */
    public static void openBuyActionInventory(Player player, TradeCommodity commodity) {
        BuyActionInventory.clear(); // Reset vor dem Öffnen

        double price = FetchFromDataFolder.getPrice(commodity);
        String unit = FetchFromDataFolder.getUnit(commodity);
        String balance = Money.getFormatted(player);
        int owned = PortfolioManager.getStockAmount(player, commodity);

        // --- Info Item (Mitte Oben) ---
        ItemStack infoItem = new ItemStack(commodity.getMaterial());
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e" + capitalize(commodity.getCommodityName()));

        double priceRaw = FetchFromDataFolder.getPrice(commodity);       // Originalpreis
        String unitRaw = FetchFromDataFolder.getUnit(commodity);         // Originalunit

        // Preis in USD/Tonne und auf 2 Nachkommastellen runden
        double pricePerKilo = UnitConverter.toUSD(priceRaw, unitRaw, UnitConverter.OutputUnit.KG);
        price = Math.round(pricePerKilo * 100.0) / 100.0;

        balance = Money.getFormatted(player); // bereits formatiert
        owned = PortfolioManager.getStockAmount(player, commodity);

        infoMeta.setLore(Arrays.asList(
                "§7Price per share: §a" + String.format("%.2f", price) + " $/kg",
                "§7Your money: §6" + balance + "$",
                "§7Owned shares: §b" + owned
        ));
        infoItem.setItemMeta(infoMeta);
        BuyActionInventory.setItem(4, infoItem);

        // --- Buy Buttons ---
        BuyActionInventory.setItem(20, createBuyButton(price, 1));
        BuyActionInventory.setItem(22, createBuyButton(price, 10));
        BuyActionInventory.setItem(24, createBuyButton(price, 64));



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

        for (int i = 0; i < 54; i++) {
            if (BuyActionInventory.getItem(i) == null) {
                BuyActionInventory.setItem(i, fillerItem);
            }
        }

        // 5. Help Head
        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE4MDMxNThjY2VlY2QyMjY4YTBhYTZmNTU2ZjYxMGY1ZjRhMWMzYzI2NDM1NTk3Njg0OGU0YTE5ZjQwMmMzZCJ9fX0=";
        ItemStack skull = createCustomHead(texture);
        ItemMeta skullItemMeta = skull.getItemMeta();
        skullItemMeta.setDisplayName("§6Help");
        skull.setItemMeta(skullItemMeta);
        BuyActionInventory.setItem(45, skull);

        player.openInventory(BuyActionInventory);
    }

    private static ItemStack createBuyButton(double price, int amount) {
        ItemStack button = new ItemStack(Material.EMERALD);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName("§aBuy " + amount + " shares");

        double totalCost = Math.round(price * amount * 100.0) / 100.0; // Preis * Menge auf 2 Nachkommastellen runden

        meta.setLore(Arrays.asList(
                "§7Cost: §c" + String.format("%.2f", totalCost) + "$",
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

                // Preis wieder aus Daten + UnitConverter
                double priceRaw = FetchFromDataFolder.getPrice(commodity);
                String unitRaw = FetchFromDataFolder.getUnit(commodity);
                double pricePerKilo = UnitConverter.toUSD(priceRaw, unitRaw, UnitConverter.OutputUnit.KG);
                double price = Math.round(pricePerKilo * 100.0) / 100.0;

                double totalCost = Math.round(price * amountToBuy * 100.0) / 100.0; // totalCost ebenfalls runden

                // Geld prüfen und abziehen
                if (Money.get(player) >= totalCost) {
                    if (Money.remove(player, totalCost)) {
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

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        text = text.replace("-", " ");
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }
        return result.toString().trim();
    }

    private static String decapitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        text = text.toLowerCase();
        text = text.replace(" ", "-");
        return text;
    }
}