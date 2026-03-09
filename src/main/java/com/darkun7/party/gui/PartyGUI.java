package com.darkun7.party.gui;

import com.darkun7.party.PartyPlugin;
import com.darkun7.party.models.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyGUI implements Listener {

    private final PartyPlugin plugin;
    private static final String GUI_TITLE = ChatColor.AQUA + "Party Tracker";

    public PartyGUI(PartyPlugin plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        Party party = plugin.getPartyManager().getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party.");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        int slot = 0;
        for (UUID memberId : party.getMembers()) {
            if (memberId.equals(player.getUniqueId()))
                continue; // Don't track self

            OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(member);
                meta.setDisplayName(ChatColor.YELLOW + "Track: " + ChatColor.WHITE + member.getName());

                List<String> lore = new ArrayList<>();
                if (member.isOnline()) {
                    boolean isAlreadyTracking = plugin.getTrackerManager().isTracking(player.getUniqueId())
                            && plugin.getTrackerManager().getTarget(player.getUniqueId()).equals(memberId);
                    if (isAlreadyTracking) {
                        lore.add(ChatColor.GREEN + "Currently Tracking!");
                    } else {
                        lore.add(ChatColor.GRAY + "Click to start tracking this member.");
                    }
                } else {
                    lore.add(ChatColor.RED + "Offline");
                }
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            inv.setItem(slot++, head);
        }

        // Stop tracking button
        ItemStack stopItem = new ItemStack(Material.BARRIER);
        ItemMeta stopMeta = stopItem.getItemMeta();
        if (stopMeta != null) {
            stopMeta.setDisplayName(ChatColor.RED + "Stop Tracking");
            stopItem.setItemMeta(stopMeta);
        }
        inv.setItem(26, stopItem); // Bottom right corner

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE))
            return;
        event.setCancelled(true); // Prevent picking up items

        if (!(event.getWhoClicked() instanceof Player player))
            return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        if (clicked.getType() == Material.BARRIER) {
            plugin.getTrackerManager().stopTracking(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "You stopped tracking party members.");
            player.closeInventory();
            return;
        }

        if (clicked.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            if (meta != null && meta.getOwningPlayer() != null) {
                OfflinePlayer target = meta.getOwningPlayer();
                if (!target.isOnline()) {
                    player.sendMessage(ChatColor.RED + "That player is offline.");
                    return;
                }

                plugin.getTrackerManager().startTracking(player.getUniqueId(), target.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Now tracking " + ChatColor.YELLOW + target.getName()
                        + ChatColor.GREEN + "!");
                player.closeInventory();
            }
        }
    }
}
