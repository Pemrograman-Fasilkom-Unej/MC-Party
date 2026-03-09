package com.darkun7.party.api;

import com.darkun7.party.PartyPlugin;
import com.darkun7.party.models.Party;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyAPI {

    /**
     * Checks if a player is currently in a party.
     * 
     * @param uuid The player's UUID.
     * @return True if in a party, false otherwise.
     */
    public static boolean isInParty(UUID uuid) {
        if (PartyPlugin.getInstance() == null)
            return false;
        return PartyPlugin.getInstance().getPartyManager().isInParty(uuid);
    }

    /**
     * Gets the party prefix for a player.
     * 
     * @param uuid The player's UUID.
     * @return The 3-4 character prefix, or null if they are not in a party.
     */
    public static String getPartyPrefix(UUID uuid) {
        if (PartyPlugin.getInstance() == null)
            return null;
        return PartyPlugin.getInstance().getPartyManager().getPartyPrefix(uuid);
    }

    /**
     * Checks if two players are in the identical party.
     * 
     * @param uuid1 First player UUID
     * @param uuid2 Second player UUID
     * @return True if they share the same party, false otherwise.
     */
    public static boolean inSameParty(UUID uuid1, UUID uuid2) {
        if (PartyPlugin.getInstance() == null)
            return false;
        Party p1 = PartyPlugin.getInstance().getPartyManager().getParty(uuid1);
        Party p2 = PartyPlugin.getInstance().getPartyManager().getParty(uuid2);
        return p1 != null && p1.equals(p2);
    }
}
