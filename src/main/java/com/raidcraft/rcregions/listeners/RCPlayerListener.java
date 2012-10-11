package com.raidcraft.rcregions.listeners;

import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.RegionWarning;
import com.raidcraft.rcregions.WorldGuardManager;
import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.commands.RegionCommand;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.database.RegionsDatabase;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.spout.SpoutRegionBuy;
import com.silthus.raidcraft.bukkit.BukkitBasePlugin;
import com.silthus.raidcraft.util.RCMessaging;
import com.silthus.raidcraft.util.SignUtils;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.getspout.spoutapi.player.SpoutPlayer;

import java.util.*;

/**
 * 21.01.12 - 19:09
 *
 * @author Silthus
 */
public class RCPlayerListener implements Listener {

	private static Map<String, List<RegionWarning>> warnedPlayers = new HashMap<String, List<RegionWarning>>();
    // 20 ticks is one second and we want a 10 second delay
    private static final long DELAY = 20 * 10;
	private static boolean taskIsRunning = false;
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getPlayer().getItemInHand().getType().getId() == MainConfig.get().getToolId()) {
                if (player.hasPermission("rcregions.info")) {
                    try {
                        Region region = RegionManager.getInstance().getRegion(event.getClickedBlock().getLocation());
                        RegionCommand.showRegionInfo(player, region);
                    } catch (UnknownRegionException e) {
                        RCMessaging.warn(player, e.getMessage());
                        //print region info
                        ApplicableRegionSet applicableRegionSet = WorldGuardManager.getLocalRegions(event.getClickedBlock().getLocation());
                        if(applicableRegionSet.size() != 0) {
                            RCMessaging.send(player, "| " + "---------------------------------------", false);
                            RCMessaging.send(player, "| " + RCMessaging.green("WorldGuard Regions Informationen:"), false);
                            for(ProtectedRegion region : applicableRegionSet) {
                                RCMessaging.send(player, "| " + "---------------------------------------", false);
                                RCMessaging.send(player, "| " + RCMessaging.green("ID: ") + ChatColor.GOLD + (region.getId()), false);
                                if(region.getOwners().size() > 0) {
                                    RCMessaging.send(player, "| " + RCMessaging.green("Owner: ") + RCMessaging.yellow(region.getOwners().toUserFriendlyString()), false);
                                }
                                if(region.getMembers().size() > 0) {
                                RCMessaging.send(player, "| " + RCMessaging.green("Member: ") + RCMessaging.yellow(region.getMembers().toUserFriendlyString()), false);
                                }
                                    String flags = "";
                                for(Map.Entry<Flag<?>, Object> flag : region.getFlags().entrySet()) {
                                    if(flags.length() > 0)
                                        flags += ChatColor.WHITE + ", ";
                                    flags += ChatColor.GOLD + flag.getKey().getName() + ": " + ChatColor.YELLOW + flag.getValue().toString();
                                }
                                if(flags.length() > 0) {
                                    RCMessaging.send(player, "| " + RCMessaging.green("Flags: ") + flags, false);
                                }
                            }
                            RCMessaging.send(player, "| " + "---------------------------------------", false);
                        }
                    }
                }
            }
            if (SignUtils.isSign(event.getClickedBlock())) {
                Sign sign = SignUtils.getSign(event.getClickedBlock());
                if (SignUtils.equals(sign.getLine(3), "[" + MainConfig.get().getSignIdentifier() + "]")) {
                    try {
                        Region region = RegionManager.getInstance().getRegion(ChatColor.stripColor(sign.getLine(1).replaceAll("Region: ", "")));
                        String owner = region.getOwner();
                        if (owner == null) owner = "";
                        if (!(player.hasPermission("rcregions.admin")) && owner.equalsIgnoreCase("")) {
                            RCMessaging.send(player, "Dieses Grundstück wird vom Server verwaltet.");
                            event.setCancelled(true);
                            return;
                        }
                        if ((player.hasPermission("rcregions.admin")) ||
                                (player.hasPermission("rcregions.region.sell") && owner.equalsIgnoreCase(player.getName()))) {
                            if (region.isBuyable()) {
                                region.setBuyable(false);
                                RCMessaging.send(player, "Die Region kann nun nicht mehr gekauft werden.");
                            } else {
                                region.setBuyable(true);
                                RCMessaging.send(player, "Die Region kann nun gekauft werden.");
                            }
                            RegionManager.getInstance().updateSign(sign, region);
                        } else {
                            RCMessaging.noPermission(player);
                        }
                    } catch (UnknownRegionException e) {
                        RCMessaging.warn(player, e.getMessage());
                    }
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && SignUtils.isSign(event.getClickedBlock())) {
            Sign sign = SignUtils.getSign(event.getClickedBlock());
            if (SignUtils.equals(sign.getLine(3), "[" + MainConfig.get().getSignIdentifier() + "]")) {
                if (player.getGameMode() == GameMode.CREATIVE && !player.isSneaking()) {
                    event.setCancelled(true);
                }
                try {
                    Region region = RegionManager.getInstance().getRegion(ChatColor.stripColor(sign.getLine(1).replaceAll("Region: ", "")));
                    RegionManager.getInstance().updateSign(sign, region);
                    if (region.isBuyable()) {
                        if (BukkitBasePlugin.isSpoutEnabled() && ((SpoutPlayer)player).isSpoutCraftEnabled()){
                            new SpoutRegionBuy(player, region);
                        }
                        else
                        {
                        double price = RegionManager.getInstance().getFullPrice(player, region);
                        RCMessaging.send(player, "Gebe \"/rcr -b " + region.getName() + "\" ein um die Region zu kaufen.");
                        RCMessaging.send(player, "Grundpreis: " + region.getBasePrice());
                        RCMessaging.send(player, "Preis inkl. Steuern("
                                + RCMessaging.green(RegionManager.getInstance().getTaxes(player, region) * 100 + "%") + "): " + price);
                        }
                    }
                } catch (UnknownRegionException e) {
                    RCMessaging.warn(player, e.getMessage());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (warnedPlayers.containsKey(player)) {
            warnedPlayers.remove(player);
        }
    }

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		List<RegionWarning> warnings = new ArrayList<RegionWarning>();
		for (Region region : RegionManager.getInstance().getPlayerRegions(player)) {
			if (region.hasWarnings()) {
				warnings.addAll(region.getWarnings());
			}
		}
		if (warnings.size() > 0) {
			warnedPlayers.put(player.getName(), warnings);
		}
	}

	public static void addWarning(RegionWarning warning) {

		Player player = Bukkit.getPlayer(warning.getRegion().getOwner());
		if (player != null && player.isOnline()) {
			if (warnedPlayers.containsKey(player.getName())) {
				warnedPlayers.get(player.getName()).add(warning);
			}
		}
	}

	public static void removeWarning(RegionWarning warning) {

		Player player = Bukkit.getPlayer(warning.getRegion().getOwner());
		if (player != null && player.isOnline()) {
			if (warnedPlayers.containsKey(player.getName())) {
				warnedPlayers.get(player.getName()).remove(warning);
			}
			if (warnedPlayers.get(player.getName()).size() < 1) {
				warnedPlayers.remove(player.getName());
			}
		}
	}

    public static void startWarningTask() {
        if (taskIsRunning) {
            return;
        }
	    long interval = 20 * MainConfig.get().getWarnInterval();
	    Bukkit.getScheduler().scheduleSyncRepeatingTask(RegionsPlugin.get(), new Runnable() {
		    @Override
		    public void run() {
			    Set<Map.Entry<String, List<RegionWarning>>> entries =
					    new HashSet<Map.Entry<String, List<RegionWarning>>>(warnedPlayers.entrySet());
			    if (entries.size() > 0) {
				    for (Player player : Bukkit.getOnlinePlayers()) {
					    if (player.hasPermission("rcregions.warn.list")) {
						    player.sendMessage(ChatColor.GRAY + "Aktuell stehen " + RegionsDatabase.getWarningCount() + " Regions " +
								    "Verwarnungen aus. (" + ChatColor.ITALIC + "/rcr listwarnings" + ChatColor.RESET + ChatColor.GRAY + ")");
					    }
				    }
			    }
			    for (Map.Entry<String, List<RegionWarning>> entry : entries) {
				    Player player = Bukkit.getPlayer(entry.getKey());
				    if (player != null && player.isOnline()) {
					    player.sendMessage(ChatColor.RED + "Folgende Regionen von dir wurden verwarnt:");
					    for (RegionWarning warning : entry.getValue()) {
						    player.sendMessage(
								    ChatColor.YELLOW + "[" + ChatColor.GREEN + warning.getId() + ChatColor.YELLOW + "]" +
										    "[" + ChatColor.AQUA + warning.getRegion().getName() + ChatColor.YELLOW + "]" +
										    ChatColor.GREEN + " - " + ChatColor.YELLOW +
										    RegionWarning.DATE_FORMAT.format(new Date(warning.getTime()))
										    + ChatColor.GREEN + " - " + ChatColor.RED + warning.getMessage());
					    }
				    }
			    }
		    }
	    }, DELAY, interval);
        taskIsRunning = true;
    }
}
