package com.darkun7.party.commands;

import com.darkun7.party.PartyPlugin;
import com.darkun7.party.models.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyCommand implements CommandExecutor {

    private final PartyPlugin plugin;

    public PartyCommand(PartyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party create <3-4 character tag>");
                    return true;
                }
                plugin.getPartyManager().createParty(player, args[1]);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party invite <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found online.");
                    return true;
                }
                plugin.getPartyManager().invitePlayer(player, target);
                break;
            case "join":
            case "accept":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party join <prefix>");
                    return true;
                }
                plugin.getPartyManager().joinParty(player, args[1]);
                break;
            case "gui":
                com.darkun7.party.gui.PartyGUI gui = plugin.getPartyGUI();
                if (gui != null) {
                    gui.openGUI(player);
                }
                break;
            case "leave":
                plugin.getPartyManager().leaveParty(player);
                break;
            case "list":
                Party p = plugin.getPartyManager().getParty(player.getUniqueId());
                if (p == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a party.");
                    return true;
                }
                player.sendMessage(ChatColor.AQUA + "--- Party [" + p.getPrefix() + "] ---");
                player.sendMessage(ChatColor.YELLOW + "Leader: " + ChatColor.WHITE
                        + Bukkit.getOfflinePlayer(p.getLeader()).getName());
                player.sendMessage(ChatColor.YELLOW + "Members:");
                for (UUID uuid : p.getMembers()) {
                    player.sendMessage(ChatColor.WHITE + "- " + Bukkit.getOfflinePlayer(uuid).getName());
                }
                break;
            default:
                sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.AQUA + "=== Party Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/party create <tag>" + ChatColor.WHITE + " - Create a new party");
        player.sendMessage(ChatColor.YELLOW + "/party invite <player>" + ChatColor.WHITE + " - Invite a player");
        player.sendMessage(
                ChatColor.YELLOW + "/party join <tag>" + ChatColor.WHITE + " - Accept an invite to join a party");
        player.sendMessage(ChatColor.YELLOW + "/party leave" + ChatColor.WHITE + " - Leave your current party");
        player.sendMessage(ChatColor.YELLOW + "/party list" + ChatColor.WHITE + " - View party members");
        player.sendMessage(ChatColor.YELLOW + "/party gui" + ChatColor.WHITE + " - Open Party Tracker Menu");
        player.sendMessage(ChatColor.YELLOW + "/pc <message>" + ChatColor.WHITE + " - Party chat");
    }
}
