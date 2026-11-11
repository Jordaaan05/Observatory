package gay.wumpus.observatory.config;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PermissionRegistry {

    private final PluginManager pluginManager;
    private final Map<String, Set<String>> owners = new HashMap<>();
    private final Set<String> declared = new HashSet<>();

    public PermissionRegistry(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public void sync(String feature, Set<String> wanted) {
        for (var it = owners.entrySet().iterator(); it.hasNext();) {
            var e = it.next();
            if (wanted.contains(e.getKey())) continue;
            if (e.getValue().remove(feature) && e.getValue().isEmpty()) {
                if (declared.remove(e.getKey())) {
                    pluginManager.removePermission(e.getKey());
                }
                it.remove();
            }
        }
        for (String node : wanted) {
            Set<String> set = owners.computeIfAbsent(node, n -> new HashSet<>());
            if (set.isEmpty() && pluginManager.getPermission(node) == null) {
                pluginManager.addPermission(new Permission(node, PermissionDefault.FALSE));
                declared.add(node);
            }
            set.add(feature);
        }
    }

    public void releaseAll(String feature) {
        sync(feature, Set.of());
    }
}
