package pl.ibcgames.mclvotifier;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import pl.ibcgames.mclvotifier.Modules.Configuration;

public final class Votifier extends JavaPlugin {

    private static Logger log = Bukkit.getLogger();
    private static Votifier plugin;
    private static Configuration Config;

    @Override
    public void onEnable() {

        plugin = this;

        Config = (new Configuration(this));
        this.saveDefaultConfig();

        String token = getPlugin().getConfiguration().get().getString("server_id");
        if (token == null || token.equalsIgnoreCase("paste_server_id_here")) {

            this.warning("No server id found in MCL-Votifier config");
            this.warning("How to use this plugin? See tutorial at:");
            this.warning("https://minecraft-servers.gg/mcl-votifier-plugin");

        }

        this.getCommand("mcl-vote").setExecutor(new Vote());
        this.getCommand("mcl-reward").setExecutor(new Reward());
        this.getCommand("mcl-test").setExecutor(new Test());

    }

    @Override
    public void onDisable() {

        this.warning("MCL-Votifier-OG shut down successfully.");

    }

    public Configuration getConfiguration() {

        return Config;

    }

    public void log(String log) {

        getLog().info("[MCL-Votifier-OG] " + log);

    }

    public void warning(String log) {

        getLog().warning("[MCL-Votifier-OG] " + log);

    }

    public static Votifier getPlugin() {

        return plugin;

    }

    public static Logger getLog() {

        return log;

    }

}