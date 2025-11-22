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
}
