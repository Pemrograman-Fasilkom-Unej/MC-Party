package com.darkun7.party.managers;

import com.darkun7.party.PartyPlugin;
import com.darkun7.party.models.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PartyManager {
    private final PartyPlugin plugin;
    private final Map<UUID, Party> activeParties = new HashMap<>(); // Party ID -> Party
    private final Map<UUID, UUID> playerParties = new HashMap<>(); // Player UUID -> Party ID
    private final Map<UUID, UUID> pendingInvites = new HashMap<>(); // Invitee -> Party ID

    public PartyManager(PartyPlugin plugin) {
        this.plugin = plugin;
    }

    public Party getParty(UUID playerUuid) {
        UUID partyId = playerParties.get(playerUuid);
        if (partyId != null) {
            return activeParties.get(partyId);
        }
        return null;
    }

    public Party getPartyByPrefix(String prefix) {
        for (Party party : activeParties.values()) {
            if (party.getPrefix().equalsIgnoreCase(prefix)) {
                return party;
            }
        }
        return null;
    }

    public boolean isInParty(UUID playerUuid) {
        return playerParties.containsKey(playerUuid);
    }

    public String getPartyPrefix(UUID playerUuid) {
        Party p = getParty(playerUuid);
        return p != null ? p.getPrefix() : null;
    }

    public Party createParty(Player leader, String prefix) {
        // Validate prefix
        if (prefix.length() < 3 || prefix.length() > 4) {
            leader.sendMessage(ChatColor.RED + "Party prefix must be 3 or 4 characters long.");
            return null;
        }
        if (!prefix.matches("[a-zA-Z0-9]+")) {
            leader.sendMessage(ChatColor.RED + "Party prefix can only contain alphanumeric characters.");
            return null;
        }

        if (getPartyByPrefix(prefix) != null) {
            leader.sendMessage(ChatColor.RED + "A party with prefix '" + prefix + "' already exists.");
            return null;
        }

        if (isInParty(leader.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + "You are already in a party!");
            return null;
        }

        Party newParty = new Party(leader.getUniqueId(), prefix.toUpperCase());
        activeParties.put(newParty.getId(), newParty);
        playerParties.put(leader.getUniqueId(), newParty.getId());

        leader.sendMessage(ChatColor.GREEN + "Party " + ChatColor.AQUA + "[" + newParty.getPrefix() + "] "
                + ChatColor.GREEN + "created successfully!");
        return newParty;
    }

    public void invitePlayer(Player inviter, Player invitee) {
        Party party = getParty(inviter.getUniqueId());
        if (party == null) {
            inviter.sendMessage(ChatColor.RED + "You must be in a party to invite players.");
            return;
        }
        if (!party.getLeader().equals(inviter.getUniqueId())) {
            inviter.sendMessage(ChatColor.RED + "Only the party leader can invite players.");
            return;
        }
        if (isInParty(invitee.getUniqueId())) {
            inviter.sendMessage(ChatColor.RED + "That player is already in a party.");
            return;
        }

        pendingInvites.put(invitee.getUniqueId(), party.getId());
        inviter.sendMessage(ChatColor.GREEN + "Invited " + invitee.getName() + " to the party.");
        invitee.sendMessage(
                ChatColor.AQUA + inviter.getName() + ChatColor.YELLOW + " has invited you to join their party "
                        + ChatColor.AQUA + "[" + party.getPrefix() + "]" + ChatColor.YELLOW + ".");
        invitee.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.GREEN + "/party join " + party.getPrefix()
                + ChatColor.YELLOW + " to accept.");
    }

    public void joinParty(Player player, String prefix) {
        if (isInParty(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already in a party.");
            return;
        }

        Party party = getPartyByPrefix(prefix);
        if (party == null) {
            player.sendMessage(ChatColor.RED + "No party found with that prefix.");
            return;
        }

        UUID invitedPartyId = pendingInvites.get(player.getUniqueId());
        if (invitedPartyId == null || !invitedPartyId.equals(party.getId())) {
            player.sendMessage(ChatColor.RED + "You have not been invited to this party.");
            return;
        }

        // Accept invite
        pendingInvites.remove(player.getUniqueId());
        party.addMember(player.getUniqueId());
        playerParties.put(player.getUniqueId(), party.getId());

        broadcastToParty(party, ChatColor.GREEN + player.getName() + " has joined the party!");
    }

    public void leaveParty(Player player) {
        Party party = getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage(ChatColor.RED + "You are not in a party.");
            return;
        }

        party.removeMember(player.getUniqueId());
        playerParties.remove(player.getUniqueId());

        if (party.getLeader().equals(player.getUniqueId())) {
            // Disband if leader leaves, or pass lead (simple disband for now)
            broadcastToParty(party, ChatColor.RED + "The leader has left. The party has been disbanded.");
            for (UUID memberUuid : Set.copyOf(party.getMembers())) {
                playerParties.remove(memberUuid);
                Player m = Bukkit.getPlayer(memberUuid);
                if (m != null)
                    m.sendMessage(ChatColor.RED + "Your party was disbanded.");
            }
            activeParties.remove(party.getId());
        } else {
            broadcastToParty(party, ChatColor.YELLOW + player.getName() + " has left the party.");
            player.sendMessage(ChatColor.YELLOW + "You left the party.");
        }
    }

    // PC feature
    public void broadcastToParty(Party party, String message) {
        for (UUID memberUuid : party.getMembers()) {
            Player p = Bukkit.getPlayer(memberUuid);
            if (p != null) {
                p.sendMessage(ChatColor.BLUE + "@PARTY " + ChatColor.RESET + message);
            }
        }
    }

    public void saveParties() {
        File file = new File(plugin.getDataFolder(), "parties.yml");
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Party> entry : activeParties.entrySet()) {
            String path = "parties." + entry.getKey().toString();
            Party party = entry.getValue();
            config.set(path + ".prefix", party.getPrefix());
            config.set(path + ".leader", party.getLeader().toString());
            List<String> memberList = new ArrayList<>();
            for (UUID memberId : party.getMembers()) {
                memberList.add(memberId.toString());
            }
            config.set(path + ".members", memberList);
        }

        try {
            config.save(file);
            plugin.getLogger().info("Saved " + activeParties.size() + " parties.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save parties.yml!");
            e.printStackTrace();
        }
    }

    public void loadParties() {
        File file = new File(plugin.getDataFolder(), "parties.yml");
        if (!file.exists())
            return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("parties"))
            return;

        activeParties.clear();
        playerParties.clear();

        for (String key : config.getConfigurationSection("parties").getKeys(false)) {
            try {
                UUID partyId = UUID.fromString(key);
                String prefix = config.getString("parties." + key + ".prefix");
                UUID leader = UUID.fromString(config.getString("parties." + key + ".leader"));
                List<String> memberStrings = config.getStringList("parties." + key + ".members");

                Party party = new Party(leader, prefix);
                // The constructor adds the initiator, but we'll clear and add all saved members
                party.getMembers().clear();
                for (String mStr : memberStrings) {
                    UUID memberId = UUID.fromString(mStr);
                    party.addMember(memberId);
                    playerParties.put(memberId, partyId);
                }

                // Set the ID manually since random ID is generated in constructor
                java.lang.reflect.Field idField = Party.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(party, partyId);

                activeParties.put(partyId, party);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load party: " + key);
                e.printStackTrace();
            }
        }
        plugin.getLogger().info("Loaded " + activeParties.size() + " parties.");
    }
}
