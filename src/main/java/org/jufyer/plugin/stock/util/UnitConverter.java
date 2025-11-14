package org.jufyer.plugin.stock.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.json.JSONObject;
import org.jufyer.plugin.stock.Main;

public class UnitConverter {

    public enum OutputUnit { T, KG }

    // Wechselkurse von Frankfurter API
    private static Map<String, Double> currencyToUSD = fetchCurrencyRates();

    // Umrechnungsfaktoren auf Tonne
    private static final Map<String, Double> unitToTonne = new HashMap<>();
    static {
        unitToTonne.put("T", 1.0);
        unitToTonne.put("KG", 0.001);
        unitToTonne.put("LBS", 0.000453592);
        unitToTonne.put("BU", 0.0272155);
        unitToTonne.put("BBL", 0.136);
        unitToTonne.put("T.OZ", 0.0000311035);
        unitToTonne.put("CWT", 0.0453592);
        unitToTonne.put("MMBTU", 0.000026);
    }

    // Maps mögliche Schreibvarianten zu Standard-Unit
    private static final Map<String, String> unitAliases = new HashMap<>();
    static {
        unitAliases.put("USD/T", "T");
        unitAliases.put("USD/KG", "KG");
        unitAliases.put("USD/LBS", "LBS");
        unitAliases.put("USD/BU", "BU");
        unitAliases.put("USD/Bbl", "BBL");
        unitAliases.put("USD/t.oz", "T.OZ");
        unitAliases.put("USD/CWT", "CWT");
        unitAliases.put("USD/MMBtu", "MMBTU");
        unitAliases.put("USd/Lbs", "LBS");
        unitAliases.put("USd/Bu", "BU");
        unitAliases.put("CNY/Kg", "KG");
        unitAliases.put("MYR/T", "T");
        unitAliases.put("CNY/T", "T");
        unitAliases.put("USD/t", "T");
    }

    /**
     * Wandelt einen Preiswert in USD/Tonne oder USD/Kg um
     */
    public static double toUSD(double value, String unit, OutputUnit outputUnit) {
        // Einheit in Großbuchstaben
        unit = unit.trim().toUpperCase();

        // Währung und Maßeinheit trennen
        String[] parts = unit.split("/");
        if(parts.length != 2) throw new IllegalArgumentException("Invalid unit: " + unit);

        String currency = parts[0];
        String measure = parts[1];

        // Einheit normalisieren, z. B. USd/Bu → BU
        String stdMeasure = unitAliases.getOrDefault(currency + "/" + measure, measure);

        // Umrechnen in USD
        double valueInUSD = value;
        if(!currency.equals("USD")) {
            Double rate = currencyToUSD.get(currency);
            if(rate == null) throw new IllegalArgumentException("No exchange rate for currency: " + currency);
            valueInUSD = value / rate; // USD = value / rate
        }

        // Umrechnen auf Tonne
        Double factor = unitToTonne.get(stdMeasure);
        if(factor == null) throw new IllegalArgumentException("No conversion for unit: " + stdMeasure);

        double valueInTonne = valueInUSD / factor;

        if(outputUnit == OutputUnit.KG) return valueInTonne / 1000.0;
        return valueInTonne;
    }

    /**
     * Holt Wechselkurse von Frankfurter API
     */
    public static Map<String, Double> fetchCurrencyRates() {
        Map<String, Double> rates = new HashMap<>();
        try {
            URL url = new URL("https://api.frankfurter.dev/v1/latest?base=USD");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while((line = in.readLine()) != null) content.append(line);
            in.close();
            conn.disconnect();

            JSONObject json = new JSONObject(content.toString());
            if(json.has("rates")) {
                JSONObject jsonRates = json.getJSONObject("rates");
                for(String key : jsonRates.keySet()) rates.put(key.toUpperCase(), jsonRates.getDouble(key));
            } else {
                Main.getInstance().getLogger().info("API did not return rates, using defaults");
                rates.put("USD", 1.0);
                rates.put("CNY", 0.14);
                rates.put("MYR", 0.22);
            }
        } catch(Exception e) {
            e.printStackTrace();
            Main.getInstance().getLogger().info("Error fetching rates, using default values");
            rates.put("USD", 1.0);
            rates.put("CNY", 0.14);
            rates.put("MYR", 0.22);
        }
        return rates;
    }

//    public static void main(String[] args) {
//        String[] testUnits = {
//                "CNY/Kg", "USD/LBS", "MYR/T", "USd/Bu", "USd/Lbs", "USD/Bbl", "USD/t.oz"
//        };
//        System.out.println("=== USD per Tonne ===");
//        for(String u : testUnits) {
//            System.out.printf("%s → USD/T: %.2f%n", u, toUSD(100, u, OutputUnit.T));
//        }
//
//        System.out.println("\n=== USD per Kg ===");
//        for(String u : testUnits) {
//            System.out.printf("%s → USD/Kg: %.4f%n", u, toUSD(100, u, OutputUnit.KG));
//        }
//    }
}

