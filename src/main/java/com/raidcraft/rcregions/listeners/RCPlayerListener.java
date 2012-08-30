package com.raidcraft.rcregions.listeners;

import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.WorldGuardManager;
import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.commands.RegionCommand;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.spout.SpoutRegionBuy;
import com.silthus.raidcraft.util.RCMessaging;
import com.silthus.raidcraft.util.SignUtils;
import com.silthus.raidcraft.util.Task;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 21.01.12 - 19:09
 *
 * @author Silthus
 */
public class RCPlayerListener implements Listener {

    private static Set<Player> warnedPlayers = new HashSet<Player>();
    // 20 ticks is one second and we want a 10 second delay
    private static final long DELAY = 20 * 10;
    // 20 ticks is one second and we want a five minute interval
    private static long INTERVAL;
    private static boolean taskIsRunning = false;
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getPlayer().getItemInHand().getType().getId() == MainConfig.get().getToolId()) {
                if (player.hasPermission("rcregions.info")) {
                    try {
                        Region region = RegionManager.get().getRegion(event.getClickedBlock().getLocation());
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
                        Region region = RegionManager.get().getRegion(ChatColor.stripColor(sign.getLine(1).replaceAll("Region: ", "")));
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
                            RegionManager.get().updateSign(sign, region);
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
                    Region region = RegionManager.get().getRegion(ChatColor.stripColor(sign.getLine(1).replaceAll("Region: ", "")));
                    RegionManager.get().updateSign(sign, region);
                    if (region.isBuyable()) {
                        if (RegionsPlugin.get().isSpoutEnabled() && ((SpoutPlayer)player).isSpoutCraftEnabled()){
                            new SpoutRegionBuy(player, region);
                        }
                        else
                        {
                        double price = RegionManager.get().getFullPrice(player, region);
                        RCMessaging.send(player, "Gebe \"/rcr -b " + region.getName() + "\" ein um die Region zu kaufen.");
                        RCMessaging.send(player, "Grundpreis: " + region.getBasePrice());
                        RCMessaging.send(player, "Preis inkl. Steuern("
                                + RCMessaging.green(RegionManager.get().getTaxes(player, region) * 100 + "%") + "): " + price);
                        }
                    }
                } catch (UnknownRegionException e) {
                    RCMessaging.warn(player, e.getMessage());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RCPlayerListener.startWarningTask();
        Player player = event.getPlayer();
        if (RegionManager.get().hasWarnedRegions(player) && !warnedPlayers.contains(player)) {
            warnedPlayers.add(player);
        } else if (warnedPlayers.contains(player)) {
            warnedPlayers.remove(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (warnedPlayers.contains(player)) {
            warnedPlayers.remove(player);
        }
    }

    public static void startWarningTask() {
        if (taskIsRunning) {
            return;
        }
        INTERVAL = 20 * MainConfig.get().getWarnInterval();
        Task task = new Task(RegionsPlugin.get(), new Warning()) {

            @Override
            public void run() {
                Warning warning = (Warning) getArg(0);
                warning.issue();
            }
        };
        task.startRepeating(DELAY, INTERVAL, false);
        taskIsRunning = true;
    }

    public static class Warning {

        public Warning() {
        }
        
        public synchronized void issue() {
            for (Player player : warnedPlayers) {
                RCMessaging.warn(player, "Ein Grundstück von dir wurde verwarnt! Gebe /rcr für Details ein.");
            }
        }
    }
}
