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

	public static void message(CommandSender sender, String message) {

		if(sender instanceof Player) {

			UtilitiesOG.trueogMessage((Player) sender, message);


		}
		else {

			UtilitiesOG.logToConsole("[MCL-Votifier-OG] ", message);

		}

	}

	public static JSONObject sendRequest(String url) {

		try {

			URL _url = new URL(url);
			HttpURLConnection con = (HttpURLConnection) _url.openConnection();

			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");

			con.setConnectTimeout(5 * 1000);

			int status = con.getResponseCode();
			Reader streamReader = null;
			if (status > 299) {

				streamReader = new InputStreamReader(con.getErrorStream());

			}
			else {

				streamReader = new InputStreamReader(con.getInputStream());

			}

			BufferedReader in = new BufferedReader(streamReader);
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {

				content.append(inputLine);

			}

			in.close();

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(content.toString());

			return json;

		}
		catch (ParseException | IOException error) {

			error.printStackTrace();

		}

		return new JSONObject();

	}

}