package com.raidcraft.rcregions.commands;

import com.raidcraft.rcregions.District;
import com.raidcraft.rcregions.DistrictManager;
import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.exceptions.PlayerException;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.silthus.raidcraft.util.RCCommandManager;
import com.silthus.raidcraft.util.RCMessaging;
import com.silthus.raidcraft.util.RCUtils;
import com.sk89q.worldedit.BlockVector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 17.12.11 - 11:30
 *
 * @author Silthus
 */
public class RegionCommand implements CommandExecutor {

    private final RCCommandManager cmd = RCCommandManager.get();
    private CommandSender sender;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmdLabel, String[] args) {
        String label;
        this.sender = sender;
        if (args.length > 0) {
            label = args[0];
            // buys or claims the region the player is standing on
            // [/rcr claim <region>]
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
            // sells to region to the server
            // [/rcr drop <region>]
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
            // warps the player to the max point of the region
            // [/rcr warp <region>]
            if (cmd.is(label, "warp", "-w") && args.length > 1) {
                if (sender.hasPermission("rcregions.region.warp") && sender instanceof Player) {
                    warp((Player) sender, args[1]);
                } else {
                    RCMessaging.noPermission(sender);
                }
            }
            // displays the diffrent taxes for the districts
            // [/rcr tax]
            if (cmd.is(label, "tax", "-t")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    for (District district : DistrictManager.get().getDistricts().values()) {
                        double taxes = RegionManager.get().getTaxes(player, district);
                        if (taxes > 0) {
                            RCMessaging.send(sender, district + ": "
                                    + RCMessaging.yellow(taxes * 100 + "%"));
                        }
                    }
                } else {
                    RCMessaging.noPermission(sender);
                }
            }
            // reloads the config
            // [/rcr reload]
            if (cmd.is(label, "reload")) {
                if (sender.hasPermission("rcregions.admin")) {
                    RegionsPlugin.get().reload();
                    RCMessaging.send(sender, "Config Files reloaded and Cache cleared.");
                } else {
                    RCMessaging.noPermission(sender);
                }
            }
        } else if (sender instanceof Player) {
            showRegionInfo((Player)sender);
        }
        return true;
    }

    private void warp(Player player, String strRegion) {
        try {
            Region region = RegionManager.get().getRegion(strRegion);
            BlockVector maximumPoint = region.getRegion().getMaximumPoint();
            Location location = new Location(player.getWorld(), maximumPoint.getX(), maximumPoint.getY(), maximumPoint.getZ());
            player.teleport(location);
            RCMessaging.send(player, "Teleported to Region "
                    + RCMessaging.green(region.getName()) + " owned by " + RCMessaging.green(region.getOwner()));
        } catch (UnknownRegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }

    private void showRegionInfo(Player player) {
        List<Region> regions = RegionManager.get().getPlayerRegions(player);
        Set<District> uniqueDistricts = new HashSet<District>();
        Map<String,District> districts = DistrictManager.get().getDistricts();
        for (Region region : regions) {
            uniqueDistricts.add(region.getDistrict());
        }
        RCMessaging.send(sender, "|---------- " + RCMessaging.green("Raid-Craft.de") + " -----------|",false);
        RCMessaging.send(sender, "| " + RCMessaging.green("Regionen: ") + RCMessaging.yellow(String.valueOf(regions.size())) + " | "
                + RCMessaging.green("Distrikte: ") + RCMessaging.yellow(uniqueDistricts.size() + "/" + districts.size()),false);
        for (District district : uniqueDistricts) {
            ArrayList<String> list = new ArrayList<String>();
            for (Region region : RegionManager.get().getPlayerRegions(player, district)) {
                list.add(region.toString());
            }
            RCMessaging.send(sender, "| " + district.toString() + ": "
                    + RCUtils.arrayToString(list, ", "), false);
        }
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
            try {
                RegionManager.get().dropRegion(player, region);
                RCMessaging.send(sender, "Deine Region " + region.getName() + " wurde für " +
                        RCMessaging.yellow(region.getDistrict().getMinPrice() + "") + " Coins an den Server verkauft.");
            } catch (RegionException e) {
                RCMessaging.warn(sender, e.getMessage());
            }
        } else {
            RCMessaging.noPermission(player);
        }
    }
}
