package pl.ibcgames.mclvotifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.trueog.utilitiesog.UtilitiesOG;

public class Utils {

    public static JSONObject sendRequest(String url) {

        try {

            final URL requestUrl = new URL(url);
            final HttpURLConnection con = (HttpURLConnection) requestUrl.openConnection();

            con.setRequestMethod("GET");

            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");

            con.setConnectTimeout(5 * 1000);

            final int status = con.getResponseCode();
            Reader streamReader = null;

            if (status > 299) {

                streamReader = new InputStreamReader(con.getErrorStream());

            } else {

                streamReader = new InputStreamReader(con.getInputStream());

            }

            final BufferedReader in = new BufferedReader(streamReader);
            final StringBuilder content = new StringBuilder();
            in.lines().forEach(content::append);

            in.close();

            final JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(content.toString());

        } catch (ParseException | IOException error) {

            error.printStackTrace();

        }

        return new JSONObject();

    }

    public static void send(CommandSender sender, String message) {

        if (sender instanceof Player player) {

            UtilitiesOG.trueogMessage(player, Votifier.getPrefix() + " " + message);
            return;

        }

        UtilitiesOG.logToConsole(Votifier.getPrefix(), message);

    }

    public static boolean tokenExists(CommandSender sender) {

        final String token = Votifier.getPlugin().getConfiguration().get().getString("server_id");

        if (token != null) {

            return true;

        }

        Utils.send(sender, "&cERROR: No server id found in MCL-Votifier config.");
        Utils.send(sender, "&6Not sure how to use this plugin? Read the tutorial at:");
        Utils.send(sender, "&ahttps://minecraft-servers.gg/mcl-votifier-plugin");

        return false;

    }

}