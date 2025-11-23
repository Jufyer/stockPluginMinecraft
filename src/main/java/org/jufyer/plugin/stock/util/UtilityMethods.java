package org.jufyer.plugin.stock.util;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class UtilityMethods {
    public static CompletableFuture<JsonArray> loadJsonAsync(String path) {
        return CompletableFuture.supplyAsync(() -> {
            try (FileInputStream fis = new FileInputStream(path);
                 JsonReader reader = Json.createReader(fis)) {

                return reader.readArray();
            } catch (IOException e) {
                System.err.println("Error loading JSON file: " + path + " - " + e.getMessage());
                return Json.createArrayBuilder().build();
            }
        });
    }

    public static String capitalize(String text) {
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

    public static String decapitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        text = text.toLowerCase();
        text = text.replace(" ", "-");
        return text;
    }
}
