package org.jufyer.plugin.stock.gui;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jufyer.plugin.stock.Main;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StockGraph implements Listener {

    private static final Gson GSON = new Gson();
    private static final Map<Player, List<ItemFrame>> ACTIVE_PLAYER_GRAPHS = new HashMap<>();
//    private static final List<String> STOCK_NAMES = Arrays.asList(
//            "gold", "iron-ore", "copper", "rhodium", "platinum", "indium",
//            "cobalt", "silicon", "coal", "natural-gas", "crude-oil", "uranium",
//            "wheat", "corn", "coffee", "sugar", "cotton", "palm-oil",
//            "orange-juice", "live-cattle", "milk", "sulfur"
//    );
//
//    @Override
//    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
//        if (args.length == 1) {
//            String input = args[0].toLowerCase();
//            return STOCK_NAMES.stream()
//                    .filter(name -> name.startsWith(input))
//                    .collect(Collectors.toList());
//        }
//        return List.of();
//    }

    public static void displayStockGraph(Player player, String stockName) {

        List<StockDataPoint> dataPoints = loadStockData(stockName);

        if (dataPoints.isEmpty()) {
            player.sendMessage("§cNo data for stock '§e" + stockName + "§c' found!");
            return;
        }

        dataPoints.sort(Comparator.comparing(d -> d.date));

        World world = player.getWorld();
        Location startLoc = WorldManager.activeTraders.get(player.getUniqueId());

        int xOffset = 0, zOffset = 1;
        int xStep = -1, zStep = 0;

        int MAX_GRAPH_POINTS = 896;
        List<StockDataPoint> sampledData = downsampleData(dataPoints, MAX_GRAPH_POINTS);

        int mapsPerRow = 7;
        int rows = 4;

        double startX = startLoc.getX() + (xOffset * 2) - (xStep * 3);
        double startY = startLoc.getY() + 4;
        double startZ = startLoc.getZ() + (zOffset * 7) - (zStep * 3);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < mapsPerRow; col++) {
                double x = startX + (col * xStep);
                double y = startY - row;
                double z = startZ + (col * zStep);

                Location frameLoc = new Location(world, x, y, z, 0, 0);

                ItemFrame frame = (ItemFrame) world.spawnEntity(frameLoc, EntityType.ITEM_FRAME);

                frame.setFacingDirection(BlockFace.SOUTH);
                frame.setVisible(false);
                frame.setFixed(true);
                frame.setInvulnerable(true);
                frame.setCustomNameVisible(false);

                MapView view = Bukkit.createMap(world);
                view.getRenderers().clear();
                view.setScale(MapView.Scale.NORMAL);
                view.setTrackingPosition(false);
                view.addRenderer(new StockMapRenderer(sampledData, col, row, mapsPerRow, rows, stockName));

                ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) mapItem.getItemMeta();
                if (meta != null) {
                    meta.setMapView(view);
                    mapItem.setItemMeta(meta);
                }


                frame.setItem(mapItem);
                frame.setItemDropChance(0);

                ACTIVE_PLAYER_GRAPHS.computeIfAbsent(player, k -> new ArrayList<>()).add(frame);

                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (p.equals(player)) continue;
                    p.hideEntity(Main.getInstance(), frame);
                }
            }
        }
    }


    private static List<StockDataPoint> loadStockData(String stockName) {
        List<StockDataPoint> points = new ArrayList<>();
        File dataFolder = new File(Main.getInstance().getDataFolder(), "data");
        File stockFile = new File(dataFolder, stockName + ".json");

        if (!stockFile.exists()) {
            return points;
        }

        try (FileReader reader = new FileReader(stockFile)) {
            JsonArray array = GSON.fromJson(reader, JsonArray.class);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

            for (int i = 0; i < array.size(); i++) {
                JsonObject obj = array.get(i).getAsJsonObject();
                try {
                    Date date = sdf.parse(obj.get("date").getAsString());
                    double value = Double.parseDouble(obj.get("value").getAsString());
                    String currency = obj.get("currency").getAsString();
                    points.add(new StockDataPoint(date, value, currency));
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            Main.getInstance().getLogger().warning("Error loading " + stockName + ".json: " + e.getMessage());
        }

        return points;
    }

    private static List<StockDataPoint> downsampleData(List<StockDataPoint> data, int maxPoints) {
        if (data.size() <= maxPoints) {
            return new ArrayList<>(data);
        }

        List<StockDataPoint> sampled = new ArrayList<>();
        double step = (double) data.size() / maxPoints;

        for (int i = 0; i < maxPoints; i++) {
            int index = (int) (i * step);
            sampled.add(data.get(index));
        }

        return sampled;
    }

    private static class StockDataPoint {
        final Date date;
        final double value;
        final String currency;

        StockDataPoint(Date date, double value, String currency) {
            this.date = date;
            this.value = value;
            this.currency = currency;
        }
    }

    public static class StockMapRenderer extends MapRenderer {
        private final List<StockDataPoint> dataPoints;
        private final int tileX, tileY;
        private final int tilesWide, tilesHigh;
        private final String stockName;

        public StockMapRenderer(List<StockDataPoint> dataPoints, int tileX, int tileY, int tilesWide, int tilesHigh, String stockName) {
            this.dataPoints = dataPoints;
            this.tileX = tileX;
            this.tileY = tileY;
            this.tilesWide = tilesWide;
            this.tilesHigh = tilesHigh;
            this.stockName = stockName;
        }

        @Override
        public void render(MapView view, MapCanvas canvas, Player player) {
            for (int x = 0; x < 128; x++) {
                for (int y = 0; y < 128; y++) {
                    canvas.setPixel(x, y, MapPalette.WHITE);
                }
            }

            if (dataPoints.isEmpty()) return;

            double minValue = dataPoints.stream().mapToDouble(p -> p.value).min().orElse(0);
            double maxValue = dataPoints.stream().mapToDouble(p -> p.value).max().orElse(100);
            double valueRange = maxValue - minValue;
            if (valueRange == 0) valueRange = 1;

            int totalWidth = tilesWide * 128;
            int totalHeight = tilesHigh * 128;

            int offsetX = tileX * 128;
            int offsetY = tileY * 128;

            int axisX = 30;
            int axisY = totalHeight - 30;

            if (tileX == 0) {
                for (int y = 0; y < 128; y++) {
                    int globalY = offsetY + y;
                    if (globalY >= 20 && globalY <= axisY) {
                        canvas.setPixel(axisX, y, MapPalette.WHITE);
                    }
                }
            }

            if (offsetY <= axisY && offsetY + 128 > axisY) {
                int localAxisY = axisY - offsetY;
                for (int x = 0; x < 128; x++) {
                    int globalX = offsetX + x;
                    if (globalX >= axisX && globalX < totalWidth - 20) {
                        canvas.setPixel(x, localAxisY, MapPalette.WHITE);
                    }
                }
            }

            int graphWidth = totalWidth - axisX - 20;
            int graphHeight = axisY - 20;

            for (int i = 0; i < dataPoints.size() - 1; i++) {
                double value1 = dataPoints.get(i).value;
                double value2 = dataPoints.get(i + 1).value;

                double norm1 = (value1 - minValue) / valueRange;
                double norm2 = (value2 - minValue) / valueRange;

                int globalX1 = axisX + (i * graphWidth / dataPoints.size());
                int globalY1 = axisY - (int) (norm1 * graphHeight);
                int globalX2 = axisX + ((i + 1) * graphWidth / dataPoints.size());
                int globalY2 = axisY - (int) (norm2 * graphHeight);

                if (isLineSegmentVisible(globalX1, globalY1, globalX2, globalY2, offsetX, offsetY)) {
                    int localX1 = globalX1 - offsetX;
                    int localY1 = globalY1 - offsetY;
                    int localX2 = globalX2 - offsetX;
                    int localY2 = globalY2 - offsetY;

                    byte color = (value2 >= value1) ? MapPalette.LIGHT_GREEN : MapPalette.RED;
                    drawBresenhamLine(canvas, localX1, localY1, localX2, localY2, color);
                }
            }

            if (tileX == 0 && tileY == 0) {
                drawMapText(canvas, 2, 5, stockName.toUpperCase());
                drawMapText(canvas, 2, 15, String.format("%.2f", maxValue));
            }
            if (tileX == 0 && offsetY <= axisY && offsetY + 128 > axisY) {
                int localAxisY = axisY - offsetY;
                drawMapText(canvas, 2, localAxisY - 5, String.format("%.2f", minValue));
            }
        }

        private boolean isLineSegmentVisible(int x1, int y1, int x2, int y2, int tileX, int tileY) {
            int tileRight = tileX + 128;
            int tileBottom = tileY + 128;

            boolean p1Inside = (x1 >= tileX && x1 < tileRight && y1 >= tileY && y1 < tileBottom);
            boolean p2Inside = (x2 >= tileX && x2 < tileRight && y2 >= tileY && y2 < tileBottom);

            if (p1Inside || p2Inside) return true;

            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);

            return !(maxX < tileX || minX >= tileRight || maxY < tileY || minY >= tileBottom);
        }

        private void drawBresenhamLine(MapCanvas canvas, int x1, int y1, int x2, int y2, byte color) {
            int dx = Math.abs(x2 - x1);
            int dy = Math.abs(y2 - y1);
            int sx = x1 < x2 ? 1 : -1;
            int sy = y1 < y2 ? 1 : -1;
            int err = dx - dy;
            while (true) {
                if (x1 >= 0 && y1 >= 0 && x1 < 128 && y1 < 128) {
                    canvas.setPixel(x1, y1, color);
                }
                if (x1 == x2 && y1 == y2) break;
                int e2 = 2 * err;
                if (e2 > -dy) { err -= dy; x1 += sx; }
                if (e2 < dx) { err += dx; y1 += sy; }
            }
        }

        private void drawMapText(MapCanvas canvas, int x, int y, String text) {
            MapFont font = MinecraftFont.Font;
            canvas.drawText(x, y, font, text);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getLocation().getWorld().equals(Bukkit.getWorld("trade_world"))) {
            WorldManager.endTrade(event.getPlayer());
        }
    }
}