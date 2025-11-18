package org.jufyer.plugin.stock.gui;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jufyer.plugin.stock.Main;
import org.jufyer.plugin.stock.util.BlockPlacer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldManager implements Listener {

    public static final Map<java.util.UUID, Location> activeTraders = new HashMap<>();

    private static int playerCounter = 0;
    private static final int SLOT_DISTANCE = 50;
    public static final NamespacedKey VILLAGER_TRADING_KEY = new NamespacedKey(Main.getInstance(), "VILLAGER_TRADING");

    private static final List<String> STOCK_NAMES = Arrays.asList(
            "gold", "iron-ore", "copper", "rhodium", "platinum", "indium",
            "cobalt", "silicon", "coal", "natural-gas", "crude-oil", "uranium",
            "wheat", "corn", "coffee", "sugar", "cotton", "palm-oil",
            "orange-juice", "live-cattle", "milk", "sulfur"
    );

    public static void setupWorld(Player player, String stock) {
        World tradeWorld = Bukkit.getWorld("trade_world");
        if (tradeWorld == null) {
            WorldCreator wc = new WorldCreator("trade_world");

            wc.environment(World.Environment.NORMAL);
            wc.type(WorldType.FLAT);
            wc.generatorSettings("{\"layers\": [{\"block\": \"air\", \"height\": 1}, {\"block\": \"air\", \"height\": 1}], \"biome\":\"the_void\"}");
            wc.generateStructures(false);

            tradeWorld = wc.createWorld();

            tradeWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            tradeWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            tradeWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
            tradeWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            tradeWorld.setGameRule(GameRule.FALL_DAMAGE, false);
            tradeWorld.setGameRule(GameRule.FIRE_DAMAGE, false);
            tradeWorld.setGameRule(GameRule.TNT_EXPLODES, false);
            tradeWorld.setPVP(false);
            tradeWorld.setTime(1000);
        }

        int index = getPlayerTradeIndex(player);
        Location base = new Location(tradeWorld, index * SLOT_DISTANCE, 100, 0);
        player.teleport(base);
        //player.setRotation(0,0);

        setupTradingBooth(player, stock);

        activeTraders.put(player.getUniqueId(), base);
    }

    private static int getPlayerTradeIndex(Player player) {
        return activeTraders.size();
    }

    private static void setupTradingBooth(Player player, String stock) {

        BlockPlacer.placeBlocksFromFile(player);

        //player.sendMessage(String.valueOf(player.getLocation().getWorld()));

        new BukkitRunnable() {
            @Override
            public void run() {
                graphui.executeStuff(player, stock);

                return;
            }
        }.runTaskLater(Main.getInstance(), 10);

        //VILLAGER LOC: Coordinates: -5, -1, 5 | Type: DIAMOND_BLOCK, Rotation: NONE
        Location playerLocation = player.getLocation();
        Location villagerLocation = player.getLocation().clone().add(-4.5, 0, 5.5);
        Villager villager = (Villager) player.getWorld().spawnEntity(villagerLocation, EntityType.VILLAGER);
        villager.getPersistentDataContainer().set(VILLAGER_TRADING_KEY, PersistentDataType.BOOLEAN, true);

        villager.setRotation(-135, 0);

        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setCustomName("§cTrading Villager");
        villager.setCustomNameVisible(true);


    }

    public static void endTrade(Player player) {
        Location base = activeTraders.remove(player.getUniqueId());
        if (base == null) return;

        for (Entity e : base.getNearbyEntities(10, 10, 10)) {
            if (e instanceof ItemFrame) {
                e.remove();
            } else if (e instanceof Villager) {
                e.remove();
            }
        }

        World mainWorld = Bukkit.getWorld("world");
        if (mainWorld != null) {
            player.teleport(mainWorld.getSpawnLocation());
        }
    }

    public static boolean isTrading(Player player) {
        return activeTraders.containsKey(player.getUniqueId());
    }


    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        if (entity.getPersistentDataContainer().has(VILLAGER_TRADING_KEY)) {
            player.openInventory(VillagerInvTradingWorld.VillagerInvTradingWorld);
        }
    }
}
