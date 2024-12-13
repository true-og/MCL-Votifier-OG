package pl.ibcgames.mclvotifier;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;

public class Reward implements CommandExecutor {

	String token = Votifier.plugin.getConfiguration().get().getString("server_id");
	boolean require_permission = Votifier.plugin.getConfiguration().get().getBoolean("require_permission");
	List<String> list = Votifier.plugin.getConfiguration().get().getStringList("commands_on_reward");
	String[] array = list.toArray(new String[0]);
	Map<String, Date> timeouts = new HashMap<>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		Runnable runnable = () -> {

			if (token == null || token.equalsIgnoreCase("paste_server_id_here")) {

				Utils.message(sender, "&cNo server id found in MCL-Votifier config");
				Utils.message(sender, "&cHow to use this plugin? See tutorial at:");
				Utils.message(sender, "&ahttps://minecraft-servers.gg/mcl-votifier-plugin");

				return;

			}

			if (require_permission && !sender.hasPermission("mclvotifier.reward")) {

				Utils.message(sender, "&cYou need &amclvotifier.reward &c permission to use this command");

				return;

			}

			if (timeouts.containsKey(sender.getName())) {

				Date d = timeouts.get(sender.getName());

				long diff = (long) Math.floor((new Date().getTime() / 1000) - (d.getTime() / 1000));
				if (diff < 60) {

					long remaining = 60 - diff;

					Utils.message(sender, "&cThis command can be used again after " + remaining + " seconds");

					return;

				}

			}

			Utils.message(sender, "&aValidating your vote, please wait...");

			JSONObject res = Utils.sendRequest("https://minecraft-servers.gg/api/server-by-key/" + token + "/get-vote/" + sender.getName());

			timeouts.put(sender.getName(), new Date());

			execute(res, sender);

		};

		Thread thread = new Thread(runnable);
		thread.start();

		return true;

	}

	private void execute(JSONObject res, CommandSender sender) {

		boolean canClaimReward = false;

		if (res.containsKey("can_claim_reward")) {

			canClaimReward = Boolean.parseBoolean(res.get("can_claim_reward").toString());

		}

		if (res.containsKey("error")) {

			Utils.message(sender, res.get("error").toString());

		}

		if (! canClaimReward && !res.containsKey("error")) {

			Utils.message(sender, "&cUnable to claim reward, try again later");


			return;

		}

		if (canClaimReward) {

			for (String cmd : list) {

				cmd = cmd.replace("{PLAYER}", sender.getName());

				String finalCmd = cmd;
				Bukkit.getScheduler().scheduleSyncDelayedTask(Votifier.plugin, () -> {

					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCmd);

				}, 0);

			}

		}

	}

}