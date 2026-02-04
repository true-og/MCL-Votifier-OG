package pl.ibcgames.mclvotifier;

import java.util.Date;
import java.util.Objects;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Vote implements CommandExecutor {

    private final Object cacheLock = new Object();
    private JSONArray messages;
    private String url;
    private Date lastUpdate = new Date();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Votifier plugin = Votifier.getPlugin();
        if (plugin == null) {

            Votifier.getLog().warning("[MCL-Votifier-OG] Plugin not initialized; cannot execute command.");
            return true;

        }

        String token = plugin.getConfiguration().get().getString("server_id");
        if (token == null || token.equalsIgnoreCase("paste_server_id_here")) {

            sender.sendMessage(Utils.message("&cNo server id found in MCL-Votifier config"));
            sender.sendMessage(Utils.message("&cHow to use this plugin? See tutorial at:"));
            sender.sendMessage(Utils.message("&ahttps://minecraft-servers.gg/mcl-votifier-plugin"));

            return true;

        }

        Runnable runnable = () -> {

            try {

                boolean refresh;
                synchronized (cacheLock) {

                    long diff = new Date().getTime() - lastUpdate.getTime();
                    long diffMinutes = diff / (60 * 1000) % 60;
                    refresh = url == null || diffMinutes >= 60F;
                    if (refresh) {

                        lastUpdate = new Date();

                    }

                }

                if (refresh) {

                    plugin.getServer().getScheduler().runTask(plugin,
                            () -> sender.sendMessage(Utils.message("&aRefreshing data...")));

                    JSONObject res = Utils
                            .sendRequest("https://minecraft-servers.gg/api/server-by-key/" + token + "/get-vote");

                    Object responseUrl = res.get("vote_url");
                    Object responseText = res.get("text");
                    if (responseUrl == null || responseText == null) {

                        throw new IllegalStateException(
                                "Invalid response from minecraft-servers.gg. Expected vote_url and text.");

                    }

                    synchronized (cacheLock) {

                        url = responseUrl.toString();
                        messages = (JSONArray) responseText;

                    }

                }

                JSONArray cachedMessages;
                String cachedUrl;
                synchronized (cacheLock) {

                    cachedMessages = messages;
                    cachedUrl = url;

                }

                if (cachedMessages == null || cachedUrl == null) {

                    throw new IllegalStateException("Voting data is unavailable after refresh.");

                }

                JSONArray finalMessages = cachedMessages;
                String finalUrl = cachedUrl;
                plugin.getServer().getScheduler().runTask(plugin, () -> {

                    finalMessages.forEach((message) -> sender.sendMessage(Utils.message(message.toString())));
                    sender.sendMessage(Utils.message(finalUrl));

                });

            } catch (Exception error) {

                error.printStackTrace();

                plugin.warning("Failed to process /mcl-vote command: " + Objects.toString(error.getMessage()));

                plugin.getServer().getScheduler().runTask(plugin,
                        () -> sender.sendMessage(Utils.message("&cUnable to fetch data, please try again later.")));

            }

        };

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);

        return true;

    }

}
