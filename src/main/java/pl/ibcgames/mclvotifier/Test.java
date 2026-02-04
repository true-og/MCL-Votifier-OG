package pl.ibcgames.mclvotifier;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Test implements CommandExecutor {

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

        if (!sender.isOp()) {

            sender.sendMessage(Utils.message("&cThis command can be used only by server operator"));

            return true;

        }

        boolean requirePermission = plugin.getConfiguration().get().getBoolean("require_permission");
        if (requirePermission && !sender.hasPermission("mclvotifier.reward")) {

            sender.sendMessage(Utils.message("&cYou need &amclvotifier.reward &c permission to use this command"));

            return true;

        }

        List<String> commandList = plugin.getConfiguration().get().getStringList("commands_on_reward");
        if (commandList.isEmpty()) {

            sender.sendMessage(Utils.message("&cNo reward commands are configured. Check commands_on_reward."));
            plugin.warning("commands_on_reward is empty; /mcl-test has nothing to execute.");
            return true;

        }

        sender.sendMessage(Utils.message("&aThis command can be used to test a reward"));
        sender.sendMessage(Utils.message("&aIf you need to test connection with out website"));
        sender.sendMessage(Utils.message("&asimply claim your reward using &c/mcl-reward &acommand"));

        execute(sender, commandList);

        return true;

    }

    private void execute(CommandSender sender, List<String> commandList) {

        for (String cmd : commandList) {

            cmd = cmd.replace("{PLAYER}", sender.getName());
            String finalCmd = cmd;

            Bukkit.getScheduler().scheduleSyncDelayedTask(Votifier.getPlugin(), () -> {

                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCmd);

            }, 0);

        }

    }

}
