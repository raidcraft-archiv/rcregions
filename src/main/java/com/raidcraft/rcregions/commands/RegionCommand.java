package com.raidcraft.rcregions.commands;

import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.exceptions.PlayerException;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.silthus.raidcraft.util.RCCommandManager;
import com.silthus.raidcraft.util.RCMessaging;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 17.12.11 - 11:30
 *
 * @author Silthus
 */
public class RegionCommand implements CommandExecutor {

    private final RCCommandManager cmd = new RCCommandManager();
    private CommandSender sender;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmdLabel, String[] args) {
        String label;
        this.sender = sender;
        if (args.length > 0) {
            label = args[0];
            // buys or claims the region the player is standing on
            // [/rcr claim]
            if (cmd.is(label, "buy", "claim", "-b")) {
                if (sender.hasPermission("rcregions.region.buy") && sender instanceof Player) {
                    if (args.length > 1) {
                        buyRegion(args[1]);
                    } else {
                        buyRegion();
                    }
                } else {
                    RCMessaging.noPermission(sender);
                }
            }
            if (cmd.is(label, "drop", "sell", "-d", "-s")) {
                if (sender.hasPermission("rcregions.region.drop") && sender instanceof Player) {
                    if (args.length > 1) {
                        dropRegion(args[1]);
                    } else {
                        dropRegion();
                    }
                } else {
                    RCMessaging.noPermission(sender);
                }
            }
            if (cmd.is(label, "reload")) {
                if (sender.hasPermission("rcregions.admin")) {
                    RegionsPlugin.get().reload();
                    RCMessaging.send(sender, "Config Files reloaded and Cache cleared.");
                } else {
                    RCMessaging.noPermission(sender);
                }
            }
        }
        return true;
    }
    
    private void buyRegion(String region) {
        try {
            buyRegion(RegionManager.get().getRegion(region));
        } catch (UnknownRegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }
    
    private void buyRegion() {
        try {
            Player player = cmd.getPlayerOfSender(sender);
            Location location = player.getLocation();
            buyRegion(RegionManager.get().getRegion(location));
        } catch (UnknownRegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }

    private void buyRegion(Region region) {
        try {
            Player player = cmd.getPlayerOfSender(sender);
            RegionManager regionManager = RegionManager.get();
            regionManager.buyRegion(player, region);
            RCMessaging.send(sender, "Neues Grundstück erworben: " + region.getName());
        } catch (RegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        } catch (PlayerException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }

    private void dropRegion(String region) {
        try {
            dropRegion(RegionManager.get().getRegion(region));
        } catch (UnknownRegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }
    
    private void dropRegion() {
        try {
            Player player = cmd.getPlayerOfSender(sender);
            Region region = RegionManager.get().getRegion(player.getLocation());
            dropRegion(region);
        } catch (UnknownRegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }

    private void dropRegion(Region region) {
        Player player = cmd.getPlayerOfSender(sender);
        if (region.getOwner().equalsIgnoreCase(player.getName()) || sender.hasPermission("rcregions.admin")) {
            RegionManager.get().clearRegion(player, region);
            RCMessaging.send(sender, "Deine Region " + region.getName() + " wurde für " +
                    RCMessaging.yellow(region.getDistrict().getMinPrice() + "") + " Coins an den Server verkauft.");
        } else {
            RCMessaging.noPermission(player);
        }
    }
}
