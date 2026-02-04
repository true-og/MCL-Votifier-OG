package pl.ibcgames.mclvotifier;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;

public class Reward implements CommandExecutor {

    private final Map<String, Date> timeouts = new HashMap<>();

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

        boolean requirePermission = plugin.getConfiguration().get().getBoolean("require_permission");
        if (requirePermission && !sender.hasPermission("mclvotifier.reward")) {

            sender.sendMessage(Utils.message("&cYou need &amclvotifier.reward &c permission to use this command"));

            return true;

        }

        if (timeouts.containsKey(sender.getName())) {

            Date d = timeouts.get(sender.getName());
            long diff = (long) Math.floor((new Date().getTime() / 1000) - (d.getTime() / 1000));
            if (diff < 60) {

                long remaining = 60 - diff;

                sender.sendMessage(Utils.message("&cThis command can be used again after " + remaining + " seconds"));

                return true;

            }

        }

        sender.sendMessage(Utils.message("&aValidating your vote, please wait..."));

        List<String> commandList = plugin.getConfiguration().get().getStringList("commands_on_reward");
        if (commandList.isEmpty()) {

            sender.sendMessage(Utils.message("&cNo reward commands are configured. Check commands_on_reward."));
            plugin.warning("commands_on_reward is empty; /mcl-reward has nothing to execute.");
            return true;

        }

        Runnable runnable = () -> {

            try {

                JSONObject res = Utils.sendRequest(
                        "https://minecraft-servers.gg/api/server-by-key/" + token + "/get-vote/" + sender.getName());

                plugin.getServer().getScheduler().runTask(plugin, () -> {

                    timeouts.put(sender.getName(), new Date());
                    execute(res, sender, commandList);

                });

            } catch (Exception error) {

                error.printStackTrace();
                plugin.warning("Failed to process /mcl-reward command: " + Objects.toString(error.getMessage()));
                plugin.getServer().getScheduler().runTask(plugin, () -> sender
                        .sendMessage(Utils.message("&cUnable to validate your vote right now. Check server logs.")));

            }

        };

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);

        return true;

    }

    private void execute(JSONObject res, CommandSender sender, List<String> commandList) {

        boolean canClaimReward = false;

        if (res.containsKey("can_claim_reward")) {

            canClaimReward = Boolean.parseBoolean(res.get("can_claim_reward").toString());

        }

        if (res.containsKey("error")) {

            sender.sendMessage(Utils.message(res.get("error").toString()));

        }

        if (!canClaimReward && !res.containsKey("error")) {

            sender.sendMessage(Utils.message("&cUnable to claim reward, try again later"));

            return;

        }

        if (canClaimReward) {

            for (String cmd : commandList) {

                cmd = cmd.replace("{PLAYER}", sender.getName());
                String finalCmd = cmd;

                Bukkit.getScheduler().scheduleSyncDelayedTask(Votifier.getPlugin(), () -> {

                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCmd);

                }, 0);

            }

        }

    }

}
