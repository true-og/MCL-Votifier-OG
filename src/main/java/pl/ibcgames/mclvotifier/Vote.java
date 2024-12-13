package pl.ibcgames.mclvotifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Vote implements CommandExecutor {

	private final String token = Votifier.plugin.getConfiguration().get().getString("server_id");
	private List<String> messages = new ArrayList<>();
	private String url;
	private Date lastUpdate = new Date();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		Bukkit.getScheduler().runTaskAsynchronously(Votifier.plugin, () -> {

			try {

				// Check for a valid token.
				if (token == null || token.equalsIgnoreCase("paste_server_id_here")) {

					Utils.message(sender, "<red>No server id found in MCL-Votifier config");
					Utils.message(sender, "<red>How to use this plugin? See tutorial at:");
					Utils.message(sender, "<green>https://minecraft-servers.gg/mcl-votifier-plugin");

					return;

				}

				// Check if data needs to be refreshed.
				long diffMinutes = (new Date().getTime() - lastUpdate.getTime()) / (60 * 1000);
				if (url == null || diffMinutes >= 60) {

					Utils.message(sender, "<green>Refreshing data...");

					JSONObject res = Utils.sendRequest("https://minecraft-servers.gg/api/server-by-key/" + token + "/get-vote");
					url = (String) res.get("vote_url");

					messages = convertJSONArrayToList((JSONArray) res.get("text"));

					// Update last refresh time.
					lastUpdate = new Date();

				}

				messages.forEach(message -> Utils.message(sender, message));

				Utils.message(sender, url);

			}
			catch (Exception error) {

				error.printStackTrace();

				Utils.message(sender, "<red>Unable to fetch data, please try again later!");

			}

		});

		return true;

	}

	// Utility to convert JSONArray to List<String>.
	private List<String> convertJSONArrayToList(JSONArray jsonArray) {

		List<String> list = new ArrayList<>();
		for (Object item : jsonArray) {

			// Ensure type safety.
			if (item instanceof String) {

				list.add((String) item);

			}
			else {

				// Log a warning or handle unexpected types gracefully.
				System.out.println("Unexpected item type in JSONArray: " + item.getClass().getName());

			}

		}

		return list;

	}

}