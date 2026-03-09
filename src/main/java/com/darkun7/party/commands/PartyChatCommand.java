package com.darkun7.party.commands;

import com.darkun7.party.PartyPlugin;
import com.darkun7.party.models.Party;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyChatCommand implements CommandExecutor {
    private final PartyPlugin plugin;

    public PartyChatCommand(PartyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /pc <message>");
            return true;
        }

        Party p = plugin.getPartyManager().getParty(player.getUniqueId());
        if (p == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party. You cannot use party chat.");
            return true;
        }

        String message = String.join(" ", args);
        plugin.getPartyManager().broadcastToParty(p,
                ChatColor.WHITE + player.getName() + ChatColor.WHITE + "> " + message);
        return true;
    }
}
