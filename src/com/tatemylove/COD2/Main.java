package com.tatemylove.COD2;

import com.mysql.fabric.xmlrpc.base.Array;
import com.tatemylove.COD2.Achievement.AchievementAPI;
import com.tatemylove.COD2.Achievement.AchievementMenu;
import com.tatemylove.COD2.Achievement.BaseAchievements;
import com.tatemylove.COD2.Arenas.BaseArena;
import com.tatemylove.COD2.Arenas.KillConfirmed;
import com.tatemylove.COD2.Arenas.TDM;
import com.tatemylove.COD2.Commands.MainCommand;
import com.tatemylove.COD2.Files.*;
import com.tatemylove.COD2.Guns.Guns;
import com.tatemylove.COD2.Inventories.GameInventory;
import com.tatemylove.COD2.KillStreaks.AttackDogs;
import com.tatemylove.COD2.KillStreaks.Mortar;
import com.tatemylove.COD2.KillStreaks.UAV;
import com.tatemylove.COD2.Listeners.InventoryInteract;
import com.tatemylove.COD2.Listeners.PlayerDeath;
import com.tatemylove.COD2.Listeners.PlayerJoin;
import com.tatemylove.COD2.Tasks.CountDown;
import com.tatemylove.COD2.Tasks.LevelUpdater;
import com.tatemylove.COD2.Updater.Updater;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

public class Main extends JavaPlugin {
    public static String prefix;
    public static int time;
    public static int gametime;
    public static int minplayers;
    public static ArrayList<String> arenas = new ArrayList<>();
    public static ArrayList<String> onGoingArenas = new ArrayList<>();
    public static ArrayList<Player> WaitingPlayers = new ArrayList<>();
    public static ArrayList<Player> AllPlayingPlayers = new ArrayList<>();


    public static ArrayList<String> guns = new ArrayList<>();
    public static ArrayList<String> perks = new ArrayList<>();

    public static HashMap<String, ArrayList<String>> ownedGuns = new HashMap<>();

    public static HashMap<String, ArrayList<String>> ownedPerks = new HashMap<>();
    public static HashMap<String, ArrayList<String>> unlockedAchievements = new HashMap<>();
    public static ArrayList<String> achievements = new ArrayList<>();
    public boolean enabled = false;
    public static String version = "2.0.5";



    public static ArrayList<Player> cooldowns = new ArrayList<>();

