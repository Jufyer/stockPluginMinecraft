package org.jufyer.plugin.stock.getPrice;

import org.bukkit.Material;

public enum TradeCommodity {

    GOLD(Material.GOLD_INGOT, "gold"),
    IRON_ORE(Material.IRON_ORE, "iron-ore"),
    COPPER(Material.COPPER_INGOT, "copper"),
    RHODIUM(Material.DIAMOND, "rhodium"),
    PLATINUM(Material.NETHERITE_INGOT, "platinum"),
    INDIUM(Material.EMERALD, "indium"),
    COBALT(Material.LAPIS_LAZULI, "cobalt"),
    SILICON(Material.AMETHYST_SHARD, "silicon"),
    COAL(Material.COAL, "coal"),
    NATURAL_GAS(Material.BLAZE_POWDER, "natural-gas"),
    CRUDE_OIL(Material.LAVA_BUCKET, "crude-oil"),
    URANIUM(Material.GLOWSTONE_DUST, "uranium"),
    WHEAT(Material.WHEAT, "wheat"),
    CORN(Material.MELON_SLICE, "corn"),
    COFFEE(Material.COCOA_BEANS, "coffee"),
    SUGAR(Material.SUGAR, "sugar"),
    COTTON(Material.STRING, "cotton"),
    PALM_OIL(Material.BAMBOO, "palm-oil"),
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
}
