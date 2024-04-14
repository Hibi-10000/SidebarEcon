package com.github.hibi_10000.plugins;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SidebarEconomy extends JavaPlugin implements Listener {
    Map<UUID, Boolean> enableMap = new HashMap<>();
    BukkitTask task = null;
    Economy econ = null;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        if (!setupEconomy()) getServer().getPluginManager().disablePlugin(this);
        task = getServer().getScheduler().runTaskTimer(this, this::updateScoreboard, 0L, 20L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        enableMap.put(e.getPlayer().getUniqueId(), true);
        updateScoreboard();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        enableMap.remove(e.getPlayer().getUniqueId());
    }

    @SuppressWarnings("deprecation")
    public void updateScoreboard() {
        for (Player p : getServer().getOnlinePlayers()) {
            p.setScoreboard(getServer().getScoreboardManager().getNewScoreboard());
            Scoreboard board = getServer().getScoreboardManager().getNewScoreboard();
            Objective obj = board.registerNewObjective(p.getName(), "dummy");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.setDisplayName(ChatColor.GOLD + "Money");
            if (enableMap.get(p.getUniqueId())) {
                Score score = obj.getScore(p);
                score.setScore((int) econ.getBalance(p.getName()));
                p.setScoreboard(board);
            } else {
                board.resetScores(p.getName());
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("side")) {
            if (!(sender instanceof Player)) return false;
            UUID uuid = ((Player) sender).getUniqueId();
            if (enableMap.get(uuid)) sender.sendMessage("§A[SideBarEcon] §6Hide the sidebar.");
            else                     sender.sendMessage("§A[SideBarEcon] §6Show the sidebar.");
            enableMap.replace(uuid, !enableMap.get(uuid));
            return true;
        }
        return false;
    }

    public boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
