package org.jufyer.plugin.stock.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import com.mojang.authlib.properties.Property;

import java.lang.reflect.Field;
import java.util.UUID;

public class CreateCustomHeads {
    public static ItemStack createCustomHead(String base64) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        meta.setPlayerProfile(
                Bukkit.createProfile(UUID.randomUUID(), "custom_head")
        );

        PlayerProfile profile = meta.getPlayerProfile();
        if (profile != null) {
            profile.setProperty(new ProfileProperty("textures", base64));
            meta.setPlayerProfile(profile);
        }

        head.setItemMeta(meta);
        return head;
    }
}
