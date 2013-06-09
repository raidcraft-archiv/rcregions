package com.raidcraft.rcregions.listeners;

import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.WorldGuardManager;
import com.raidcraft.rcregions.commands.RegionCommand;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.util.RegionUtil;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.raidcraft.RaidCraft;
import de.raidcraft.util.SignUtil;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

/**
 * 21.01.12 - 19:09
 *
 * @author Silthus
 */
public class RCPlayerListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        RegionsPlugin plugin = RaidCraft.getComponent(RegionsPlugin.class);
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getPlayer().getItemInHand().getType().getId() == plugin.getMainConfig().tool_id) {
                if (player.hasPermission("rcregions.info")) {
                    try {
                        Region region = RaidCraft.getComponent(RegionManager.class).getRegion(event.getClickedBlock().getLocation());
                        RegionCommand.showRegionInfo(player, region);
                    } catch (UnknownRegionException e) {
                        player.sendMessage(ChatColor.RED + e.getMessage());
                        //print region info
                        ApplicableRegionSet applicableRegionSet = WorldGuardManager.getLocalRegions(event.getClickedBlock().getLocation());
                        if (applicableRegionSet.size() != 0) {
                            player.sendMessage(ChatColor.YELLOW + "| " + "---------------------------------------");
                            player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "WorldGuard Regions Informationen:");
                            for (ProtectedRegion region : applicableRegionSet) {
                                player.sendMessage(ChatColor.YELLOW + "| " + "---------------------------------------");
                                player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "ID: " + ChatColor.GOLD + (region.getId()));
                                if (region.getOwners().size() > 0) {
                                    player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Owner: " + ChatColor.YELLOW + region.getOwners().toUserFriendlyString());
                                }
                                if (region.getMembers().size() > 0) {
                                    player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Member: " + ChatColor.YELLOW + region.getMembers().toUserFriendlyString());
                                }
                                String flags = "";
                                for (Map.Entry<Flag<?>, Object> flag : region.getFlags().entrySet()) {
                                    if (flags.length() > 0)
                                        flags += ChatColor.WHITE + ", ";
                                    flags += ChatColor.GOLD + flag.getKey().getName() + ": " + ChatColor.YELLOW + flag.getValue().toString();
                                }
                                if (flags.length() > 0) {
                                    player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Flags: " + ChatColor.YELLOW + flags);
                                }
                            }
                            player.sendMessage(ChatColor.YELLOW + "| " + "---------------------------------------");
                        }
                    }
                }
            }
            if (SignUtil.isSign(event.getClickedBlock())) {
                Sign sign = SignUtil.getSign(event.getClickedBlock());
                if (SignUtil.isLineEqual(sign.getLine(3), "[" + plugin.getMainConfig().sign_identitifer + "]")) {
                    try {
                        String regionName = RegionUtil.parseRegionName(sign);
                        Region region = plugin.getRegionManager().getRegion(regionName);
                        String owner = region.getOwner();
                        if (owner == null) owner = "";
                        if (!(player.hasPermission("rcregions.admin")) && owner.equalsIgnoreCase("")) {
                            player.sendMessage(ChatColor.RED + "Dieses Grundstück wird vom Server verwaltet.");
                            event.setCancelled(true);
                            return;
                        }
                        if ((player.hasPermission("rcregions.admin")) ||
                                (player.hasPermission("rcregions.region.sell") && owner.equalsIgnoreCase(player.getName()))) {
                            if (region.isBuyable()) {
                                region.setBuyable(false);
                                player.sendMessage(ChatColor.YELLOW + "Die Region kann nun nicht mehr gekauft werden.");
                            } else {
                                region.setBuyable(true);
                                player.sendMessage(ChatColor.YELLOW + "Die Region kann nun gekauft werden.");
                            }
                            plugin.getRegionManager().updateSign(sign, region);
                        } else {
                            player.sendMessage(ChatColor.RED + "Du hast dazu keine Rechte!");
                        }
                    } catch (UnknownRegionException e) {
                        player.sendMessage(ChatColor.RED + e.getMessage());
                    }
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && SignUtil.isSign(event.getClickedBlock())) {
            Sign sign = SignUtil.getSign(event.getClickedBlock());
            if (SignUtil.isLineEqual(sign.getLine(3), "[" + plugin.getMainConfig().sign_identitifer + "]")) {
                if (player.getGameMode() == GameMode.CREATIVE && !player.isSneaking()) {
                    event.setCancelled(true);
                }
                try {
                    String regionName = RegionUtil.parseRegionName(sign);
                    Region region = plugin.getRegionManager().getRegion(regionName);
                    plugin.getRegionManager().updateSign(sign, region);
                    if (region.isBuyable()) {
                        double price = plugin.getRegionManager().getFullPrice(player, region);
                        player.sendMessage(ChatColor.YELLOW + "Gebe \"/rcr -b " + region.getName() + "\" ein um die Region zu kaufen.");
                        player.sendMessage(ChatColor.YELLOW + "Grundpreis: " + RaidCraft.getEconomy().getFormattedAmount(region.getBasePrice()));
                        player.sendMessage(ChatColor.YELLOW + "Preis inkl. Steuern("
                                + ChatColor.GREEN + plugin.getRegionManager().getTaxes(player, region) * 100 + "%" + ChatColor.YELLOW + "): " + RaidCraft.getEconomy().getFormattedAmount(price));
                    }
                } catch (UnknownRegionException e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                }
            }
        }
    }
}
