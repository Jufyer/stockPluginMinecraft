package org.jufyer.plugin.stock.chatImplementation.commands;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jufyer.plugin.stock.chatImplementation.stockLoader;
import org.jufyer.plugin.stock.getPrice.FetchPrice;
import org.jufyer.plugin.stock.getPrice.TradeCommodity;
import org.jufyer.plugin.stock.util.PriceHistoryChart;

import java.util.List;
import java.util.stream.Collectors;

public class showPrices implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (args.length == 0) {

                player.sendMessage("§6§l<<------------Menu------------>>\n");

                TextComponent wheat = new TextComponent("§6§l   Wheat");
                wheat.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices wheat"));
                wheat.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the wheat menu").create()));
                player.sendMessage(wheat);

                // gold
                TextComponent gold = new TextComponent("§6§l   Gold");
                gold.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices gold"));
                gold.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the gold menu").create()));
                player.sendMessage(gold);

                // iron_ore
                TextComponent ironOre = new TextComponent("§6§l   Iron Ore");
                ironOre.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices iron_ore"));
                ironOre.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the iron ore menu").create()));
                player.sendMessage(ironOre);

                // copper
                TextComponent copper = new TextComponent("§6§l   Copper");
                copper.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices copper"));
                copper.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the copper menu").create()));
                player.sendMessage(copper);

                // rhodium
                TextComponent rhodium = new TextComponent("§6§l   Rhodium");
                rhodium.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices rhodium"));
                rhodium.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the rhodium menu").create()));
                player.sendMessage(rhodium);

                // platinum
                TextComponent platinum = new TextComponent("§6§l   Platinum");
                platinum.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices platinum"));
                platinum.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the platinum menu").create()));
                player.sendMessage(platinum);

                // indium
                TextComponent indium = new TextComponent("§6§l   Indium");
                indium.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices indium"));
                indium.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the indium menu").create()));
                player.sendMessage(indium);

                // cobalt
                TextComponent cobalt = new TextComponent("§6§l   Cobalt");
                cobalt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices cobalt"));
                cobalt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the cobalt menu").create()));
                player.sendMessage(cobalt);

                // silicon
                TextComponent silicon = new TextComponent("§6§l   Silicon");
                silicon.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices silicon"));
                silicon.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the silicon menu").create()));
                player.sendMessage(silicon);

                // coal
                TextComponent coal = new TextComponent("§6§l   Coal");
                coal.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices coal"));
                coal.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the coal menu").create()));
                player.sendMessage(coal);

                // natural-gas
                TextComponent naturalGas = new TextComponent("§6§l   Natural Gas");
                naturalGas.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices natural_gas"));
                naturalGas.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the natural gas menu").create()));
                player.sendMessage(naturalGas);

                // crude-oil
                TextComponent crudeOil = new TextComponent("§6§l   Crude Oil");
                crudeOil.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices crude_oil"));
                crudeOil.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the crude oil menu").create()));
                player.sendMessage(crudeOil);

                // uranium
                TextComponent uranium = new TextComponent("§6§l   Uranium");
                uranium.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices uranium"));
                uranium.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the uranium menu").create()));
                player.sendMessage(uranium);

                // corn
                TextComponent corn = new TextComponent("§6§l   Corn");
                corn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices corn"));
                corn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the corn menu").create()));
                player.sendMessage(corn);

                // coffee
                TextComponent coffee = new TextComponent("§6§l   Coffee");
                coffee.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices coffee"));
                coffee.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the coffee menu").create()));
                player.sendMessage(coffee);

                // sugar
                TextComponent sugar = new TextComponent("§6§l   Sugar");
                sugar.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices sugar"));
                sugar.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the sugar menu").create()));
                player.sendMessage(sugar);

                // cotton
                TextComponent cotton = new TextComponent("§6§l   Cotton");
                cotton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices cotton"));
                cotton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the cotton menu").create()));
                player.sendMessage(cotton);

                // palm-oil
                TextComponent palmOil = new TextComponent("§6§l   Palm Oil");
                palmOil.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices palm_oil"));
                palmOil.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the palm oil menu").create()));
                player.sendMessage(palmOil);

                // orange-juice
                TextComponent orangeJuice = new TextComponent("§6§l   Orange Juice");
                orangeJuice.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices orange_juice"));
                orangeJuice.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the orange juice menu").create()));
                player.sendMessage(orangeJuice);

                // live-cattle
                TextComponent liveCattle = new TextComponent("§6§l   Live Cattle");
                liveCattle.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices live_cattle"));
                liveCattle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the live cattle menu").create()));
                player.sendMessage(liveCattle);

                // milk
                TextComponent milk = new TextComponent("§6§l   Milk");
                milk.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices milk"));
                milk.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the milk menu").create()));
                player.sendMessage(milk);

                // sulfur
                TextComponent sulfur = new TextComponent("§6§l   Sulfur");
                sulfur.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "showprices sulfur"));
                sulfur.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to show the sulfur menu").create()));
                player.sendMessage(sulfur);
                player.sendMessage("§6§l<<---------------------------->>");
            }else if (args.length == 1) {
                if (args[0].toString().equalsIgnoreCase("list")) {
                    player.sendMessage("You can chose from the following symbols:\n" +
                            "gold\n" +
                            "iron_ore\n" +
                            "copper\n" +
                            "rhodium\n" +
                            "platinum\n" +
                            "indium\n" +
                            "cobalt\n" +
                            "silicon\n" +
                            "coal\n" +
                            "natural_gas\n" +
                            "crude_oil\n" +
                            "uranium\n" +
                            "wheat\n" +
                            "corn\n" +
                            "coffee\n" +
                            "sugar\n" +
                            "cotton\n" +
                            "palm_oil\n" +
                            "orange_juice\n" +
                            "live_cattle\n" +
                            "milk\n" +
                            "sulfur");
                }else {
                    try {
                        TradeCommodity commodity = TradeCommodity.valueOf(args[0].toUpperCase());
                        double price = FetchPrice.getPrice(commodity);
                        String unit = FetchPrice.getUnit(commodity);

                        player.sendMessage("The price of " + commodity.getCommodityName().replace("-", "_") + " at the moment is: " + price + " " + unit
                                + " and the Minecraft Item is: " + commodity.getMaterial());

                        PriceHistoryChart chart = new PriceHistoryChart();

                        // Einfache Version
                        chart.showChart(player, commodity, 20, PriceHistoryChart.ChartStyle.SPARKLINE);
                        chart.showChart(player, commodity, 20, PriceHistoryChart.ChartStyle.BAR);
                        chart.showChart(player, commodity, 20, PriceHistoryChart.ChartStyle.TREND);


                    } catch (IllegalArgumentException e) {
                        player.sendMessage("The name you provided does not exist!");
                    } catch (Exception e) {
                        player.sendMessage("An error occurred while fetching the price.");
                        e.printStackTrace();
                    }
                }
            }

        }else {
            commandSender.sendMessage("You need to be a player to send this command!");
        }
        return false;
    }

    public static List<Double> getHistory(TradeCommodity commodity, int n) throws Exception {
        List<stockLoader.PricePoint> points = stockLoader.loadHistory(commodity.getCommodityName());
        // Nur die letzten n Werte, in der richtigen Reihenfolge
        List<Double> values = points.stream()
                .map(p -> Double.parseDouble(p.value))
                .collect(Collectors.toList());
        int size = values.size();
        if (size <= n) return values;
        else return values.subList(size - n, size);
    }
}
