package org.jufyer.plugin.stock.getPrice;

import org.jufyer.plugin.stock.getPrice.TradeCommodity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FetchPrice {

  public static double getPrice(TradeCommodity commodity) {
    String symbol = commodity.getCommodityName();
    try {
      Document doc = Jsoup.connect("https://tradingeconomics.com/commodity/" + symbol)
              .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
              .timeout(1000)
              .get();

      Elements scripts = doc.select("script");

      for (Element script : scripts) {
        String scriptContent = script.html();

        if (scriptContent.contains("TEChartsMeta")) {
          int startIdx = scriptContent.indexOf("TEChartsMeta = [");
          if (startIdx != -1) {
            startIdx += "TEChartsMeta = ".length();
            int endIdx = scriptContent.indexOf("];", startIdx) + 1;

            String jsonStr = scriptContent.substring(startIdx, endIdx);

            JSONArray jsonArray = new JSONArray(jsonStr);
            JSONObject data = jsonArray.getJSONObject(0);

            return data.getDouble("value");
          }
        }
      }

    } catch (Exception e) {
      System.out.println("ERROR: " + e.getMessage());
      e.printStackTrace();
    }

    return 0;
  }

  public static String getUnit(TradeCommodity commodity) {
    String symbol = commodity.getCommodityName();
    try {
      Document doc = Jsoup.connect("https://tradingeconomics.com/commodity/" + symbol)
              .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
              .timeout(1000)
              .get();

      Element metaDesc = doc.selectFirst("meta[name=description]");
      if (metaDesc != null) {
        String content = metaDesc.attr("content");

        Pattern pattern = Pattern.compile("\\d{1,3}(?:[,\\.]\\d{3})*(?:\\.\\d+)?\\s+([A-Za-z/\\.]+)");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
          return matcher.group(1).trim();
        }
      }

    } catch (Exception e) {
      System.out.println("ERROR: " + e.getMessage());
      e.printStackTrace();
    }

    return "";
  }
}
