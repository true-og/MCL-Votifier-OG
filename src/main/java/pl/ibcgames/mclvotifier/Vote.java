package pl.ibcgames.mclvotifier;

import java.util.Date;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Vote implements CommandExecutor {

    String token = Votifier.getPlugin().getConfiguration().get().getString("server_id");
    JSONArray messages;
    String url;
    Date lastUpdate = new Date();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        @SuppressWarnings("unchecked")
        Runnable runnable = () -> {
            try {

                if (token == null || token.equalsIgnoreCase("paste_server_id_here")) {

                    sender.sendMessage(Utils.message("&cNo server id found in MCL-Votifier config"));
                    sender.sendMessage(Utils.message("&cHow to use this plugin? See tutorial at:"));
                    sender.sendMessage(Utils.message("&ahttps://minecraft-servers.gg/mcl-votifier-plugin"));

                    return;
                }

                long diff = new Date().getTime() - lastUpdate.getTime();
                long diffMinutes = diff / (60 * 1000) % 60;

                lastUpdate = new Date();

                if (url == null || diffMinutes >= 60F) {

                    sender.sendMessage(Utils.message("&aRefreshing data..."));

                    JSONObject res =
                            Utils.sendRequest("https://minecraft-servers.gg/api/server-by-key/" + token + "/get-vote");

                    url = res.get("vote_url").toString();

                    messages = (JSONArray) res.get("text");
                }

                messages.forEach((message) -> {
                    sender.sendMessage(Utils.message(message.toString()));
                });

                sender.sendMessage(Utils.message(url));

            } catch (Exception error) {

                error.printStackTrace();

                sender.sendMessage(Utils.message("&cUnable to fetch data, please try again later."));
            }
        };

        Thread thread = new Thread(runnable);

        thread.start();

        return true;
    }
}
