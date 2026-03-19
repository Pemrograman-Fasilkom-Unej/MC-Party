package com.darkun7.party.listeners;

import com.darkun7.party.PartyPlugin;
import com.darkun7.party.models.Party;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PartyChatListener implements Listener {
    private final PartyPlugin plugin;

    public PartyChatListener(PartyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!plugin.getChatModeManager().isInPartyChatMode(event.getPlayer().getUniqueId())) {
            return;
        }

        Party party = plugin.getPartyManager().getParty(event.getPlayer().getUniqueId());
        if (party == null) {
            plugin.getChatModeManager().disablePartyChatMode(event.getPlayer().getUniqueId());
            return;
        }

        event.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        plugin.getPartyManager().broadcastToParty(party, 
                ChatColor.WHITE + event.getPlayer().getName() + ChatColor.WHITE + "> " + message);
    }
}
