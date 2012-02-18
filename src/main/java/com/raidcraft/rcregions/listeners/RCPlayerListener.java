package com.raidcraft.rcregions.listeners;

import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.config.MainConfig;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.silthus.raidcraft.util.RCMessaging;
import com.silthus.raidcraft.util.SignUtils;
import com.silthus.raidcraft.util.Task;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 21.01.12 - 19:09
 *
 * @author Silthus
 */
public class RCPlayerListener implements Listener {

    private static Map<String, Task> _taskList = new HashMap<String, Task>();
    // 20 ticks is one second and we want a 10 second delay
    private static final long DELAY = 20 * 10;
    // 20 ticks is one second and we want a five minute interval
    private static final long INTERVAL = 20 * 60 * 5;
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && SignUtils.isSign(event.getClickedBlock())) {
            Sign sign = SignUtils.getSign(event.getClickedBlock());
            if (SignUtils.equals(sign.getLine(3), "[" + MainConfig.getSignIdentifier() + "]")) {
                try {
                    Region region = RegionManager.get().getRegion(ChatColor.stripColor(sign.getLine(1).replaceAll("Region: ", "")));
                    String owner = region.getOwner();
                    if (owner == null) owner = "";
                    if ( !(player.hasPermission("rcregions.admin")) && owner.equalsIgnoreCase("")) {
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
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && SignUtils.isSign(event.getClickedBlock())) {
            Sign sign = SignUtils.getSign(event.getClickedBlock());
            if (SignUtils.equals(sign.getLine(3), "[" + MainConfig.getSignIdentifier() + "]")) {
                if (player.getGameMode() == GameMode.CREATIVE && !player.isSneaking()) {
                    event.setCancelled(true);
                }
                try {
                    Region region = RegionManager.get().getRegion(ChatColor.stripColor(sign.getLine(1).replaceAll("Region: ", "")));
                    RegionManager.get().updateSign(sign, region);
                    if (region.isBuyable()) {
                        double price = RegionManager.get().getFullPrice(player, region);
                        RCMessaging.send(player, "Gebe \"/rcr -b " + region.getName() + "\" ein um die Region zu kaufen.");
                        RCMessaging.send(player, "Grundpreis: " + region.getBasePrice());
                        RCMessaging.send(player, "Preis inkl. Steuern("
                                + RCMessaging.green(RegionManager.get().getTaxes(player, region) * 100 + "%") + "): " + price);
                    }
                } catch (UnknownRegionException e) {
                    RCMessaging.warn(player, e.getMessage());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!(_taskList.containsKey(player.getName())) && RegionManager.get().hasWarnedRegions(player)) {
            List<Region> regions = RegionManager.get().getWarnedRegions(player);
            Task task = new Task(RegionsPlugin.get(), new Warning(player, regions)) {

                @Override
                public void run() {
                    Warning warning = (Warning) getArg(0);
                    warning.issue();
                }
            };
            _taskList.put(player.getName(), task);
            task.startRepeating(DELAY, INTERVAL, false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String name = event.getPlayer().getName();
        if (_taskList.containsKey(name)) {
            Task task = _taskList.get(name);
            task.stop();
            _taskList.remove(name);
        }
    }

    public class Warning {

        private final Player player;
        private final List<Region> regions;
        
        public Warning(Player player, List<Region> regions) {
            this.player = player;
            this.regions = regions;
        }
        
        public synchronized void issue() {
            for (Region region : regions) {
                RCMessaging.send(player, RCMessaging.red("Dein Grundstück " + region.getName() + " wurde verwarnt! Bitte verschönere es..."));
            }
        }
    }
}
