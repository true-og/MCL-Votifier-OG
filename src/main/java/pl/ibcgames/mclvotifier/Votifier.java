package pl.ibcgames.mclvotifier;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.luckperms.api.LuckPerms;
import net.trueog.utilitiesog.UtilitiesOG;
import pl.ibcgames.mclvotifier.Modules.Configuration;
import pl.ibcgames.mclvotifier.Modules.LuckPermsPrefix;

public final class Votifier extends JavaPlugin {

    private static Votifier plugin;
    private static Configuration Config;
    private static LuckPerms luckPerms;

    @Override
    public void onEnable() {

        plugin = this;

        Config = (new Configuration(this));
        this.saveDefaultConfig();

        this.registerCommand("mcl-vote", new Vote());
        this.registerCommand("mcl-reward", new Reward());
        this.registerCommand("mcl-test", new Test());

        // Hook LuckPerms API.
        final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager()
                .getRegistration(LuckPerms.class);
        if (provider != null) {

            luckPerms = LuckPermsPrefix.hook();

        } else {

            getLogger().severe("LuckPerms not found – prefixes will be empty.");

        }

    }

    @Override
    public void onDisable() {

        UtilitiesOG.logToConsole(getPrefix(), "Shut down successfully.");

    }

    public Configuration getConfiguration() {

        return Config;

    }

    public static Votifier getPlugin() {

        return plugin;

    }

    public static String getPrefix() {

        return "&7[&aMCL-Votifier&f-&4OG&7]";

    }

    private void registerCommand(String name, CommandExecutor executor) {

        final PluginCommand command = this.getCommand(name);
        if (command == null) {

            UtilitiesOG.logToConsole(getPrefix(),
                    "ERROR: Command '" + name + "' is missing from plugin.yml. It will not be available.");
            return;

        }

        command.setExecutor(executor);

    }

    public static String getPlayerPrefix(Player p) {

        return LuckPermsPrefix.getPrefixLegacy(luckPerms, p);

    }

}