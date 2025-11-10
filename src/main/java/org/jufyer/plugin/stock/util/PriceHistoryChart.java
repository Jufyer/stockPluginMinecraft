package org.jufyer.plugin.stock.util;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jufyer.plugin.stock.chatImplementation.stockLoader;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;

import java.util.List;
import java.util.stream.Collectors;

public class PriceHistoryChart {

    public void showChart(Player player, TradeCommodity commodity, int dataPoints, ChartStyle style) {
        try {
            List<PriceData> priceData = getHistoryWithDates(commodity, dataPoints);

            if (priceData == null || priceData.isEmpty()) {
                player.sendMessage("§cNo historical data available for " + commodity.getCommodityName());
                return;
            }

            switch (style) {
                case BAR:
                    sendBarChart(player, commodity, priceData);
                    break;
                case SPARKLINE:
                    sendSparkline(player, commodity, priceData);
                    break;
                case TREND:
                    sendTrendChart(player, commodity, priceData);
                    break;
                default:
                    sendBarChart(player, commodity, priceData);
            }
        } catch (Exception e) {
            player.sendMessage("§cError loading price history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<PriceData> getHistoryWithDates(TradeCommodity commodity, int n) throws Exception {
        List<stockLoader.PricePoint> points = stockLoader.loadHistory(commodity.getCommodityName());

        int size = points.size();
        List<stockLoader.PricePoint> relevantPoints;
        if (size <= n) {
            relevantPoints = points;
        } else {
            relevantPoints = points.subList(size - n, size);
        }

        return relevantPoints.stream()
                .map(p -> new PriceData(
                        Double.parseDouble(p.value),
                        formatDate(p.date)
                ))
                .collect(Collectors.toList());
    }

    private String formatDate(String dateStr) {
        try {
            // Format: "2025-10-23 18:30:02 UTC"
            String[] parts = dateStr.split(" ");
            String datePart = parts[0]; // "2025-10-23"
            String timePart = parts[1]; // "18:30:02"

            String[] dateSplit = datePart.split("-");
            String day = dateSplit[2];
            String month = dateSplit[1];

            String[] timeSplit = timePart.split(":");
            String hour = timeSplit[0];
            String minute = timeSplit[1];

            return day + "." + month + " " + hour + ":" + minute;
        } catch (Exception e) {
            return dateStr; // Fallback bei Fehler
        }
    }

    private void sendBarChart(Player player, TradeCommodity commodity, List<PriceData> priceData) {
        List<Double> prices = priceData.stream().map(p -> p.value).collect(Collectors.toList());

        double min = prices.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = prices.stream().mapToDouble(Double::doubleValue).max().orElse(100);
        double range = max - min;
        if (range == 0) range = 1;

        int maxBarLength = 30;

        player.sendMessage("§b§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6Price History: §f" + commodity.getCommodityName());
        player.sendMessage("");

        for (int i = 0; i < priceData.size(); i++) {
            PriceData data = priceData.get(i);
            double price = data.value;
            int barLength = (int) Math.round(((price - min) / range) * maxBarLength);

            String color = "§a";
            String trend = "";
            String trendText = "No change";
            if (i > 0) {
                double prev = priceData.get(i - 1).value;
                double diff = price - prev;
                double percentChange = (diff / prev) * 100;
                if (diff > 0) {
                    color = "§a";
                    trend = " §a▲";
                    trendText = String.format("Up +%.2f (%.2f%%)", diff, percentChange);
                } else if (diff < 0) {
                    color = "§c";
                    trend = " §c▼";
                    trendText = String.format("Down %.2f (%.2f%%)", diff, percentChange);
                } else {
                    color = "§e";
                    trend = " §e■";
                }
            }

            String bar = color + "█".repeat(Math.max(1, barLength));
            String label = String.format("§7#%-2d §8│ ", (i + 1));
            String value = String.format("§f%.2f%s", price, trend);

            TextComponent message = new TextComponent(label);

            TextComponent barComponent = new TextComponent(bar);
            barComponent.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("§6Data Point #" + (i + 1) +
                            "\n§7Date: §f" + data.date +
                            "\n§7Value: §f" + String.format("%.2f", price) +
                            "\n§7" + trendText).create()
            ));

            TextComponent valueComponent = new TextComponent(" §7" + value);

            message.addExtra(barComponent);
            message.addExtra(valueComponent);

            player.spigot().sendMessage(message);
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

    private void sendSparkline(Player player, TradeCommodity commodity, List<PriceData> priceData) {
        char[] sparks = {'▁', '▂', '▃', '▄', '▅', '▆', '▇', '█'};

        List<Double> prices = priceData.stream().map(p -> p.value).collect(Collectors.toList());
        double min = prices.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = prices.stream().mapToDouble(Double::doubleValue).max().orElse(100);
        double range = max - min;
        if (range == 0) range = 1;

        TextComponent message = new TextComponent("§8[§6" + commodity.getCommodityName() + "§8] ");

        for (int i = 0; i < priceData.size(); i++) {
            PriceData data = priceData.get(i);
            double price = data.value;
            int index = (int) Math.round(((price - min) / range) * (sparks.length - 1));

            TextComponent spark = new TextComponent("§a" + sparks[index]);

            String trendInfo = "First data point";
            if (i > 0) {
                double prev = priceData.get(i - 1).value;
                double diff = price - prev;
                double percentChange = (diff / prev) * 100;
                if (diff > 0) {
                    trendInfo = String.format("§a▲ +%.2f (%.2f%%)", diff, percentChange);
                } else if (diff < 0) {
                    trendInfo = String.format("§c▼ %.2f (%.2f%%)", diff, percentChange);
                } else {
                    trendInfo = "§e■ No change";
                }
            }

            spark.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("§6Point #" + (i + 1) +
                            "\n§7Date: §f" + data.date +
                            "\n§7Value: §f" + String.format("%.2f", price) +
                            "\n" + trendInfo).create()
            ));

            message.addExtra(spark);
        }

        double current = prices.get(prices.size() - 1);
        double first = prices.get(0);
        double change = ((current - first) / first) * 100;
        String trend = change > 0 ? "§a▲" : change < 0 ? "§c▼" : "§e■";

        message.addExtra(new TextComponent(" §7│ §f" + String.format("%.2f", current) +
                " " + trend + String.format(" §7%.1f%%", Math.abs(change))));

        player.spigot().sendMessage(message);
    }

