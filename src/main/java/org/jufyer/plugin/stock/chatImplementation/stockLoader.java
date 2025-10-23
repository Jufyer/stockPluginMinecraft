package org.jufyer.plugin.stock.chatImplementation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

public class stockLoader {
    public static class PricePoint {
        String name;
        String date;
        String currency;
        public String value;
    }

    public static List<PricePoint> loadHistory(String symbol) throws Exception {
        String urlStr = "https://raw.githubusercontent.com/Jufyer/stocksPluginDatabase/refs/heads/main/" + symbol + ".json";
        URL url = new URL(urlStr);
        InputStreamReader reader = new InputStreamReader(url.openStream());

        Type listType = new TypeToken<List<PricePoint>>(){}.getType();
        List<PricePoint> prices = new Gson().fromJson(reader, listType);
        reader.close();

        return prices;
    }

    public static void main(String[] args) throws Exception {
        List<PricePoint> wheatPrices = loadHistory("wheat");
        for (PricePoint p : wheatPrices) {
            System.out.println(p.date + " -> " + p.value + " " + p.currency);
        }
    }
}