    public void onEnable(){
        MainCommand cmd = new MainCommand(this);
        getCommand("cod").setExecutor(cmd);

        ArenasFile.setup(this);
        LobbyFile.setup(this);
        GunsFile.setup(this);
        PlayerData.setup(this);
        AchievementFile.setup(this);
        StatsFile.setup(this);

        UAV.settUp();
        AttackDogs.settUp();
        Mortar.settUp();

        BaseAchievements.createAchievements();

        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        if(!GunsFile.getData().contains("Perks.")){
            GunsFile.getData().set("DISCLAIMER", "Don't change/add/remove any Perk values here (except Level, Desc, Material) until I give confirmation that they can be changed. Seriously, you'll break the plugin");
            GunsFile.getData().set("Perks.§6§nFeatherWeight.Name", "§6§nFeatherWeight");
            GunsFile.getData().set("Perks.§6§nFeatherWeight.Desc", Arrays.asList("&bTake less fall damage and faster moving speed ", "&2&lLevel: &b15"));
            GunsFile.getData().set("Perks.§6§nFeatherWeight.Commands", Arrays.asList("/cod give potion swiftness", "/cod give legs enchant falling"));
            GunsFile.getData().set("Perks.§6§nFeatherWeight.Material", "ELYTRA");
            GunsFile.getData().set("Perks.§6§nFeatherWeight.Level", 15);


            GunsFile.getData().set("Perks.§7§nScavenger.Name", "§7§nScavenger");
            GunsFile.getData().set("Perks.§7§nScavenger.Desc", Arrays.asList("&bGet Ammo back on kills", "&2&lLevel: &b10"));
            GunsFile.getData().set("Perks.§7§nScavenger.Level", 10);
            GunsFile.getData().set("Perks.§7§nScavenger.Commands", Arrays.asList("/cod give ammo"));
            GunsFile.getData().set("Perks.§7§nScavenger.Material", "ENDER_CHEST");



            GunsFile.getData().set("Perks.§7§nGhost.Name", "§7§nGhost");
            GunsFile.getData().set("Perks.§7§nGhost.Desc", Arrays.asList("&bRemain invisible to enemy UAVs", "&2&lLevel: &b20"));
            GunsFile.getData().set("Perks.§7§nGhost.Commands", Arrays.asList("/cod give ghost"));
            GunsFile.getData().set("Perks.§7§nGhost.Material", "ENDER_EYE");
            GunsFile.getData().set("Perks.§7§nGhost.Level", 20);


            GunsFile.saveData();
        }

        GameInventory.settUp();

        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
        Bukkit.getServer().getPluginManager().registerEvents(new InventoryInteract(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerDeath(), this);


        getConfig().options().copyDefaults(true);
        saveDefaultConfig();



        matchConfig(new File(getDataFolder() + File.separator + "config.yml"));

        if(getConfig().getBoolean("auto-update")){
            new Updater().autoUpdate();
        }
        new LevelUpdater().runTaskTimer(this, 0, 20);

        time=getConfig().getInt("time-to-start");
        minplayers =getConfig().getInt("min-players");
        gametime=getConfig().getInt("game-time");
        prefix= ChatColor.translateAlternateColorCodes('&',getConfig().getString("prefix"));

        if(ArenasFile.getData().contains("Arenas.")) {
            for (String s : ArenasFile.getData().getConfigurationSection("Arenas.").getKeys(false)) {
                arenas.add(s);
            }
        }
        if(ArenasFile.getData().contains("Arenas.")) {
            CountDown c = new CountDown();
            c.runTaskTimer(this, 0, 20L);

        }else{
            Logger log = getLogger();
            log.severe("COD is disabled, you need to create an arena first, then type /cod enable");
        }
        if(GunsFile.getData().contains("Guns.")){
            for(String s : GunsFile.getData().getConfigurationSection("Guns.").getKeys(false)){
                guns.add(s);
            }
        }

        for(String s : GunsFile.getData().getConfigurationSection("Perks.").getKeys(false)){
            perks.add(s);
        }

        for(String s : AchievementFile.getData().getConfigurationSection("Achievements.").getKeys(false)){
            achievements.add(s);
        }

        if(PlayerData.getData().contains("Players.")){
            for(String s: PlayerData.getData().getConfigurationSection("Players.").getKeys(false)){
                ownedGuns.put(s, new ArrayList<>(PlayerData.getData().getStringList("Players." + s + ".Guns")));
                //ownedSecondary.put(s, new ArrayList<>(PlayerData.getData().getStringList("Players." + s + ".Secondary")));
                ownedPerks.put(s, new ArrayList<>(PlayerData.getData().getStringList("Players." +s+ ".Perks")));
               // ownedSplodes.put(s, new ArrayList<>(PlayerData.getData().getStringList("Players." + s + ".Splode")));*/
            }
        }

    }

    private void matchConfig(File config) {
        try {
            InputStream is = getResource("config.yml");
            if (is != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(config);

                for (String key : defConfig.getConfigurationSection("").getKeys(false))
                    if (!getConfig().contains(key)) getConfig().set(key, defConfig.getConfigurationSection(key));

                for (String key : getConfig().getConfigurationSection("").getKeys(false))
                    if (!defConfig.contains(key)) getConfig().set(key, null);

                saveConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public void onDisable(){
        ArrayList<String> allArenas = new ArrayList<>();
        ArrayList<String > worlds = new ArrayList<>();
        for(String s : ArenasFile.getData().getConfigurationSection("Arenas.").getKeys(false)){
            allArenas.add(s);
        }

        for(String s : allArenas){
            ArenasFile.getData().getString("Arenas." + s + ".Spawns.Blue.World");

            if(!worlds.contains(ArenasFile.getData().getString("Arenas." + s + ".Spawns.Blue.World"))){
                worlds.add(ArenasFile.getData().getString("Arenas." + s + ".Spawns.Blue.World"));
            }
        }

        for(String s : worlds){
            for(Entity e : Bukkit.getWorld(s).getEntities()){
                if(e.hasMetadata("codRedFlag") || e.hasMetadata("codBlueFlag")){
                    e.remove();
                }
            }
        }
    }*/
}
