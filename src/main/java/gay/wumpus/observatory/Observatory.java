package gay.wumpus.observatory;

import gay.wumpus.observatory.messages.MessageFile;
import gay.wumpus.observatory.messages.JoinQuitMessage;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;


public final class Observatory extends JavaPlugin {
    private MessageFile messageFile;
    private JoinQuitMessage joinQuit;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        this.messageFile = new MessageFile(this);
        this.joinQuit = new JoinQuitMessage(this);
        this.getServer().getPluginManager().registerEvents(this.joinQuit, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean reloadMessages() {
        if (!this.messageFile.reload()) {
            return false;
        }
        return this.joinQuit.rebuild();
    }

    public boolean safeReloadConfig() {
        YamlConfiguration fresh = new YamlConfiguration();
        try {
            fresh.load(new File(getDataFolder(), "config.yml"));
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().warning("config.yml failed to load, keeping previous version" + e);
            return false;
        }
        reloadConfig();
        return joinQuit.rebuild();
    }

    public boolean reloadAll() {
        if (!this.reloadMessages()) return false;
        return this.safeReloadConfig();
    }
}
