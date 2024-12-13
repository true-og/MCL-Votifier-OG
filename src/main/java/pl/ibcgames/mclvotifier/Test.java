package pl.ibcgames.mclvotifier;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Test implements CommandExecutor {

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

			if (! sender.isOp()) {

				Utils.message(sender, "&cThis command can be used only by server operator");

				return;

			}

			if (require_permission && !sender.hasPermission("mclvotifier.reward")) {

				Utils.message(sender, "&cYou need &amclvotifier.reward &c permission to use this command");

				return;

			}

			Utils.message(sender, "&aThis command can be used to test a reward");
			Utils.message(sender, "&aIf you need to test connection with out website");
			Utils.message(sender, "&asimply claim your reward using &c/mcl-reward &acommand");

			execute(sender);

		};

		Thread thread = new Thread(runnable);
		thread.start();

		return true;

	}

	private void execute(CommandSender sender) {

		for (String cmd : list) {

			cmd = cmd.replace("{PLAYER}", sender.getName());
			String finalCmd = cmd;

			Bukkit.getScheduler().scheduleSyncDelayedTask(Votifier.plugin, () -> {

				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCmd);

			}, 0);

		}

	}

}