    private void sendTrendChart(Player player, TradeCommodity commodity, List<PriceData> priceData) {
        player.sendMessage("§b━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§6Trend Analysis: §f" + commodity.getCommodityName());
        player.sendMessage("");

        for (int i = 0; i < priceData.size(); i++) {
            PriceData data = priceData.get(i);
            double current = data.value;
            String arrow = "§e→";
            String colorCode = "§f";
            String hoverText = "§6Data Point #" + (i + 1) +
                    "\n§7Date: §f" + data.date +
                    "\n§7Value: §f" + String.format("%.2f", current);

            if (i > 0) {
                PriceData prevData = priceData.get(i - 1);
                double prev = prevData.value;
                double diff = current - prev;
                double percentChange = (diff / prev) * 100;

                if (diff > 0) {
                    arrow = "§a↗";
                    colorCode = "§a";
                    hoverText += "\n§aUp: +" + String.format("%.2f", diff) + String.format(" (+%.2f%%)", percentChange);
                } else if (diff < 0) {
                    arrow = "§c↘";
                    colorCode = "§c";
                    hoverText += "\n§cDown: " + String.format("%.2f", diff) + String.format(" (%.2f%%)", percentChange);
                } else {
                    hoverText += "\n§eNo change from previous";
                }

                TextComponent message = new TextComponent(String.format("§7[%2d] ", i + 1));

                TextComponent dataPoint = new TextComponent(String.format("%s §f%7.2f %s§7(%s%+.2f%%§7)",
                        arrow, current, colorCode, colorCode, percentChange));

                dataPoint.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(hoverText + "\n§7Previous: §f" + String.format("%.2f", prev) + " §8(" + prevData.date + ")").create()
                ));

                message.addExtra(dataPoint);
                player.spigot().sendMessage(message);
            } else {
                hoverText += "\n§7Starting value";

                TextComponent message = new TextComponent(String.format("§7[%2d] ", i + 1));
                TextComponent dataPoint = new TextComponent(String.format("%s §f%7.2f §7(start)", arrow, current));

                dataPoint.setHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(hoverText).create()
                ));

                message.addExtra(dataPoint);
                player.spigot().sendMessage(message);
            }
        }

        player.sendMessage("");
        player.sendMessage("§b━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private static class PriceData {
        final double value;
        final String date;

        PriceData(double value, String date) {
            this.value = value;
            this.date = date;
        }
    }

    public enum ChartStyle {
        BAR,
        SPARKLINE,
        TREND
    }
}