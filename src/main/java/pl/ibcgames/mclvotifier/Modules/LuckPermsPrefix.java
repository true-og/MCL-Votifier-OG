package pl.ibcgames.mclvotifier.Modules;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;

public final class LuckPermsPrefix {

    private LuckPermsPrefix() {

    }

    public static LuckPerms hook() {

        final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager()
                .getRegistration(LuckPerms.class);

        return provider == null ? null : provider.getProvider();

    }

    public static String getPrefixLegacy(LuckPerms luckPerms, Player player) {

        if (luckPerms == null || player == null) {

            return "";

        }

        final User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {

            return "";

        }

        final CachedMetaData meta = user.getCachedData().getMetaData();
        final String prefix = meta.getPrefix();

        if (prefix == null || StringUtils.isEmpty(prefix)) {

            return "";

        }

        return StringUtils.trim(prefix).replace('§', '&');

    }

}