package org.jufyer.plugin.stock.getPrice;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;

public class FetchPrice {
  public static double wheat() {
    try {
      Document doc = Jsoup.connect("https://tradingeconomics.com/commodity/wheat")
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .timeout(10000)
        .get();

      Elements scripts = doc.select("script");

      for (Element script : scripts) {
        String scriptContent = script.html();

        if (scriptContent.contains("TEChartsMeta")) {
          // Extrahiere JSON (zwischen [ und ];)
          int startIdx = scriptContent.indexOf("TEChartsMeta = [");
          if (startIdx != -1) {
            startIdx += "TEChartsMeta = ".length();
            int endIdx = scriptContent.indexOf("];", startIdx) + 1;

            String jsonStr = scriptContent.substring(startIdx, endIdx);

            // Parse JSON
            JSONArray jsonArray = new JSONArray(jsonStr);
            JSONObject data = jsonArray.getJSONObject(0);

            double price = data.getDouble("value");
            String name = data.getString("name");

            return price;
          }
        }
      }

    } catch (Exception e) {
      System.out.println("ERROR: " + e.getMessage());
      e.printStackTrace();
    }

    return 0;
  }
}
