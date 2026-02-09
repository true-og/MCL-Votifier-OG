package pl.ibcgames.mclvotifier;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

                        // Useful for debugging issues with the minecraft-servers.gg API response.
                        // debugDumpResponse(sender, "GET /get-vote response", res);

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

        if (!(sender instanceof org.bukkit.entity.Player)) {

            Utils.send(sender, "&cERROR: Only players can claim vote rewards.");

            return;

        }

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

        if (!canClaimReward) {

            return;

        }

        commandList.stream().map(cmd -> {

            cmd = cmd.replace("{PLAYER}", sender.getName());
            return cmd;

        }).forEach(finalCmd -> Bukkit.getScheduler().scheduleSyncDelayedTask(Votifier.getPlugin(), () -> {

            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCmd);

        }, 0));
        Bukkit.getOnlinePlayers().forEach(p -> UtilitiesOG.trueogMessage(p, Votifier.getPlayerPrefix((Player) sender)
                + sender.getName()
                + " &ahas voted for &aTrue&4OG &eNetwork &aat minecraft-servers.gg and earned 15 experience levels!"));

    }

    // A debug function to see the JSON returned from minecraft-servers.gg
    /*
     * private static void debugDumpResponse(CommandSender sender, String context,
     * JSONObject res) {
     * 
     * final String header = "[DEBUG] " + context + " | player=" + sender.getName();
     * UtilitiesOG.logToConsole(Votifier.getPrefix(), header);
     * 
     * if (res == null) {
     * 
     * UtilitiesOG.logToConsole(Votifier.getPrefix(), "[DEBUG] response = null");
     * return;
     * 
     * }
     * 
     * final StringBuilder sb = new StringBuilder(2048); appendJsonDump(sb, res,
     * "$", 0, 6, 30, 180);
     * 
     * for (String line : sb.toString().split("\n")) {
     * 
     * if (!line.isBlank()) {
     * 
     * UtilitiesOG.logToConsole(Votifier.getPrefix(), "[DEBUG] " + line);
     * 
     * }
     * 
     * }
     * 
     * }
     * 
     * private static void appendJsonDump(StringBuilder sb, Object node, String
     * path, int depth, int maxDepth, int maxCollectionItems, int maxValueChars) {
     * 
     * if (node == null) {
     * 
     * sb.append(path).append(" = null\n"); return;
     * 
     * }
     * 
     * if (depth >= maxDepth) {
     * 
     * sb.append(path).append(" = <max depth reached> (").append(node.getClass().
     * getName()).append(")\n"); return;
     * 
     * }
     * 
     * if (node instanceof JSONObject obj) {
     * 
     * sb.append(path).append(" {JSONObject} keys=").append(obj.size()).append("\n")
     * ;
     * 
     * final List<String> keys = new ArrayList<>(); for (Object k : obj.keySet())
     * keys.add(String.valueOf(k)); Collections.sort(keys);
     * 
     * int shown = 0; for (String k : keys) {
     * 
     * if (shown++ >= maxCollectionItems) {
     * 
     * sb.append(path).append(" ... (").append(obj.size() -
     * maxCollectionItems).append(" more keys)\n"); break;
     * 
     * }
     * 
     * appendJsonDump(sb, obj.get(k), path + "." + k, depth + 1, maxDepth,
     * maxCollectionItems, maxValueChars);
     * 
     * }
     * 
     * return;
     * 
     * }
     * 
     * if (node instanceof JSONArray arr) {
     * 
     * sb.append(path).append(" {JSONArray} size=").append(arr.size()).append("\n");
     * final int limit = Math.min(arr.size(), maxCollectionItems); for (int i = 0; i
     * < limit; i++) {
     * 
     * appendJsonDump(sb, arr.get(i), path + "[" + i + "]", depth + 1, maxDepth,
     * maxCollectionItems, maxValueChars);
     * 
     * }
     * 
     * if (arr.size() > maxCollectionItems) {
     * 
     * sb.append(path).append(" ... (").append(arr.size() -
     * maxCollectionItems).append(" more items)\n");
     * 
     * }
     * 
     * return;
     * 
     * }
     * 
     * String val = String.valueOf(node); val = val.replace("\r",
     * "\\r").replace("\n", "\\n"); if (val.length() > maxValueChars) val =
     * val.substring(0, maxValueChars) + "...";
     * 
     * sb.append(path).append(" = ").append(val).append(" (").append(node.getClass()
     * .getName()).append(")\n");
     * 
     * }
     */

}