package com.darkun7.party;

import com.darkun7.party.managers.PartyManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PartyPlugin extends JavaPlugin {

    private static PartyPlugin instance;
    private PartyManager partyManager;
    private com.darkun7.party.managers.TrackerManager trackerManager;
    private com.darkun7.party.gui.PartyGUI partyGUI;

    @Override
    public void onEnable() {
        instance = this;
        this.partyManager = new PartyManager(this);
        this.trackerManager = new com.darkun7.party.managers.TrackerManager(this);
        this.partyGUI = new com.darkun7.party.gui.PartyGUI(this);

        this.partyManager.loadParties();

        getServer().getPluginManager().registerEvents(this.partyGUI, this);

        getCommand("party").setExecutor(new com.darkun7.party.commands.PartyCommand(this));
        getCommand("partychat").setExecutor(new com.darkun7.party.commands.PartyChatCommand(this));

        getLogger().info("Standalone Party system initialized.");
    }

    @Override
    public void onDisable() {
        if (this.partyManager != null) {
            this.partyManager.saveParties();
        }
    }

    public static PartyPlugin getInstance() {
        return instance;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public com.darkun7.party.managers.TrackerManager getTrackerManager() {
        return trackerManager;
    }

    public com.darkun7.party.gui.PartyGUI getPartyGUI() {
        return partyGUI;
    }
}
