package org.jufyer.plugin.stock.gui;

import net.minecraft.world.item.BundleItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jufyer.plugin.stock.Main;
import org.jufyer.plugin.stock.getPrice.FetchFromDataFolder;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;
import org.jufyer.plugin.stock.moneySystem.MoneyManager;
import org.jufyer.plugin.stock.util.UnitConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.jufyer.plugin.stock.util.CreateCustomHeads.createCustomHead;
import static org.jufyer.plugin.stock.util.UtilityMethods.capitalize;
import static org.jufyer.plugin.stock.util.UtilityMethods.decapitalize;

public class SellItemGui implements Listener {

    public static final Inventory SellItemMenuInventory = Bukkit.createInventory(null, 54, "§6Sell item menu");
    private static final Map<UUID, TradeCommodity> openSellInventories = new HashMap<>();

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

        int i = 10;
        for (String itemName : STOCK_NAMES) {

            while (i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36) i++;

            TradeCommodity commodity = TradeCommodity.fromCommodityName(itemName);
            Material mat = commodity.getMaterial();

            ItemStack placeholder = new ItemStack(mat);
            ItemMeta placeholderMeta = placeholder.getItemMeta();
            placeholderMeta.setDisplayName("§e" + capitalize(itemName));
            placeholderMeta.setLore(Arrays.asList("§7Loading price…"));
            placeholder.setItemMeta(placeholderMeta);

            SellItemMenuInventory.setItem(i, placeholder);

            int slot = i;

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
    }

