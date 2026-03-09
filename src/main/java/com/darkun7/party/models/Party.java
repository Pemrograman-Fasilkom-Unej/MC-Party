package com.darkun7.party.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {
    private final UUID id;
    private String prefix;
    private UUID leader;
    private final Set<UUID> members;

    public Party(UUID initiator, String prefix) {
        this.id = UUID.randomUUID();
        this.prefix = prefix;
        this.leader = initiator;
        this.members = new HashSet<>();
        this.members.add(initiator);
    }

    public UUID getId() {
        return id;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID uuid) {
        this.members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }
}
