package org.jufyer.plugin.stock.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jufyer.plugin.stock.Main;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class BlockPlacer {

    public static void placeBlocksFromFile(Player player) {
        try (InputStream inputStream = Main.getInstance().getResource("blockDatas.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            int blocksPlaced = 0;
            int errors = 0;

            Location playerLocation = player.getLocation();

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    String coordPart = line.substring(line.indexOf("Coordinates:") + 12, line.indexOf("| Type:"));
                    String[] coords = coordPart.split(",");
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());
                    int z = Integer.parseInt(coords[2].trim());

                    String typePart = line.substring(line.indexOf("Type:") + 5, line.indexOf(", Rotation:"));
                    Material blockType = Material.valueOf(typePart.trim());

                    String rotationPart = line.substring(line.indexOf("Rotation:") + 9).trim();

                    Location blockLocation = playerLocation.clone().add(x, y, z);

                    Block block = blockLocation.getBlock();
                    block.setType(blockType);

                    applyRotation(block, rotationPart);

                    blocksPlaced++;

                } catch (Exception e) {
                    errors++;
                    Main.getInstance().getLogger().warning("Error in line: " + line);
                    Main.getInstance().getLogger().warning("Error: " + e.getMessage());
                }
            }

            //player.sendMessage("§a" + blocksPlaced + " blocks were placed!" + (errors > 0 ? " §c(" + errors + " Error)" : ""));

        } catch (Exception e) {
            player.sendMessage("§cError reading block-datas: " + e.getMessage());
            Main.getInstance().getLogger().severe("Error reading blockDatas.txt: " + e.getMessage());
        }
    }

    private static void applyRotation(Block block, String rotation) {
        if (rotation.equals("NONE")) {
            return;
        }

        BlockData blockData = block.getBlockData();

        try {
            String[] rotationParts = rotation.split("_");
            String primaryRotation = rotationParts[0];
            String secondaryRotation = rotationParts.length > 1 ? rotationParts[1] : null;

            if (blockData instanceof Directional) {
                Directional directional = (Directional) blockData;
                BlockFace face = parseBlockFace(primaryRotation);
                if (face != null) {
                    directional.setFacing(face);
                }
            }

            if (blockData instanceof Rotatable) {
                Rotatable rotatable = (Rotatable) blockData;
                org.bukkit.block.BlockFace rotation_face = parseRotation(primaryRotation);
                if (rotation_face != null) {
                    rotatable.setRotation(rotation_face);
                }
            }

            if (blockData instanceof Bisected && secondaryRotation != null) {
                Bisected bisected = (Bisected) blockData;
                if (secondaryRotation.equals("TOP")) {
                    bisected.setHalf(Bisected.Half.TOP);
                } else if (secondaryRotation.equals("BOTTOM")) {
                    bisected.setHalf(Bisected.Half.BOTTOM);
                }
            }

            if (blockData instanceof Slab) {
                Slab slab = (Slab) blockData;
                String slabType = secondaryRotation != null ? secondaryRotation : primaryRotation;
                if (slabType.equals("BOTTOM")) {
                    slab.setType(Slab.Type.BOTTOM);
                } else if (slabType.equals("TOP")) {
                    slab.setType(Slab.Type.TOP);
                } else if (slabType.equals("DOUBLE")) {
                    slab.setType(Slab.Type.DOUBLE);
                }
            }

            block.setBlockData(blockData);

        } catch (Exception e) {
            Main.getInstance().getLogger().warning("Error while setting Rotation '" + rotation + "': " + e.getMessage());
        }
    }

    private static BlockFace parseBlockFace(String rotation) {
        switch (rotation) {
            case "NORTH": return BlockFace.NORTH;
            case "SOUTH": return BlockFace.SOUTH;
            case "EAST": return BlockFace.EAST;
            case "WEST": return BlockFace.WEST;
            case "UP": return BlockFace.UP;
            case "DOWN": return BlockFace.DOWN;
            default: return null;
        }
    }

    private static BlockFace parseRotation(String rotation) {
        switch (rotation) {
            case "NORTH": return BlockFace.NORTH;
            case "NORTH_NORTH_EAST": return BlockFace.NORTH_NORTH_EAST;
            case "NORTH_EAST": return BlockFace.NORTH_EAST;
            case "EAST_NORTH_EAST": return BlockFace.EAST_NORTH_EAST;
            case "EAST": return BlockFace.EAST;
            case "EAST_SOUTH_EAST": return BlockFace.EAST_SOUTH_EAST;
            case "SOUTH_EAST": return BlockFace.SOUTH_EAST;
            case "SOUTH_SOUTH_EAST": return BlockFace.SOUTH_SOUTH_EAST;
            case "SOUTH": return BlockFace.SOUTH;
            case "SOUTH_SOUTH_WEST": return BlockFace.SOUTH_SOUTH_WEST;
            case "SOUTH_WEST": return BlockFace.SOUTH_WEST;
            case "WEST_SOUTH_WEST": return BlockFace.WEST_SOUTH_WEST;
            case "WEST": return BlockFace.WEST;
            case "WEST_NORTH_WEST": return BlockFace.WEST_NORTH_WEST;
            case "NORTH_WEST": return BlockFace.NORTH_WEST;
            case "NORTH_NORTH_WEST": return BlockFace.NORTH_NORTH_WEST;
            default: return null;
        }
    }
}