    public static void openSellItemInventory(Player player, TradeCommodity commodity) {
        Inventory SellItemInventory = Bukkit.createInventory(null, 54, "§4Put the items below! (" + capitalize(commodity.getCommodityName()) + ")");

        openSellInventories.put(player.getUniqueId(), commodity);

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

            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                currentPriceItemMeta.setLore(Arrays.asList("§eCurrent price: " + String.format("%.2f", pricePerKilo) + " $/kg"));
                currentPriceItem.setItemMeta(currentPriceItemMeta);
                SellItemInventory.setItem(4, currentPriceItem);
            });
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
                "   - You will get them back after selling.");

        bm.addPage("§6Page 3: Navigation\n" +
                "§0- Click the BARRIER to close the menu.\n" +
                "- Click on an item in the main menu to open its sell menu.\n");

        bm.setAuthor("");
        bm.setTitle("Help book");
        HelpSellBook.setItemMeta(bm);
        player.openBook(HelpSellBook);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        if (event.getCurrentItem() == null) return;

        if (event.getInventory().equals(SellItemMenuInventory)) {
            if (event.getCurrentItem().getType().equals(Material.ARROW)) {
                player.openInventory(VillagerInvTradingWorld.VillagerInvTradingWorld);
                event.setCancelled(true);
            }

            event.setCancelled(true);

            if (event.getCurrentItem().getItemMeta() == null || event.getCurrentItem().getItemMeta().getDisplayName() == null) return;

            if (event.getCurrentItem().getItemMeta().getDisplayName().startsWith("§e")) {

                String stockName = decapitalize(event.getCurrentItem().getItemMeta().getDisplayName().replace("§e" , ""));
                TradeCommodity commodity = TradeCommodity.fromCommodityName(stockName);
                openSellItemInventory(player, commodity);

            } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals("§cClose menu")) {
                player.closeInventory();
            } else if (event.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {
                openHelpSellBook(player);
            } else if (event.getCurrentItem().getType().equals(Material.ARROW)) {
                player.openInventory(VillagerInvTradingWorld.VillagerInvTradingWorld);
            }
            return;
        }

        if (event.getView().getTitle().startsWith("§4Put the items below!")) {

            TradeCommodity commodity = openSellInventories.get(player.getUniqueId());
            if (commodity == null) return; // Sollte nicht passieren, aber zur Sicherheit.

            ItemStack item = event.getCurrentItem();
            if (item == null) return;

            // Back-Button
            if (item.getType().equals(Material.ARROW) && event.getSlot() == 0) {
                player.openInventory(SellItemMenuInventory);
                event.setCancelled(true);
                return;
            }

            // Help-Button
            if (item.getType().equals(Material.PLAYER_HEAD) && event.getSlot() == 45) {
                openHelpSellBook(player);
                event.setCancelled(true);
                return;
            }

            // Close-Button
            if (item.getType().equals(Material.BARRIER) && event.getSlot() == 8) {
                player.closeInventory();
                event.setCancelled(true);
                return;
            }

            if (item.getType() == Material.LIME_DYE && event.getSlot() == 53) {
                Material sellingMaterial = event.getInventory().getItem(4) != null ? event.getInventory().getItem(4).getType() : null;
                if (sellingMaterial == null) {
                    event.setCancelled(true);
                    player.sendMessage("§cError: No commodity selected.");
                    return;
                }

                int itemCount = 0;
                List<ItemStack> Bundles = new ArrayList<>();
                List<ItemStack> ShulkerBoxes = new ArrayList<>();

                for (int i = 0; i <= 53; i++) {
                    boolean isBlocked = false;
                    for (int b : blocked) {
                        if (i == b) {
                            isBlocked = true;
                            break;
                        }
                    }
                    if (isBlocked) continue;

                    ItemStack currentItem = event.getInventory().getItem(i);
                    if (currentItem != null && !currentItem.equals(item)) {
                        if (currentItem.getType().equals(sellingMaterial)) {
                            itemCount += currentItem.getAmount();
                        } else if (currentItem.getType().name().endsWith("SHULKER_BOX")) {
                            if (currentItem.getItemMeta() instanceof BlockStateMeta meta && meta.getBlockState() instanceof ShulkerBox box) {
                                if (box.getInventory().contains(sellingMaterial)) {
                                    for (ItemStack shulkerBoxItemStack : box.getInventory().getContents()) {
                                        if (shulkerBoxItemStack != null && shulkerBoxItemStack.getType().equals(sellingMaterial)) {
                                            itemCount += shulkerBoxItemStack.getAmount();
                                            ShulkerBoxes.add(currentItem);
                                        }
                                    }
                                }
                            }
                        } else if (currentItem.getItemMeta() instanceof BundleMeta) {
                            BundleMeta meta = (BundleMeta) currentItem.getItemMeta();
                            for (ItemStack bundleItemStack : meta.getItems()) {
                                if (bundleItemStack != null && bundleItemStack.getType() == sellingMaterial) {
                                    itemCount += bundleItemStack.getAmount();
                                    Bundles.add(currentItem);
                                }
                            }
                        }
                    }
                }

                if (event.getInventory().getItem(4) != null && event.getInventory().getItem(4).getType().equals(sellingMaterial)) {
                    itemCount -= event.getInventory().getItem(4).getAmount();
                }

                if (itemCount <= 0) {
                    player.sendMessage("§cYou don't have any of the selected commodity to sell!");
                    event.setCancelled(true);
                    return;
                }


                Inventory giveItemsBackInv = Bukkit.createInventory(null, 54, "§4Take your items!");
                for (ItemStack bundle : Bundles) {
                    BundleMeta meta = (BundleMeta) bundle.getItemMeta();
                    List<ItemStack> noItems = new ArrayList<>();
                    meta.setItems(noItems);
                    bundle.setItemMeta(meta);
                    giveItemsBackInv.addItem(bundle);
                }

                for (ItemStack shulker : ShulkerBoxes) {
                    BlockStateMeta meta = (BlockStateMeta) shulker.getItemMeta();
                    ShulkerBox box = (ShulkerBox) meta.getBlockState();
                    box.getInventory().clear();
                    meta.setBlockState(box);
                    shulker.setItemMeta(meta);
                    giveItemsBackInv.addItem(shulker);
                }

                player.closeInventory();
                player.openInventory(giveItemsBackInv);

                int finalItemCount = itemCount;
                FetchFromDataFolder.getLatestByName(TradeCommodity.fromMaterial(sellingMaterial)).thenAccept(json -> {
                    double pricePerUnit = 0;
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
                    player.sendMessage(String.format("Added %.2f USD to your wallet. Your new balance is: " + MoneyManager.getFormatted(player) + " USD!", wholePrice));
                });

                openSellInventories.remove(player.getUniqueId());
                event.setCancelled(true);
                return;
            }

            if (event.getSlot() == 4 || item.getType().equals(Material.LIGHT_GRAY_STAINED_GLASS_PANE) || item.getType().equals(Material.NAME_TAG)) {
                event.setCancelled(true);
                return;
            }

            if (event.getSlot() == 4) {
                event.setCancelled(true);
                return;
            }

            if (clickedInventory != null && event.getClickedInventory().equals(player.getInventory())) {
                return;
            }

            if (clickedInventory != null && event.getClickedInventory().equals(event.getInventory())) {
                if (event.getClick().isShiftClick() || event.getClick() == ClickType.DOUBLE_CLICK) {
                    event.setCancelled(true);
                }
                Material sellingMaterial = event.getInventory().getItem(4).getType();
                if (item.getType().equals(sellingMaterial) || item.getType().name().endsWith("SHULKER_BOX") || item.getItemMeta() instanceof BundleMeta) {
                    if (item.getType().name().endsWith("SHULKER_BOX")) {
                        if (!(item.getItemMeta() instanceof BlockStateMeta meta && meta.getBlockState() instanceof ShulkerBox box && box.getInventory().contains(sellingMaterial))) {
                            event.setCancelled(true);
                        }
                    } else if (item.getItemMeta() instanceof BundleMeta meta && !meta.getItems().stream().anyMatch(i -> i.getType() == sellingMaterial)) {
                        event.setCancelled(true);
                    }
                    return;
                } else {
                    event.setCancelled(true);
                }
            }

        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().startsWith("§4Put the items below!")) {
            openSellInventories.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(SellItemMenuInventory)) {
            event.setCancelled(true);
            return;
        }
        if (event.getView().getTitle().startsWith("§4Put the items below!")) {
            for (int slot : blocked) {
                if (event.getRawSlots().contains(slot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getSource().equals(SellItemMenuInventory) || event.getDestination().equals(SellItemMenuInventory)) {
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