package pl.ibcgames.mclvotifier;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.trueog.utilitiesog.UtilitiesOG;

public class Test implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        final Votifier plugin = Votifier.getPlugin();
        if (plugin == null) {

            UtilitiesOG.logToConsole(Votifier.getPrefix(), "ERROR: Plugin not initialized; cannot execute command.");
            return true;

        }

        if (Utils.tokenExists(sender)) {

            // Disabled because TrueOG Network does not use OP.
            /*
             * if (!sender.isOp()) {
             * 
             * Utils.send(sender, "&cThis command can be used only by server operator.");
             * return true;
             * 
             * }
             */

            final boolean requirePermission = plugin.getConfiguration().get().getBoolean("require_permission");
            if (requirePermission && !sender.hasPermission("mclvotifier.test")) {

                Utils.send(sender, "&cYou need &amclvotifier.test &cpermission to use this command.");
                return true;

            }

            final List<String> commandList = plugin.getConfiguration().get().getStringList("commands_on_reward");
            if (commandList.isEmpty()) {

                Utils.send(sender, "&cNo reward commands are set. Check commands_on_reward in config.yml.");
                UtilitiesOG.logToConsole(Votifier.getPrefix(),
                        "commands_on_reward is empty. /mcl-test has nothing to execute.");
                return true;

            }

            Utils.send(sender,
                    "&aNo problems were found in the tests. Voting should work correctly. Executing example reward command...");

            execute(commandList, sender.getName());

        }

        return true;

    }

    private static void execute(List<String> commandList, String playerName) {

        commandList.stream().map((final String raw) -> raw.replace("{PLAYER}", playerName))
                .forEach((final String finalCmd) -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd));

    }

}