package org.jufyer.plugin.stock.gui;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jufyer.plugin.stock.Main;
import org.jufyer.plugin.stock.util.BlockPlacer;

import java.util.*;

public class WorldManager implements Listener {

    public static final Map<UUID, Location> activeTraders = new HashMap<>();
    private static final Map<UUID, Location> startLoc = new HashMap<>();
    private static final List<Location> occupiedLocations = new ArrayList<>();

    private static final int SLOT_DISTANCE = 50;
    public static final NamespacedKey VILLAGER_TRADING_KEY =
            new NamespacedKey(Main.getInstance(), "VILLAGER_TRADING");


    // ---------------------------
    //  WORLD SETUP
    // ---------------------------

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

        startLoc.put(player.getUniqueId(), player.getLocation());

        // ---------------------------
        //  SLOT-FINDUNG OHNE SCHLEIFE
        // ---------------------------
        int slotIndex = occupiedLocations.size();
        Location base = new Location(tradeWorld, slotIndex * SLOT_DISTANCE, 100, 0);

        player.teleport(base);

        setupTradingBooth(player, stock);

        activeTraders.put(player.getUniqueId(), base);
        occupiedLocations.add(base);
    }


    // ---------------------------
    //  TRADING BOOTH SETUP
    // ---------------------------

    private static void setupTradingBooth(Player player, String stock) {

        BlockPlacer.placeBlocksFromFile(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                StockGraph.displayStockGraph(player, stock);
            }
        }.runTaskLater(Main.getInstance(), 10);

        // Villager Position
        Location villagerLocation = player.getLocation().clone().add(-4.5, 0, 5.5);
        Villager villager = (Villager) player.getWorld().spawnEntity(villagerLocation, EntityType.VILLAGER);

        villager.getPersistentDataContainer().set(VILLAGER_TRADING_KEY, PersistentDataType.BOOLEAN, true);

        villager.setRotation(-135, 0);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setCustomName("§cTrading Villager");
        villager.setCustomNameVisible(true);
    }


    // ---------------------------
    //  TRADE BEENDEN
    // ---------------------------

    public static void endTrade(Player player) {
        Location base = activeTraders.remove(player.getUniqueId());
        if (base == null) return;

        // Entfernt Villager und Item Frames
        for (Entity e : base.getNearbyEntities(10, 10, 10)) {
            if (e instanceof ItemFrame || e instanceof Villager) {
                e.remove();
            }
        }

        // Entfernt alle Blöcke
        clearArea(base, 10);

        // Spieler zurück teleportieren
        Location returnLoc = startLoc.get(player.getUniqueId());
        player.teleport(new Location(returnLoc.getWorld(), returnLoc.getX(), returnLoc.getY(), returnLoc.getZ()));
        startLoc.remove(player.getUniqueId());

        // Slot "freigeben"
        occupiedLocations.remove(base);
    }


    // ---------------------------
    //  AREA CLEARING
    // ---------------------------

    private static void clearArea(Location loc, int radius) {
        World world = loc.getWorld();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = world.getBlockAt(
                            loc.getBlockX() + x,
                            loc.getBlockY() + y,
                            loc.getBlockZ() + z
                    );

                    if (block.getType() != Material.AIR) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }


    public static boolean isTrading(Player player) {
        return activeTraders.containsKey(player.getUniqueId());
    }


    // ---------------------------
    //  INTERACTION EVENT
    // ---------------------------

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (entity.getPersistentDataContainer().has(VILLAGER_TRADING_KEY)) {
            player.openInventory(VillagerInvTradingWorld.VillagerInvTradingWorld);
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockDestroyEvent event) {
        if (event.getBlock().getLocation().getWorld().equals(Bukkit.getWorld("trade_world"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.getBlock().getLocation().getWorld().equals(Bukkit.getWorld("trade_world"))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getLocation().getWorld().equals(Bukkit.getWorld("trade_world"))) {
            event.setCancelled(true);
        }
    }
}
