package org.jufyer.plugin.stock.getPrice;

import org.bukkit.Material;

public enum TradeCommodity {

    // --- Metalle & Minerale ---
    GOLD(Material.GOLD_INGOT, "gold"),
    IRON_ORE(Material.IRON_INGOT, "iron-ore"),
    COPPER(Material.COPPER_INGOT, "copper"),

    RHODIUM(Material.NETHER_STAR, "rhodium"),          // extrem teuer
    PLATINUM(Material.DIAMOND, "platinum"),            // teuer
    INDIUM(Material.EMERALD, "indium"),                // wertvoll
    COBALT(Material.LAPIS_LAZULI, "cobalt"),
    SILICON(Material.QUARTZ, "silicon"),               // logischer als Amethyst

    URANIUM(Material.NETHERITE_INGOT, "uranium"),      // sehr wertvoll/gefährlich

    // --- Energie-Rohstoffe ---
    NATURAL_GAS(Material.FIRE_CHARGE, "natural-gas"),
    CRUDE_OIL(Material.BLACK_DYE, "crude-oil"),        // Öl = schwarz
    COAL(Material.COAL, "coal"),

    // --- Landwirtschaft ---
    WHEAT(Material.WHEAT, "wheat"),
    CORN(Material.HAY_BLOCK, "corn"),                  // Melon war unpassend
    COFFEE(Material.COCOA_BEANS, "coffee"),
    SUGAR(Material.SUGAR, "sugar"),
    COTTON(Material.WHITE_WOOL, "cotton"),             // besser als String
    PALM_OIL(Material.SLIME_BALL, "palm-oil"),         // ölig/fettig
    ORANGE_JUICE(Material.HONEY_BOTTLE, "orange-juice"),
    LIVE_CATTLE(Material.LEATHER, "live-cattle"),
    MILK(Material.MILK_BUCKET, "milk"),
    SULFUR(Material.GUNPOWDER, "sulfur");

    private final Material material;
    private final String commodityName;

    TradeCommodity(Material material, String commodityName) {
        this.material = material;
        this.commodityName = commodityName;
    }

    public Material getMaterial() {
        return material;
    }

    public String getCommodityName() {
        return commodityName;
    }

    public static TradeCommodity fromMaterial(Material material) {
        for (TradeCommodity c : values()) {
            if (c.material == material) {
                return c;
            }
        }
        return null;
    }

    public static TradeCommodity fromCommodityName(String name) {
        for (TradeCommodity c : values()) {
            if (c.commodityName.equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

}
