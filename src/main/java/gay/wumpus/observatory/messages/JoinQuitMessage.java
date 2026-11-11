package gay.wumpus.observatory.messages;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class JoinQuitMessage implements Listener {
    private final Plugin plugin;
    private final Logger logger;
    private volatile Snapshot snapshot = Snapshot.EMPTY;
    private final Set<String> registered = new HashSet<>();
    private enum EventKind { JOIN, QUIT }

    public JoinQuitMessage(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.rebuild();
    }

    private record MessageEntry(String name, String permission, String join, String quit) {
        public String template(EventKind kind) {
            return switch(kind) {
                case JOIN -> join;
                case QUIT -> quit;
            };
        }
    }

    private record Snapshot(List<MessageEntry> entries, MessageEntry defaultEntry) {
        static final Snapshot EMPTY = new Snapshot(List.of(), null);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {

    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {

    }

    public boolean rebuild() {
        FileConfiguration config = this.plugin.getConfig();
        ConfigurationSection configSection = config.getConfigurationSection("features.join-quit-messages");
        if (configSection == null) {
            this.logger.warning("Custom join/quit messages are not found.");
            return false;
        }
        if (!configSection.getBoolean("enabled")) {
            this.snapshot = Snapshot.EMPTY;
            // unregister perms
            return true;
        }
        if (!configSection.getBoolean("enabled")) {
            return true;
        }
        ConfigurationSection entries = configSection.getConfigurationSection("entries");
        if (entries == null) {
            this.logger.warning("Custom join/quit messages are enabled but no entries are present.");
            return false;
        }
        Set<String> keys = entries.getKeys(false);
        List<MessageEntry> messageEntries = new ArrayList<>();
        MessageEntry defaultEntry = null;
        Set<String> permissions = new HashSet<>();
        for (String key : keys) {
            ConfigurationSection entry = entries.getConfigurationSection(key);
            if (entry == null) {
                this.logger.warning("[join-quit] Required attributes missing from entry: " + key);
                continue;
            }
            String permission = entry.getString("permission");
            String join = entry.getString("join");
            String quit = entry.getString("quit");

            if (join == null) {
                join = "";
            }
            if (quit == null) {
                quit = "";
            }

            if (key.equals("default")) {
                defaultEntry = new MessageEntry(key, null, join, quit);
                continue;
            }

            if (permission == null) {
                this.logger.warning("[join-quit] missing permission for: '" + key + "', only the default group may not have a permission!");
                continue;
            }
            permissions.add(permission);
            messageEntries.add(new MessageEntry(key, permission, join, quit));
        }
        this.snapshot = new Snapshot(List.copyOf(messageEntries), defaultEntry);



        return true;
    }
}



