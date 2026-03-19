package com.darkun7.party.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatModeManager {
    private final Set<UUID> partyChatModePlayers = new HashSet<>();

    public void togglePartyChatMode(UUID uuid) {
        if (partyChatModePlayers.contains(uuid)) {
            partyChatModePlayers.remove(uuid);
        } else {
            partyChatModePlayers.add(uuid);
        }
    }

    public boolean isInPartyChatMode(UUID uuid) {
        return partyChatModePlayers.contains(uuid);
    }

    public void disablePartyChatMode(UUID uuid) {
        partyChatModePlayers.remove(uuid);
    }
}
