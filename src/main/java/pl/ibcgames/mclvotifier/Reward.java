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

import net.trueog.utilitiesog.UtilitiesOG;

public class Reward implements CommandExecutor {

    private final Map<String, Date> timeouts = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        final Votifier plugin = Votifier.getPlugin();
        if (plugin == null) {

            UtilitiesOG.logToConsole(Votifier.getPrefix(), "ERROR: Plugin not initialized; cannot execute command.");
            return true;

        }

        if (Utils.tokenExists(sender)) {

            final String token = plugin.getConfiguration().get().getString("server_id");
            final boolean requirePermission = plugin.getConfiguration().get().getBoolean("require_permission");
            if (requirePermission && !sender.hasPermission("mclvotifier.reward")) {

                Utils.send(sender, "&cERROR: You need &amclvotifier.reward &c permission to use this command");

                return true;

            }

            if (timeouts.containsKey(sender.getName())) {

                final Date d = timeouts.get(sender.getName());
                final long diff = (long) Math.floor((new Date().getTime() / 1000) - (d.getTime() / 1000));
                if (diff < 60) {

                    final long remaining = 60 - diff;

                    Utils.send(sender, "&6That command can be used again after &e" + remaining + " &6seconds");

                    return true;

                }

            }

            Utils.send(sender, "&aValidating your vote, please wait...");

            final List<String> commandList = plugin.getConfiguration().get().getStringList("commands_on_reward");
            if (commandList.isEmpty()) {

                Utils.send(sender,
                        "&cERROR: No reward commands are configured. Check commands_on_reward in config.yml.");
                UtilitiesOG.logToConsole(Votifier.getPrefix(),
                        "ERROR: commands_on_reward is empty; /mcl-reward has nothing to execute.");

                return true;

            }

            final Runnable runnable = () -> {

                try {

                    final JSONObject res = Utils.sendRequest("https://minecraft-servers.gg/api/server-by-key/" + token
                            + "/get-vote/" + sender.getName());

                    plugin.getServer().getScheduler().runTask(plugin, () -> {

                        timeouts.put(sender.getName(), new Date());
                        execute(res, sender, commandList);

                    });

                } catch (Exception error) {

                    error.printStackTrace();
                    Utils.send(sender, "&cERROR: Failed to process /mcl-reward command: " + error.getMessage());
                    UtilitiesOG.logToConsole(Votifier.getPrefix(),
                            "&cUnable to validate vote for " + sender.getName() + ": " + error.getMessage());

                }

            };

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);

        }

        return true;

    }

    private void execute(JSONObject res, CommandSender sender, List<String> commandList) {

        boolean canClaimReward = false;

        if (res.containsKey("can_claim_reward")) {

            canClaimReward = Boolean.parseBoolean(res.get("can_claim_reward").toString());

        }

        if (res.containsKey("error")) {

            Utils.send(sender, (res.get("error").toString()));

        }

        if (!canClaimReward && !res.containsKey("error")) {

            Utils.send(sender,
                    ("&cERROR: Unable to claim voting reward, double check that you voted today with &a/mcl-vote &cand then try again."));

            return;

        }

        if (canClaimReward) {

            commandList.stream().map(cmd -> {

                cmd = cmd.replace("{PLAYER}", sender.getName());
                return cmd;

            }).forEach(finalCmd -> Bukkit.getScheduler().scheduleSyncDelayedTask(Votifier.getPlugin(), () -> {

                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCmd);

            }, 0));

        }

    }

}