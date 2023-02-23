package dev.revivalo.dailyrewards;

import dev.revivalo.dailyrewards.commandmanager.commands.RewardMainCommand;
import dev.revivalo.dailyrewards.commandmanager.commands.RewardsMainCommand;
import dev.revivalo.dailyrewards.configuration.data.PlayerData;
import dev.revivalo.dailyrewards.configuration.enums.Config;
import dev.revivalo.dailyrewards.hooks.Hooks;
import dev.revivalo.dailyrewards.listeners.InventoryClickListener;
import dev.revivalo.dailyrewards.listeners.PlayerJoinQuitListener;
import dev.revivalo.dailyrewards.managers.MenuManager;
import dev.revivalo.dailyrewards.managers.database.MySQLManager;
import dev.revivalo.dailyrewards.managers.reward.RewardManager;
import dev.revivalo.dailyrewards.managers.reward.RewardType;
import dev.revivalo.dailyrewards.updatechecker.UpdateChecker;
import dev.revivalo.dailyrewards.updatechecker.UpdateNotificator;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class DailyRewardsPlugin extends JavaPlugin {
    /*
     */
    private final int RESOURCE_ID = 81780;

    @Setter public static DailyRewardsPlugin plugin;
    @Getter @Setter private static ConsoleCommandSender console;

    @Getter @Setter private static RewardManager rewardManager;
    @Getter @Setter private static MenuManager menuManager;

    @Setter private PluginManager pluginManager;

    @Getter @Setter public static boolean latestVersion;
    @Getter @Setter public static boolean hexSupported;

    public static DailyRewardsPlugin get() {
        return DailyRewardsPlugin.plugin;
    }

    @Override
    public void onEnable() {
        setPlugin(this);

        setConsole(get().getServer().getConsoleSender());
        setPluginManager(getServer().getPluginManager());

        Hooks.hook();

        Config.reload();

        final String serverVersion = Bukkit.getBukkitVersion();
        DailyRewardsPlugin.setHexSupported(
                serverVersion.contains("6") ||
                        serverVersion.contains("7") ||
                        serverVersion.contains("8") ||
                        serverVersion.contains("9"));

        new UpdateChecker(RESOURCE_ID).getVersion(pluginVersion -> {
            if (!Config.UPDATE_CHECKER.asBoolean()) return;

            final String actualVersion = get().getDescription().getVersion();
            final boolean versionMatches = actualVersion.equalsIgnoreCase(pluginVersion);

            get().getLogger().info(versionMatches ?
                    String.format("You are running the latest release (%s)", pluginVersion) :
                    String.format("There is a new v%s update available (You are running v%s).\n" +
                                    "Outdated versions are no longer supported, get the latest one here: " +
                                    "https://www.spigotmc.org/resources/%%E2%%9A%%A1-daily-weekly-monthly-rewards-mysql-hex-colors-support-1-8-1-19-3.81780/",
                            pluginVersion, actualVersion));
            DailyRewardsPlugin.setLatestVersion(versionMatches);
        });

        MySQLManager.init();
        DailyRewardsPlugin.setRewardManager(new RewardManager());
        DailyRewardsPlugin.setMenuManager(new MenuManager());

        get().registerCommands();
        get().implementListeners();

        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[DailyRewards] Update your version to ULTIMATE and get remove limitations!");
        getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "[DailyRewards] Get it here: link");
    }

    @Override
    public void onDisable() {
        PlayerData.removeConfigs();
    }

    private void registerCommands() {
        new RewardMainCommand().registerMainCommand(this, "reward");
        new RewardsMainCommand().registerMainCommand(this, "rewards");
    }

    private void implementListeners() {
        getPluginManager().registerEvents(InventoryClickListener.getInstance(), this);
        getPluginManager().registerEvents(PlayerJoinQuitListener.getInstance(), this);
        getPluginManager().registerEvents(UpdateNotificator.getInstance(), this);
    }

    public static String isPremium(final Player player, final RewardType type) {
        return player.hasPermission(String.format("dailyreward.%s.premium", type)) ? "_PREMIUM" : "";
    }

}