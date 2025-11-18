package org.jufyer.plugin.stock.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.json.JSONObject;
import org.jufyer.plugin.stock.Main;

public class UnitConverter {

    public enum OutputUnit { T, KG }

    private static Map<String, Double> currencyToUSD = new HashMap<>();

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
        unitToTonne.put("MMBTU", 0.000026); // symbolisch
    }

    // Maps mögliche Schreibvarianten zu Standard-Unit
    private static final Map<String, String> unitAliases = new HashMap<>();
    static {
        unitAliases.put("USD/T", "T");
        unitAliases.put("USD/KG", "KG");
        unitAliases.put("USD/LBS", "LBS");
        unitAliases.put("USD/BU", "BU");
        unitAliases.put("USD/BBL", "BBL");
        unitAliases.put("USD/T.OZ", "T.OZ");
        unitAliases.put("USD/CWT", "CWT");
        unitAliases.put("USD/MMBTU", "MMBTU");
        unitAliases.put("USd/LBS", "LBS");
        unitAliases.put("USd/BU", "BU");
        unitAliases.put("CNY/KG", "KG");
        unitAliases.put("MYR/T", "T");
        unitAliases.put("CNY/T", "T");
        unitAliases.put("USD/T", "T");
    }

    static {
        // initialer Kursabruf
        fetchCurrencyRates();

        // Timer für automatisches Update alle 30 Minuten
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchCurrencyRates();
            }
        }, 30 * 60 * 1000, 30 * 60 * 1000); // 30 min
    }

    /**
     * Wandelt einen Preiswert in USD/Tonne oder USD/Kg um
     */
    public static double toUSD(double value, String unit, OutputUnit outputUnit) {
        if(unit == null) throw new IllegalArgumentException("Unit cannot be null");
        unit = unit.trim().toUpperCase();

        // Währung und Maßeinheit trennen
        String[] parts = unit.split("/");
        if(parts.length != 2) throw new IllegalArgumentException("Invalid unit: " + unit);

        String currency = parts[0];
        String measure = parts[1];

        // Einheit normalisieren
        String stdMeasure = unitAliases.getOrDefault(currency + "/" + measure, measure);

        // Umrechnen in USD
        double valueInUSD = value;
        if(!currency.equals("USD")) {
            Double rate = currencyToUSD.get(currency);
            if(rate == null) {
                Main.getInstance().getLogger().warning("No exchange rate for currency " + currency + ", using USD as fallback");
            } else {
                valueInUSD = value / rate; // USD = value / rate
            }
        }

        // Umrechnen auf Tonne
        Double factor = unitToTonne.get(stdMeasure);
        if(factor == null) {
            Main.getInstance().getLogger().warning("Unknown unit " + stdMeasure + ", using Tonne as fallback");
            factor = 1.0;
        }

        double valueInTonne = valueInUSD / factor;
        return (outputUnit == OutputUnit.KG) ? valueInTonne / 1000.0 : valueInTonne;
    }

    /**
     * Holt Wechselkurse von Frankfurter API
     */
    public static void fetchCurrencyRates() {
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
            Map<String, Double> rates = new HashMap<>();
            if(json.has("rates")) {
                JSONObject jsonRates = json.getJSONObject("rates");
                for(String key : jsonRates.keySet()) rates.put(key.toUpperCase(), jsonRates.getDouble(key));
            }
            rates.put("USD", 1.0); // sicherheitshalber immer
            currencyToUSD = rates;

            Main.getInstance().getLogger().info("Currency rates updated successfully");

        } catch(Exception e) {
            e.printStackTrace();
            Main.getInstance().getLogger().warning("Error fetching rates, keeping old values");
        }
    }
}
