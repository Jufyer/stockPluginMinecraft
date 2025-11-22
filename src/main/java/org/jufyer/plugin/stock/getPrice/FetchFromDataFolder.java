package org.jufyer.plugin.stock.getPrice;

import org.bukkit.Bukkit;
import org.jufyer.plugin.stock.Main;
import javax.json.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

import static org.jufyer.plugin.stock.util.UtilityMethods.loadJsonAsync;

public class FetchFromDataFolder {

    // Format: "2025-10-22 19:06:57 UTC"
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");

    private static JsonArray data;

    public static CompletableFuture<JsonObject> getLatestByName(TradeCommodity commodity) {

        String name = commodity.getCommodityName();
        String filePath = Main.getInstance().getDataFolder() + "/data/" + name + ".json";

        return loadJsonAsync(filePath).thenApply(data -> {

            JsonObject latest = null;
            LocalDateTime latestDateTime = null;

            for (JsonValue value : data) {
                if (!(value instanceof JsonObject obj)) continue;

                String itemName = obj.getString("name", null);
                String dateStr  = obj.getString("date", null);

                if (itemName == null || dateStr == null) continue;
                if (!itemName.equals(name)) continue;

                try {
                    LocalDateTime current = LocalDateTime.parse(dateStr, DATE_FORMAT);

                    if (latest == null || current.isAfter(latestDateTime)) {
                        latest = obj;
                        latestDateTime = current;
                    }
                } catch (Exception ignore) {}
            }
            return latest;
        });
    }


    public static JsonObject getByNameAndDate(TradeCommodity commodity, String date) {
        String name = commodity.getCommodityName();
        String JSON_FILE_PATH = String.valueOf(Main.getInstance().getDataFolder()) + "/data/" + name + ".json";

        try (FileInputStream fis = new FileInputStream(JSON_FILE_PATH);
            JsonReader reader = Json.createReader(fis)) {
            data = reader.readArray();
        } catch (IOException e) {
            System.err.println("Error loading JSON file: " + JSON_FILE_PATH + " - " + e.getMessage());
            data = Json.createArrayBuilder().build();
            return null; // Return null if file loading fails
        }

        for (JsonValue value : data) {
            // Check if the value is an object before casting
            if (!(value instanceof JsonObject)) {
                continue;
            }

            JsonObject obj = value.asJsonObject();
            // Use safe getString with default null to avoid NullPointerException if key is missing
            String itemName = obj.getString("name", null);
            String itemDate = obj.getString("date", null);

            if (name.equals(itemName) && date.equals(itemDate)) {
                return obj;
            }
        }

        return null;
    }

    public static CompletableFuture<Double> getPrice(TradeCommodity commodity) {
        return getLatestByName(commodity).thenApply(latest -> {

            if (latest == null) {
                System.err.println("No current data for commodity: " + commodity.getCommodityName());
                return 0.0;
            }

            String priceString = latest.getString("value", null);
            if (priceString == null) return 0.0;

            try {
                return Double.parseDouble(priceString.replace(',', '.'));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        });
    }

    public static CompletableFuture<String> getUnit(TradeCommodity commodity) {
        return getLatestByName(commodity).thenApply(latestData -> {
            if (latestData == null) {
                return "";
            }

            String currencyString = latestData.getString("currency", null);

            if (currencyString == null) {
                return "";
            }

            return currencyString;
        });
    }

}