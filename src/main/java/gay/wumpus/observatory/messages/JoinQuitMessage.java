package gay.wumpus.observatory.messages;

import gay.wumpus.observatory.config.PermissionRegistry;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.logging.Logger;

public class JoinQuitMessage implements Listener {
    private final Plugin plugin;
    private final Logger logger;
    private volatile Snapshot snapshot = Snapshot.EMPTY;
    private final PermissionRegistry permissionRegistry;
    private enum EventKind { JOIN, QUIT }

    public JoinQuitMessage(Plugin plugin, PermissionRegistry permissionRegistry) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.permissionRegistry = permissionRegistry;
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        String template = this.findTemplateString(event.getPlayer(), EventKind.JOIN);
        event.joinMessage(this.renderMessage(template, event.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String template = this.findTemplateString(event.getPlayer(), EventKind.QUIT);
        event.quitMessage(this.renderMessage(template, event.getPlayer()));
    }

    private String findTemplateString(Player player, EventKind eventKind) {
        Snapshot snap = this.snapshot;
        for (MessageEntry entry : snap.entries()) {
            if (!player.hasPermission(entry.permission())) continue;
            if (entry.template(eventKind) == null) continue;
            return entry.template(eventKind);
        }
        if (snap.defaultEntry == null) {
            return "";
        } else {
            return snap.defaultEntry.template(eventKind);
        }
    }

    private Component renderMessage(String template, Player player) {
        return MiniMessage.miniMessage().deserialize(template, Placeholder.component("player", player.displayName()));
    }

    public boolean rebuild() {
        FileConfiguration config = this.plugin.getConfig();
        ConfigurationSection configSection = config.getConfigurationSection("features.join-quit-messages");
        if (configSection == null) {
            this.logger.warning("Custom join/quit messages are not found.");
            return false;
        }
        if (!configSection.getBoolean("enabled", false)) {
            this.snapshot = Snapshot.EMPTY;
            this.permissionRegistry.sync("join-quit", Set.of());
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
                this.logger.warning("[join-quit] Section: '" + key + "' is a static value, but should contain subattributes [permission] <join> <quit>.");
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
            permissions.add(permission.toLowerCase(Locale.ROOT));
            messageEntries.add(new MessageEntry(key, permission.toLowerCase(Locale.ROOT), join, quit));
        }
        this.permissionRegistry.sync("join-quit", permissions);
        this.snapshot = new Snapshot(List.copyOf(messageEntries), defaultEntry);

        return true;
    }
}



