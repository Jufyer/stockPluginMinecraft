package org.jufyer.plugin.stock.chatImplementation;

import java.util.*;

public class ASCIIBarChart {

    public static void main(String[] args) {
        List<Double> prices = Arrays.asList(503.2, 503.2, 503.9, 503.9, 504.1, 503.8);
        int height = 10; // Höhe des Charts

        double min = Collections.min(prices);
        double max = Collections.max(prices);

        for (int h = height; h >= 1; h--) {
            StringBuilder line = new StringBuilder();
            for (double price : prices) {
                // Skaliere auf Höhe
                int barHeight = (int)((price - min) / (max - min) * height);
                if (barHeight >= h) line.append("█");
                else line.append(" ");
            }
            System.out.println(line);
        }

        // X-Achse
        for (int i = 0; i < prices.size(); i++) System.out.print("─");
        System.out.println();

        // Werte anzeigen (optional)
        for (double price : prices) System.out.printf("%.1f ", price);
    }
}

