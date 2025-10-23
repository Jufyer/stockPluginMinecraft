package org.jufyer.plugin.stock.util;

import org.bukkit.entity.Player;
import org.jufyer.plugin.stock.chatImplementation.stockLoader;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PriceHistoryChart {

    /**
     * Zeigt ein Chart für eine Commodity an
     * @param player Der Spieler
     * @param commodity Die Commodity
     * @param dataPoints Anzahl der Datenpunkte (z.B. 10, 20, 30)
     * @param style Der Chart-Stil
     */
    public void showChart(Player player, TradeCommodity commodity, int dataPoints, ChartStyle style) {
        try {
            List<Double> prices = getHistory(commodity, dataPoints);

            if (prices == null || prices.isEmpty()) {
                player.sendMessage("§cNo historical data available for " + commodity.getCommodityName());
                return;
            }

            switch (style) {
                case LINE:
                    sendLineChart(player, commodity, prices);
                    break;
                case BAR:
                    sendBarChart(player, commodity, prices);
                    break;
                case SPARKLINE:
                    sendSparkline(player, commodity, prices);
                    break;
                case BOX:
                    sendBoxChart(player, commodity, prices);
                    break;
                case TREND:
                    sendTrendChart(player, commodity, prices);
                    break;
                default:
                    sendBarChart(player, commodity, prices);
            }
        } catch (Exception e) {
            player.sendMessage("§cError loading price history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lädt die Preishistorie
     */
    private List<Double> getHistory(TradeCommodity commodity, int n) throws Exception {
        List<stockLoader.PricePoint> points = stockLoader.loadHistory(commodity.getCommodityName());
        // Nur die letzten n Werte, in der richtigen Reihenfolge
        List<Double> values = points.stream()
                .map(p -> Double.parseDouble(p.value))
                .collect(Collectors.toList());
        int size = values.size();
        if (size <= n) return values;
        else return values.subList(size - n, size);
    }

    /**
     * STIL 1: Linien-Diagramm mit Box-Drawing Characters
     */
    private void sendLineChart(Player player, TradeCommodity commodity, List<Double> prices) {
        int height = 10;
        int width = prices.size();

        double min = prices.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = prices.stream().mapToDouble(Double::doubleValue).max().orElse(100);
        double range = max - min;
        if (range == 0) range = 1;

        List<Integer> normalized = new ArrayList<>();
        for (double price : prices) {
            int y = (int) Math.round(((price - min) / range) * (height - 1));
            normalized.add(y);
        }

        player.sendMessage("§b§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6Price History: §f" + commodity.getCommodityName());
        player.sendMessage("");

        for (int row = height - 1; row >= 0; row--) {
            double yValue = min + (range * row / (height - 1));
            String line = String.format("§7%7.2f §8│", yValue);

            for (int col = 0; col < width; col++) {
                int currentY = normalized.get(col);
                int prevY = col > 0 ? normalized.get(col - 1) : currentY;
                int nextY = col < width - 1 ? normalized.get(col + 1) : currentY;

                if (currentY == row) {
                    if (prevY < row && nextY < row) {
                        line += "§a╮";
                    } else if (prevY > row && nextY > row) {
                        line += "§a╰";
                    } else if (prevY < row && nextY > row) {
                        line += "§a╯";
                    } else if (prevY > row && nextY < row) {
                        line += "§a╭";
                    } else if (prevY == row || nextY == row) {
                        line += "§a─";
                    } else {
                        line += "§a●";
                    }
                } else if ((prevY <= row && currentY >= row) || (prevY >= row && currentY <= row)) {
                    line += "§a│";
                } else {
                    line += " ";
                }
            }

            player.sendMessage(line);
        }

        String xAxis = "§8         └";
        for (int i = 0; i < width - 1; i++) {
            xAxis += "─";
        }
        player.sendMessage(xAxis);

        String labels = "§7          ";
        for (int i = 0; i < width; i++) {
            if ((i + 1) % 5 == 0 || i == 0) {
                labels += "§f" + (i + 1);
                if (i + 1 < 10) labels += " ";
            } else {
                labels += "§8· ";
            }
        }
        player.sendMessage(labels);

        player.sendMessage("");
        player.sendMessage(String.format("§7Range: §f%.2f §7- §f%.2f", min, max));
        player.sendMessage("§b§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * STIL 2: Balken-Diagramm (Horizontal) - EMPFOHLEN
     */
    private void sendBarChart(Player player, TradeCommodity commodity, List<Double> prices) {
        double min = prices.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = prices.stream().mapToDouble(Double::doubleValue).max().orElse(100);
        double range = max - min;
        if (range == 0) range = 1;

        int maxBarLength = 30;

        player.sendMessage("§b§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6Price History: §f" + commodity.getCommodityName());
        player.sendMessage("");

        for (int i = 0; i < prices.size(); i++) {
            double price = prices.get(i);
            int barLength = (int) Math.round(((price - min) / range) * maxBarLength);

            String color = "§a";
            String trend = "";
            if (i > 0) {
                double diff = price - prices.get(i - 1);
                if (diff > 0) {
                    color = "§a";
                    trend = " §a▲";
                } else if (diff < 0) {
                    color = "§c";
                    trend = " §c▼";
                } else {
                    color = "§e";
                    trend = " §e■";
                }
            }

            String bar = color + "█".repeat(Math.max(1, barLength));
            String label = String.format("§7#%-2d §8│ ", (i + 1));
            String value = String.format("§f%.2f%s", price, trend);

            player.sendMessage(label + bar + " §7" + value);
        }

        double avg = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double current = prices.get(prices.size() - 1);
        double first = prices.get(0);
        double change = ((current - first) / first) * 100;

        player.sendMessage("");
        player.sendMessage(String.format("§7Min: §f%.2f §8│ §7Max: §f%.2f §8│ §7Avg: §f%.2f", min, max, avg));
        player.sendMessage(String.format("§7Change: %s%.2f%% §8(§f%.2f §7→ §f%.2f§8)",
                change >= 0 ? "§a+" : "§c", change, first, current));
        player.sendMessage("§b§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * STIL 3: Sparkline (kompakt, einzeilig)
     */
    private void sendSparkline(Player player, TradeCommodity commodity, List<Double> prices) {
        char[] sparks = {'▁', '▂', '▃', '▄', '▅', '▆', '▇', '█'};

        double min = prices.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = prices.stream().mapToDouble(Double::doubleValue).max().orElse(100);
        double range = max - min;
        if (range == 0) range = 1;

        StringBuilder sparkline = new StringBuilder("§a");
        for (double price : prices) {
            int index = (int) Math.round(((price - min) / range) * (sparks.length - 1));
            sparkline.append(sparks[index]);
        }

        double current = prices.get(prices.size() - 1);
        double first = prices.get(0);
        double change = ((current - first) / first) * 100;
        String trend = change > 0 ? "§a▲" : change < 0 ? "§c▼" : "§e■";

        player.sendMessage("§8[§6" + commodity.getCommodityName() + "§8] " +
                sparkline + " §7│ §f" + String.format("%.2f", current) +
                " " + trend + String.format(" §7%.1f%%", Math.abs(change)));
    }

    /**
     * STIL 4: Box-Chart mit Rahmen
     */
    private void sendBoxChart(Player player, org.jufyer.plugin.stock.getPrice.TradeCommodity commodity, List<Double> prices) {
        int height = 8;
        double min = prices.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = prices.stream().mapToDouble(Double::doubleValue).max().orElse(100);
        double range = max - min;
        if (range == 0) range = 1;

        String commodityName = commodity.getCommodityName();
        int padding = Math.max(0, 25 - commodityName.length());

        player.sendMessage("§b┌─────────────────────────────────────┐");
        player.sendMessage("§b│ §6Price History: §f" + commodityName + " ".repeat(padding) + "§b│");
        player.sendMessage("§b├─────────────────────────────────────┤");

        for (int row = height - 1; row >= 0; row--) {
            double yValue = min + (range * row / (height - 1));
            String line = String.format("§b│ §7%7.2f §8┤ ", yValue);

            for (int col = 0; col < prices.size(); col++) {
                int normalizedY = (int) Math.round(((prices.get(col) - min) / range) * (height - 1));

                if (normalizedY > row) {
                    line += "§a█";
                } else if (normalizedY == row) {
                    line += "§e▀";
                } else {
                    line += "§8░";
                }
            }

            // Padding am Ende
            int remainingSpace = Math.max(0, 27 - prices.size());
            line += " ".repeat(remainingSpace) + " §b│";
            player.sendMessage(line);
        }

        double avg = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        player.sendMessage("§b├─────────────────────────────────────┤");
        player.sendMessage(String.format("§b│ §7Min: §f%6.2f §8│ §7Max: §f%6.2f §8│ §7Avg: §f%6.2f §b│",
                min, max, avg));
        player.sendMessage("§b└─────────────────────────────────────┘");
    }

    /**
     * STIL 5: Trend-Anzeige mit Pfeilen und Prozent
     */
    private void sendTrendChart(Player player, org.jufyer.plugin.stock.getPrice.TradeCommodity commodity, List<Double> prices) {
        player.sendMessage("§b━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6Trend Analysis: §f" + commodity.getCommodityName());
        player.sendMessage("");

        for (int i = 0; i < prices.size(); i++) {
            double current = prices.get(i);
            String arrow = "§e→";
            String colorCode = "§f";

            if (i > 0) {
                double prev = prices.get(i - 1);
                double diff = current - prev;
                double percentChange = (diff / prev) * 100;

                if (diff > 0) {
                    arrow = "§a↗";
                    colorCode = "§a";
                } else if (diff < 0) {
                    arrow = "§c↘";
                    colorCode = "§c";
                }

                player.sendMessage(String.format("§7[%2d] %s §f%7.2f %s§7(%s%+.2f%%§7)",
                        i + 1, arrow, current, colorCode, colorCode, percentChange));
            } else {
                player.sendMessage(String.format("§7[%2d] %s §f%7.2f §7(start)",
                        i + 1, arrow, current));
            }
        }

        player.sendMessage("");
        player.sendMessage("§b━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * Chart-Stile
     */
    public enum ChartStyle {
        LINE,       // Linien-Diagramm mit ╭╮╯╰
        BAR,        // Horizontale Balken (Standard)
        SPARKLINE,  // Kompakte einzeilige Darstellung
        BOX,        // Mit Box-Rahmen
        TREND       // Mit Pfeilen und Prozent
    }
}