package com.github.hibi_10000.plugins;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class SidebarEconomy extends JavaPlugin implements Listener {
    boolean enable = true;
    BukkitTask task = null;
    Economy econ = null;
    final List<String> players = new ArrayList<>();

    @Override
    public void onEnable() {
        super.onEnable();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                updateScoreboard();
            }
        }.runTaskTimer(this, 0L, 20L);
        getServer().getPluginManager().registerEvents(this, this);
        setupEconomy();
        updateScoreboard();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        updateScoreboard();
    }

    @SuppressWarnings("deprecation")
    public void updateScoreboard() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
            Objective obj = board.registerNewObjective(p.getName(), "dummy");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.setDisplayName(ChatColor.GOLD + "Money");
            Score score = obj.getScore(p.getName());
            score.setScore((int) econ.getBalance(p.getName()));
            p.setScoreboard(board);
            players.add(p.getName());
            if (players.contains(p.getName())) {
                if (enable) {
                    Score score1 = obj.getScore(p);
                    score1.setScore((int) econ.getBalance(p.getName()));
                    p.setScoreboard(board);
                } else {
                    board.resetScores(p.getName());
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        this.players.remove(e.getPlayer().getName());
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("side")){ 
            if (enable) sender.sendMessage("§A[SideBarEcon] §6Hide the sidebar.");
            else        sender.sendMessage("§A[SideBarEcon] §6Show the sidebar.");
            enable = !enable;
            return true;
        }
        return false;
    }

    public boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
