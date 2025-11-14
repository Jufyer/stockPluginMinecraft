package org.jufyer.plugin.stock.getPrice;

import org.jufyer.plugin.stock.Main;
import javax.json.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FetchFromDataFolder {

    // Format: "2025-10-22 19:06:57 UTC"
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");

    private static JsonArray data;

    public static JsonObject getLatestByName(TradeCommodity commodity) {
        String name = commodity.getCommodityName();
        // Construct the file path using the commodity name
        String JSON_FILE_PATH = String.valueOf(Main.getInstance().getDataFolder()) + "/data/" + name + ".json";

        try (FileInputStream fis = new FileInputStream(JSON_FILE_PATH);
             JsonReader reader = Json.createReader(fis)) {
            data = reader.readArray();
        } catch (IOException e) {
            System.err.println("Error loading JSON file: " + JSON_FILE_PATH + " - " + e.getMessage());
            data = Json.createArrayBuilder().build();
            return null;
        }

        JsonObject latest = null;
        LocalDateTime latestDateTime = null;

        for (JsonValue value : data) {
            // Check if the value is an object before casting
            if (!(value instanceof JsonObject)) {
                continue;
            }

            JsonObject obj = value.asJsonObject();
            String itemName = obj.getString("name", null); // Use safe getString with default
            String dateStr = obj.getString("date", null);

            if (name.equals(itemName) && dateStr != null) {
                try {
                    LocalDateTime currentDateTime = LocalDateTime.parse(dateStr, DATE_FORMAT);

                    if (latest == null || currentDateTime.isAfter(latestDateTime)) {
                        latest = obj;
                        latestDateTime = currentDateTime;
                    }
                } catch (Exception e) {
                    System.err.println("Invalid date format in: " + dateStr + " for " + name);
                }
            }
        }

        return latest;
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

    public static double getPrice(TradeCommodity commodity) {
        JsonObject latestData = getLatestByName(commodity);

        if (latestData == null) {
            System.err.println("No current data for the commodity: " + commodity.getCommodityName());
            return 0.0;
        }

        // The price is stored under the "value" key as a string
        String priceString = latestData.getString("value", null);

        if (priceString == null) {
            System.err.println("Value 'value' missing in the latest dataset for: " + commodity.getCommodityName());
            return 0.0;
        }

        try {
            // Parse the string value to a double. Replace comma with dot to ensure correct parsing.
            return Double.parseDouble(priceString.replace(',', '.'));
        } catch (NumberFormatException e) {
            System.err.println("Error parsing the price '" + priceString + "' for " + commodity.getCommodityName() + ": " + e.getMessage());
            return 0.0;
        }
    }

    public static String getUnit(TradeCommodity commodity) {
        JsonObject latestData = getLatestByName(commodity);

        if (latestData == null) {
            System.err.println("No current data for the commodity: " + commodity.getCommodityName());
            return "";
        }

        // The price is stored under the "currency" key as a string
        String currencyString = latestData.getString("currency", null);

        if (currencyString == null) {
            System.err.println("Value 'currency' missing in the latest dataset for: " + commodity.getCommodityName());
            return "";
        }

        try {
            return currencyString;
        } catch (NumberFormatException e) {
            System.err.println("Error parsing the price '" + currencyString + "' for " + commodity.getCommodityName() + ": " + e.getMessage());
            return "";
        }
    }
}