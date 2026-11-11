package gay.wumpus.observatory.messages;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class MessageFile {
    private YamlConfiguration messages;
    private File file;
    private Logger logger;

    public MessageFile(JavaPlugin plugin) {
        this.file = new File(plugin.getDataFolder(), "messages.yml");

        if (!(this.file.exists())) {
            plugin.saveResource("messages.yml", false);
        }
        this.logger = plugin.getLogger();
        this.reload();
        this.messages = YamlConfiguration.loadConfiguration(this.file);
    }

    public boolean reload() {
        YamlConfiguration fresh = new YamlConfiguration();
        try {
            fresh.load(this.file);
        } catch (IOException | InvalidConfigurationException e) {
            this.logger.warning("messages.yml failed to load, keeping previous version" + e);
            return false;
        }
        this.messages = fresh;
        return true;
    }

    public String getString(String path) {
        return messages.getString(path);
    }
}
