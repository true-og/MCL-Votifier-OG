package pl.ibcgames.mclvotifier.Modules;

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import net.trueog.utilitiesog.UtilitiesOG;
import pl.ibcgames.mclvotifier.Votifier;

public class Configuration {

    private final Votifier plugin;

    public Configuration(Votifier plugin) {

        this.plugin = plugin;

        this.reload();

    }

    public void reload() {

        if (!new File(this.plugin.getDataFolder(), "config.yml").exists()) {

            UtilitiesOG.logToConsole(Votifier.getPrefix(),
                    "WARNING... config.yml not found. Using the default config file...");

            this.plugin.saveDefaultConfig();

        }

        this.plugin.reloadConfig();

    }

    public ConfigurationSection get(String s) {

        return this.plugin.getConfig().getConfigurationSection(s);

    }

    public FileConfiguration get() {

        return this.plugin.getConfig();

    }

}