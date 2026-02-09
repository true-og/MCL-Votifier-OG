package pl.ibcgames.mclvotifier;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.trueog.utilitiesog.UtilitiesOG;

public class Vote implements CommandExecutor {

    private static final long CACHE_TTL_MILLIS = 60L * 60L * 1000L;

    private final Object cacheLock = new Object();
    private JSONArray messages;
    private String url;
    private Date lastUpdate = new Date(0L);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        final Votifier plugin = Votifier.getPlugin();
        if (plugin == null) {

            UtilitiesOG.logToConsole(Votifier.getPrefix(), "ERROR: Plugin not initialized; cannot execute command.");

            return true;

        }

        if (Utils.tokenExists(sender)) {

            final CommandSender responder = sender;

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

                final String token = plugin.getConfiguration().get().getString("server_id");

                try {

                    final boolean refreshNeeded;
                    synchronized (cacheLock) {

                        final long ageMillis = System.currentTimeMillis() - lastUpdate.getTime();
                        refreshNeeded = url == null || messages == null || ageMillis >= CACHE_TTL_MILLIS;

                    }

                    if (refreshNeeded) {

                        final JSONObject res = Utils
                                .sendRequest("https://minecraft-servers.gg/api/server-by-key/" + token + "/get-vote");

                        final Object responseUrl = res.get("vote_url");
                        final Object responseText = res.get("text");
                        if (responseUrl == null || responseText == null) {

                            throw new IllegalStateException(
                                    "ERROR: Invalid response from minecraft-servers.gg. Expected vote_url and text.");

                        }

                        if (!(responseText instanceof JSONArray responseMessages)) {

                            throw new IllegalStateException(
                                    "ERROR Invalid response from minecraft-servers.gg. Expected 'text' to be a JSON array.");

                        }

                        synchronized (cacheLock) {

                            url = responseUrl.toString();
                            messages = responseMessages;
                            lastUpdate = new Date();

                        }

                    }

                    final JSONArray cachedMessages;
                    final String cachedUrl;
                    synchronized (cacheLock) {

                        cachedMessages = messages;
                        cachedUrl = url;

                    }

                    if (cachedMessages == null || cachedUrl == null) {

                        throw new IllegalStateException(
                                "ERROR: Voting data is unavailable even after a refresh! Please contact a server administrator.");

                    }

                    plugin.getServer().getScheduler().runTask(plugin, () -> {

                        Utils.send(responder, "&6Vote for &aTrue&4OG &eNetwork &6on &aminecraft-servers.gg&6:");

                        final String voteUrl = UtilitiesOG.stripFormatting(StringUtils.trim(cachedUrl));

                        Component component = MiniMessage.miniMessage().deserialize(
                                "<yellow><bold><underlined>Click here to vote!</underlined></bold></yellow>");
                        component = component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, voteUrl));

                        responder.sendMessage(component);

                    });

                } catch (Exception error) {

                    UtilitiesOG.logToConsole(Votifier.getPrefix(),
                            "ERROR: Failed to process /mcl-vote command: " + " " + error.getMessage());

                    plugin.getServer().getScheduler().runTask(plugin,
                            () -> Utils.send(responder, "&cERROR: Unable to fetch data, please try again later."));

                }

            });

        }

        return true;

    }

}