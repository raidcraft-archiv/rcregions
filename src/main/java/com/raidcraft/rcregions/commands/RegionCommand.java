package com.raidcraft.rcregions.commands;

import com.raidcraft.rcregions.*;
import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.database.LogTable;
import com.raidcraft.rcregions.database.RegionsDatabase;
import com.raidcraft.rcregions.exceptions.PlayerException;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.spout.SpoutRegionBuy;
import com.raidcraft.rcregions.spout.SpoutRegionInfo;
import com.raidcraft.rcregions.util.Enums;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.util.RCCommandManager;
import com.silthus.raidcraft.util.RCLogger;
import com.silthus.raidcraft.util.RCMessaging;
import com.silthus.raidcraft.util.RCUtils;
import com.silthus.raidcraft.util.databases.logblock.LBPlayer;
import com.silthus.raidcraft.util.databases.logblock.LogBlock;
import com.sk89q.worldedit.BlockVector;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
                                Region region = RegionManager.getInstance().getRegion(args[1]);
                                new SpoutRegionBuy((Player)sender, region);
                            } catch (UnknownRegionException e) {
                                RCLogger.error(e);
                            }
                        }
                        else {
                            try {
                                Region region = RegionManager.getInstance().getRegion(((SpoutPlayer) sender).getLocation());
                                new SpoutRegionBuy((Player)sender, region);
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
                return true;
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
                return true;
            }
            if (cmd.is(label, "warn")) {
                if (sender.hasPermission("rcregions.warn")) {
                    try {
                        Region region;
                        if (args.length > 2) {
                            region = RegionManager.getInstance().getRegion(args[1]);
                        } else {
                            RCMessaging.warn(sender, "Syntax: /rcr warn <region> <warning>");
	                        return true;
                        }
                        if (region != null) {
	                        String msg = "";
	                        for (int i = 2; i < args.length; i++) {
		                        msg += args[i] + " ";
	                        }
	                        RegionWarning warning =  region.addWarning(msg);
	                        RCMessaging.send(sender,
			                        "Die Region " + region.getName() + " wurde verwarnt: ");
					        RCMessaging.send(sender, ChatColor.YELLOW + "[" + ChatColor.GREEN + warning.getId() + ChatColor.YELLOW + "] " +
					                        ChatColor.RED + warning.getMessage());
                        }
                    } catch (UnknownRegionException e) {
                        RCMessaging.warn(sender, e.getMessage());
                        return true;
                    }
                } else {
                    RCMessaging.noPermission(sender);
                }
                return true;
            }
	        if (cmd.is(label, "listwarnings")) {
		        if (sender.hasPermission("rcregions.warn.list")) {
			        List<RegionWarning> warnings = RegionManager.getInstance().getAllRegionWarnings();
			        if (warnings.size() > 0) {
				        for (RegionWarning warning : warnings) {
					        sender.sendMessage(
							        ChatColor.YELLOW + "[" + ChatColor.GREEN + warning.getId() + ChatColor.YELLOW + "]" +
									        "[" + ChatColor.AQUA + warning.getRegion().getName() + ChatColor.YELLOW + "]" +
									        ChatColor.GREEN + " - " + ChatColor.YELLOW +
									        RegionWarning.DATE_FORMAT.format(new Date(warning.getTime()))
									        + ChatColor.GREEN + " - " + ChatColor.RED + warning.getMessage());
				        }
			        } else {
				        sender.sendMessage(ChatColor.RED + "Es gibt momentan keine verwarnten Regionen.");
			        }
		        } else {
			        RCMessaging.noPermission(sender);
		        }
		        return true;
	        }
	        if (cmd.is(label, "removewarning")) {
		        if (sender.hasPermission("rcregions.warn")) {
			        if (args.length > 1) {
				        try {
					        int id = Integer.parseInt(args[1]);
					        RegionManager.getInstance().getRegionWarning(id).remove();
				        } catch (NumberFormatException e) {
							RCMessaging.warn(sender, "Bitte die ID der Verwarnung angeben.");
				        } catch (UnknownRegionException e) {
							RCMessaging.warn(sender, e.getMessage());
				        }
			        } else {
						RCMessaging.warn(sender, "Syntax: /rcr removewarning <id>");
			        }
		        } else {
			        RCMessaging.noPermission(sender);
		        }
		        return true;
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
                return true;
            }
            // warps the player to the max point of the region
            // [/rcr warp <region>]
            if (cmd.is(label, "warp", "-w") && args.length > 1) {
                if (sender.hasPermission("rcregions.region.warp") && sender instanceof Player) {
                    warp((Player) sender, args[1]);
                } else {
                    RCMessaging.noPermission(sender);
                }
                return true;
            }
            // displays the diffrent taxes for the districts
            // [/rcr tax]
            if (cmd.is(label, "tax", "-t")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    for (District district : DistrictManager.get().getDistricts().values()) {
                        double taxes = RegionManager.getInstance().getTaxes(player, district);
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
                return true;
            }
            // gives information about the region
            // [/rcr -i <region>]
            if (cmd.is(label, "-i", "info")) {
                if (sender.hasPermission("rcregions.info")) {
                    if (sender instanceof Player && args.length == 1) {
                        try {
                            Region region = RegionManager.getInstance().getRegion(((Player) sender).getLocation());
                            if (BukkitBasePlugin.isSpoutEnabled() && ((SpoutPlayer)sender).isSpoutCraftEnabled()){
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
                return true;
            }
        } else if (sender instanceof Player) {
            showPlayerInfo((Player) sender);
            return true;
        }
        RCMessaging.send(sender, ChatColor.RED + "[RCRegion] Parameter konnte nicht zugeordnet werden!", false);
        return true;
    }

    private void warp(Player player, String strRegion) {
        try {
            Region region = RegionManager.getInstance().getRegion(strRegion);
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
        List<Region> regions = RegionManager.getInstance().getPlayerRegions(player);
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
            for (Region region : RegionManager.getInstance().getPlayerRegions(player, district)) {
                if (region.hasWarnings()) {
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
            showRegionInfo((Player)sender, RegionManager.getInstance().getRegion(strRegion));
        } catch (UnknownRegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }

    public static void  showRegionInfo(Player player, Region region) {
        RegionManager regionManager = RegionManager.getInstance();
        if (BukkitBasePlugin.isSpoutEnabled() && ((SpoutPlayer)player).isSpoutCraftEnabled()){
            new SpoutRegionInfo(player, region);
        }  else {
            District district = region.getDistrict();
            RCMessaging.send(player, "|---------- " + RCMessaging.green("Raid-Craft.de") + " -----------|",false);
            RCMessaging.send(player, "| " + RCMessaging.green("Region: ") + RCMessaging.yellow(region.toString()) + " | "
                        + RCMessaging.green("Distrikt: ") + RCMessaging.yellow(district.toString()),false);
            String lastLogin = "";
            LBPlayer logblockPlayer = LogBlock.getInstance().getPlayer(region.getOwner());

            if(logblockPlayer != null) {
                lastLogin += " | " + RCMessaging.green("Lastlogin: ") + RCMessaging.yellow(logblockPlayer.getLastlogin());
            }

            RCMessaging.send(player, "| " + RCMessaging.green("Besitzer: ") + RCMessaging.yellow(region.getOwner())
                    + lastLogin, false);
            RCMessaging.send(player, "| " + RCMessaging.green("Refund: ") + RCMessaging.yellow(regionManager.getRefundPercentage(region) * 100 + "%"), false);
            RCMessaging.send(player, "| " + RCMessaging.green("Aktueller Preis: ") + RCMessaging.yellow(region.getPrice() + ""),false);
            RCMessaging.send(player, "| " + RCMessaging.green("Basis Preis: ") + RCMessaging.yellow(region.getBasePrice() + ""),false);
            RCMessaging.send(player, "| " + RCMessaging.green("Rückzahlung: ") + RCMessaging.yellow(regionManager.getRefundValue(region) + ""),false);

	        if (region.hasWarnings()) {
		        RCMessaging.send(player, "| " + RCMessaging.green("Verwarnungen: "), false);
		        Collection<RegionWarning> warnings = region.getWarnings();
		        for (RegionWarning warning : warnings) {
			        RCMessaging.send(player, ChatColor.YELLOW + "[" + ChatColor.GREEN + warning.getId() + ChatColor.YELLOW + "] " +
							        ChatColor.RED + warning.getMessage(), false);
		        }
	        }

            List<RegionLog> regionHistory = RegionsDatabase.getInstance().getTable(LogTable.class).getHistory(region.getName());
            if(regionHistory.size() > 0) {
                RCMessaging.send(player, "| " + RCMessaging.green("Grundstücks-Historie:"),false);
                for(RegionLog log : regionHistory) {
                    RCMessaging.send(player, "| " 
                            + RCMessaging.yellow(log.getPlayer()) 
                            + RCMessaging.green(" - ")
                            + RCMessaging.yellow(log.getAction())
                            + RCMessaging.green(" - ")
                            + RCMessaging.yellow(log.getPrice() + "c + " + log.getTax() + "c")
                            + RCMessaging.green(" - ")
                            + RCMessaging.yellow(log.getTime())
                            ,false);
                }
            }
        }
    }

    private void buyRegion(String region) {
        try {
            buyRegion(RegionManager.getInstance().getRegion(region));
        } catch (UnknownRegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }
    
    private void buyRegion() {
        try {
            Player player = cmd.getPlayerOfSender(sender);
            Location location = player.getLocation();
            buyRegion(RegionManager.getInstance().getRegion(location));
        } catch (UnknownRegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }

    private void buyRegion(Region region) {
        try {
            Player player = cmd.getPlayerOfSender(sender);
            RegionManager regionManager = RegionManager.getInstance();
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
            dropRegion(RegionManager.getInstance().getRegion(region));
        } catch (UnknownRegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }
    
    private void dropRegion() {
        try {
            Player player = cmd.getPlayerOfSender(sender);
            Region region = RegionManager.getInstance().getRegion(player.getLocation());
            dropRegion(region);
        } catch (UnknownRegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }

    private void dropRegion(Region region) {
        Player player = cmd.getPlayerOfSender(sender);
        if ((region.getOwner() != null && region.getOwner().equalsIgnoreCase(player.getName())) || sender.hasPermission("rcregions.admin")) {
            try {
                RegionManager.getInstance().dropRegion(player, region);
                double refund = RegionManager.getInstance().getRefundValue(region);
                RCMessaging.send(sender, "Deine Region " + region.getName() + " wurde für " +
                        RCMessaging.yellow(refund + "") + " Coins an den Server verkauft.");
                RegionsDatabase.getInstance().getTable(LogTable.class).logAction(new RegionLog(player.getName()
                        , region.getName()
                        , Enums.Action.DROP
                        , refund
                        , 0));
            } catch (RegionException e) {
                RCMessaging.warn(sender, e.getMessage());
            }
        } else {
            RCMessaging.noPermission(player);
        }
    }
}
