package pro.husk.fakeblock.objects;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pro.husk.configannotations.BukkitConfigHandler;
import pro.husk.configannotations.PostLoadAction;
import pro.husk.configannotations.Value;

/**
 * Language class, providing access to alter all user facing messages
 */
public class Language extends BukkitConfigHandler implements PostLoadAction {

    @Getter
    @Value("prefix")
    private String prefix;

    @Getter
    @Value("no-permission")
    private String noPermission;

    @Getter
    @Value("walls-reloaded")
    private String wallsReloaded;

    @Getter
    @Value("wall-deleted")
    private String wallDeleted;

    @Getter
    @Value("wall-created")
    private String wallCreated;

    @Getter
    @Value("incomplete-selection")
    private String incompleteSelection;

    @Getter
    @Value("walls-toggled")
    private String wallsToggled;

    @Getter
    @Value("cant-find-player")
    private String cantFindPlayer;

    public Language(JavaPlugin plugin, FileConfiguration configuration) {
        super(plugin, configuration);
        loadFromConfig();
    }

    @Override
    public PostLoadAction getPostLoadAction() {
        return this;
    }

    @Override
    public Object acceptAndReturn(Object object) {
        if (object instanceof String) {
            return colourise((String) object);
        }
        return object;
    }

    /**
     * Helper method to colourise a string with a combination of both hex and legacy chat colours
     *
     * @param input string to colourise
     * @return colourised string
     */
    private static String colourise(String input) {
        while (input.contains("#")) {
            int index = input.indexOf("#");
            if (index != 0 && input.charAt(index - 1) == '&') {
                String hexSubstring = input.substring(index - 1, index + 7).replaceAll("&", "");

                try {
                    net.md_5.bungee.api.ChatColor transformed = net.md_5.bungee.api.ChatColor.of(hexSubstring);
                    // Apply transformation to original string
                    input = input.replaceAll("&" + hexSubstring, transformed + "");
                } catch (IllegalArgumentException ignored) {

                }
            } else {
                break;
            }
        }

        // Apply legacy transformations at end
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', input);
    }
}
