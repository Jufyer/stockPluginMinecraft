package org.jufyer.plugin.stock.chatImplementation.commands;

import net.md_5.bungee.api.chat.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jufyer.plugin.stock.getPrice.FetchPrice;

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

                player.sendMessage("");
                player.sendMessage("§6§l<<---------------------------->>");
            } else if (args.length == 1) {
                try{
                    //JsonObject latest = FetchFromDataFolder.getLatestByName(String.valueOf(args[0]));
                    //double wheatPrice = Double.parseDouble(latest.getString("value"));

                    player.sendMessage("The price of " + args[0] + " at the moment is: "
                            + FetchPrice.getPrice(String.valueOf(args[0])) + " " + FetchPrice.getUnit(String.valueOf(args[0])));

                } catch (Exception e) {
                    player.sendMessage("The name you provided does not exists!");
                }
            }
        }else {
            commandSender.sendMessage("You need to be a player to send this command!");
        }
        return false;
    }
}
