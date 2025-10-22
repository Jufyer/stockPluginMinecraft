package org.jufyer.plugin.stock.getPrice;

import org.jufyer.plugin.stock.Main;

import javax.json.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FetchFromDataFolder {

    private static final String JSON_FILE_PATH = String.valueOf(Main.getInstance().getDataFolder()) + "/data/wheat.json";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static JsonArray data;

    // Daten einmalig laden
    static {
        try {
            FileInputStream fis = new FileInputStream(JSON_FILE_PATH);
            JsonReader reader = Json.createReader(fis);
            data = reader.readArray();
            reader.close();
            fis.close();
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der JSON-Datei: " + e.getMessage());
            data = Json.createArrayBuilder().build();
        }
    }

    // Neuestes Element nach Name finden
    public static JsonObject getLatestByName(String name) {
        JsonObject latest = null;
        LocalDate latestDate = null;

        for (JsonValue value : data) {
            JsonObject obj = value.asJsonObject();
            String itemName = obj.getString("name");

            if (itemName.equals(name)) {
                String dateStr = obj.getString("date");
                LocalDate currentDate = LocalDate.parse(dateStr, DATE_FORMAT);

                if (latest == null || currentDate.isAfter(latestDate)) {
                    latest = obj;
                    latestDate = currentDate;
                }
            }
        }

        return latest;
    }

    // Element nach Name und Datum finden
    public static JsonObject getByNameAndDate(String name, String date) {
        for (JsonValue value : data) {
            JsonObject obj = value.asJsonObject();

            if (obj.getString("name").equals(name) &&
                    obj.getString("date").equals(date)) {
                return obj;
            }
        }

        return null;
    }
}