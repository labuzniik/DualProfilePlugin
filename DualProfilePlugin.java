package me.yourname.dualprofile;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DualProfilePlugin extends JavaPlugin implements TabExecutor {

    private File profilesFolder;

    @Override
    public void onEnable() {
        profilesFolder = new File(getDataFolder(), "profiles");
        if (!profilesFolder.exists()) {
            profilesFolder.mkdirs();
        }
        getCommand("switchprofile").setExecutor(this);
    }

    private File getProfileFile(UUID uuid, String profileName) {
        return new File(profilesFolder, uuid + "-" + profileName + ".yml");
    }

    private void saveProfile(Player player, String profileName) {
        File file = getProfileFile(player.getUniqueId(), profileName);
        FileConfiguration config = new YamlConfiguration();

        config.set("inventory", player.getInventory().getContents());
        config.set("armor", player.getInventory().getArmorContents());
        config.set("gamemode", player.getGameMode().toString());

        Location loc = player.getLocation();
        config.set("location.world", loc.getWorld().getName());
        config.set("location.x", loc.getX());
        config.set("location.y", loc.getY());
        config.set("location.z", loc.getZ());
        config.set("location.yaw", loc.getYaw());
        config.set("location.pitch", loc.getPitch());

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProfile(Player player, String profileName) {
        File file = getProfileFile(player.getUniqueId(), profileName);
        if (!file.exists()) {
            player.sendMessage("§cProfile does not exist.");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        player.getInventory().setContents(((List<ItemStack>) config.get("inventory")).toArray(new ItemStack[0]));
        player.getInventory().setArmorContents(((List<ItemStack>) config.get("armor")).toArray(new ItemStack[0]));

        player.setGameMode(GameMode.valueOf(config.getString("gamemode")));

        Location loc = new Location(
                Bukkit.getWorld(config.getString("location.world")),
                config.getDouble("location.x"),
                config.getDouble("location.y"),
                config.getDouble("location.z"),
                (float) config.getDouble("location.yaw"),
                (float) config.getDouble("location.pitch")
        );

        player.teleport(loc);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 1 || !(args[0].equalsIgnoreCase("admin") || args[0].equalsIgnoreCase("survival"))) {
            player.sendMessage("§cUsage: /switchprofile <admin|survival>");
            return true;
        }

        String currentProfile = args[0].equalsIgnoreCase("admin") ? "survival" : "admin";
        saveProfile(player, currentProfile);
        loadProfile(player, args[0].toLowerCase());
        player.sendMessage("§aSwitched to profile: " + args[0]);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("admin", "survival");
        }
        return Collections.emptyList();
    }
}
