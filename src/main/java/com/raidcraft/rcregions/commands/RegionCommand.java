package com.raidcraft.rcregions.commands;

import com.raidcraft.rcregions.District;
import com.raidcraft.rcregions.DistrictManager;
import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.exceptions.PlayerException;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.spout.SpoutRegionBuy;
import com.raidcraft.rcregions.spout.SpoutRegionInfo;
import com.silthus.raidcraft.util.RCCommandManager;
import com.silthus.raidcraft.util.RCLogger;
import com.silthus.raidcraft.util.RCMessaging;
import com.silthus.raidcraft.util.RCUtils;
import com.silthus.raidcraft.util.databases.logblock.LogBlock;
import com.silthus.raidcraft.util.databases.logblock.LogblockPlayer;
import com.sk89q.worldedit.BlockVector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.player.SpoutPlayer;

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
                    if (RegionsPlugin.isSpoutEnabled() && ((SpoutPlayer)sender).isSpoutCraftEnabled()){
                        if (args.length > 1) {
                            try {
                                Region region = RegionManager.get().getRegion(args[1]);
                                new SpoutRegionBuy((Player)sender, region);
                            } catch (UnknownRegionException e) {
                                RCLogger.error(e);
                            }
                        }
                        else {
                            try {
                                Region region = RegionManager.get().getRegion(((SpoutPlayer) sender).getLocation());
                                SpoutRegionBuy s = new SpoutRegionBuy((Player)sender, region);
                            } catch (UnknownRegionException e) {
                                RCLogger.error(e);
                            }
                        }
                    }
                    else
                    {
                        if (args.length > 1) {
                            buyRegion(args[1]);
                        } else {
                            buyRegion();
                        }
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
            if (cmd.is(label, "warn")) {
                if (sender.hasPermission("rcregions.warn")) {
                    try {
                        Region region = null;
                        if (args.length > 1) {
                            region = RegionManager.get().getRegion(args[1]);
                        } else if (args.length == 1 && sender instanceof Player) {
                            region = RegionManager.get().getRegion(cmd.getPlayerOfSender(sender).getLocation());
                        }
                        if (region != null) {
                            Player player = Bukkit.getPlayer(region.getOwner());
                            if (region.isWarned()) {
                                region.setWarned(false);
                                RCMessaging.send(sender, "Die Verwarnung der Region " + region.getName() + " wurde aufgehoben.");
                                if (player != null)
                                RCMessaging.send(player, "Die Verwarnung deiner Region " + region.getName() + " wurde aufgehoben.");
                            } else {
                                region.setWarned(true);
                                RCMessaging.send(sender, "Die Region " + region.getName() + " wurde verwarnt.");
                                if (player != null)
                                RCMessaging.send(player, "Deine Region " + region.getName() + " wurde verwarnt.");
                            }
                            return true;
                        }
                    } catch (UnknownRegionException e) {
                        RCMessaging.warn(sender, e.getMessage());
                        return true;
                    }
                } else {
                    RCMessaging.noPermission(sender);
                }
            }
            // gets region information about the player
            // [/rcr -p <player>]
            if (cmd.is(label, "player", "-p")) {
                if (sender.hasPermission("rcregions.playerinfo")) {
                    if (args.length > 1) {
                        Player player = Bukkit.getServer().getPlayer(args[1]);
                        if (!(player == null)) {
                            showPlayerInfo(player);
                        } else {
                            RCMessaging.send(sender, "Sorry but I can only show you players who are online.");
                        }
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
            // gives information about the region
            // [/rcr -i <region>]
            if (cmd.is(label, "-i", "info")) {
                if (sender.hasPermission("rcregions.info")) {
                    if (sender instanceof Player && args.length == 1) {
                        try {
                            Region region = RegionManager.get().getRegion(((Player) sender).getLocation());
                            if (RegionsPlugin.get().isSpoutEnabled() && ((SpoutPlayer)sender).isSpoutCraftEnabled()){
                                new SpoutRegionInfo((Player)sender, region);
                            }
                            else
                                showRegionInfo((Player)sender, region);
                        } catch (UnknownRegionException e) {
                            RCMessaging.warn(sender, e.getMessage());
                        }
                    } else {
                        showRegionInfo(args[1]);
                    }
                    return true;
                } else {
                    RCMessaging.noPermission(sender);
                }
            }
        } else if (sender instanceof Player) {
            showPlayerInfo((Player) sender);
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

    private void showPlayerInfo(Player player) {
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
                if (region.isWarned()) {
                    list.add(RCMessaging.red(region.toString()));
                    continue;
                }
                list.add(region.toString());
            }
            RCMessaging.send(sender, "| " + district.toString() + ": "
                    + RCUtils.arrayToString(list, ", "), false);
        }
    }

    private void showRegionInfo(String strRegion) {
        try {
            if (sender instanceof Player)
            showRegionInfo((Player)sender, RegionManager.get().getRegion(strRegion));
        } catch (UnknownRegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }

    public static void  showRegionInfo(Player player, Region region) {
        RegionManager regionManager = RegionManager.get();
        if (RegionsPlugin.get().isSpoutEnabled() && ((SpoutPlayer)player).isSpoutCraftEnabled()){
            SpoutRegionInfo s = new SpoutRegionInfo(player, region);
        }
        else
        {
            District district = region.getDistrict();
            RCMessaging.send(player, "|---------- " + RCMessaging.green("Raid-Craft.de") + " -----------|",false);
            RCMessaging.send(player, "| " + RCMessaging.green("Region: ") + RCMessaging.yellow(region.toString()) + " | "
                        + RCMessaging.green("Distrikt: ") + RCMessaging.yellow(district.toString()),false);
            String lastLogin = "";
            LogblockPlayer logblockPlayer = LogBlock.getInstance().getPlayer(region.getOwner());
            if(logblockPlayer != null) {
                lastLogin += " | " + RCMessaging.green("Lastlogin: ") + RCMessaging.yellow(logblockPlayer.getLastlogin());
            }
            RCMessaging.send(player, "| " + RCMessaging.green("Besitzer: ") + RCMessaging.yellow(region.getOwner())
                    + lastLogin, false);
            RCMessaging.send(player, "| " + RCMessaging.green("Refund: ") + RCMessaging.yellow(regionManager.getRefundPercentage(region) * 100 + "%"), false);
            RCMessaging.send(player, "| " + RCMessaging.green("Aktueller Preis: ") + RCMessaging.yellow(region.getPrice() + ""),false);
            RCMessaging.send(player, "| " + RCMessaging.green("Basis Preis: ") + RCMessaging.yellow(region.getBasePrice() + ""),false);
            RCMessaging.send(player, "| " + RCMessaging.green("Rückzahlung: ") + RCMessaging.yellow(regionManager.getRefundValue(region) + ""),false);
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
            if (!((SpoutPlayer)player).isSpoutCraftEnabled())
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
                        RCMessaging.yellow(RegionManager.get().getRefundValue(region) + "") + " Coins an den Server verkauft.");
            } catch (RegionException e) {
                RCMessaging.warn(sender, e.getMessage());
            }
        } else {
            RCMessaging.noPermission(player);
        }
    }
}
