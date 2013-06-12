package com.raidcraft.rcregions.listeners;

import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.WorldGuardManager;
import com.raidcraft.rcregions.api.Region;
import com.raidcraft.rcregions.commands.RegionCommand;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.exceptions.WrongSignFormatException;
import com.raidcraft.rcregions.util.RegionUtil;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.commands.QueuedCommand;
import de.raidcraft.util.BlockUtil;
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

    private final RegionsPlugin plugin;

    public RCPlayerListener(RegionsPlugin plugin) {

        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        // lets check for the tool item
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null && event.getItem().getTypeId() == plugin.getMainConfig().tool_id) {
            // show the region information and worldguard stuff
            if (player.hasPermission("rcregions.info")) {
                try {
                    Region region = plugin.getRegionManager().getRegion(event.getClickedBlock().getLocation());
                    RegionCommand.showRegionInfo(player, region);
                } catch (UnknownRegionException e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    if (player.hasPermission("rcregions.info.worldguard")) {
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
            return;
        }
        if (!SignUtil.isSign(event.getClickedBlock())) {
            return;
        }
        Sign sign = SignUtil.getSign(event.getClickedBlock());
        // check for a region sign
        if (!SignUtil.isLineEqual(sign.getLine(3), plugin.getMainConfig().sign_identitifer)) {
            return;
        }
        try {
            String regionName = RegionUtil.parseRegionName(sign);
            Region region = plugin.getRegionManager().getRegion(regionName);
            // lets check if the player already owns the region
            // if yes toggle the buyable status
            if (region.getOwner() != null && region.getOwner().equalsIgnoreCase(player.getName())) {
                if (region.isBuyable()) {
                    player.sendMessage(ChatColor.RED
                            + "Bist du dir sicher, dass du dieses Grundstück nicht mehr zum Verkauf anbieten willst?");
                } else {
                    player.sendMessage(ChatColor.RED
                            + "Bist du dir sicher, dass du dieses Grundstück zum Verkauf anbieten willst?");
                }
                new QueuedCommand(player, this, "toggleRegionBuyableState", player, region, sign);
            } else if (!region.isBuyable()) {
                player.sendMessage(ChatColor.RED + "Dieses Grundstück steht nicht zum Verkauf.");
            } else {
                if (region.getPrice() > 0) {
                    if (!RaidCraft.getEconomy().hasEnough(player.getName(), region.getPrice())) {
                        player.sendMessage(ChatColor.RED + "Du hast nicht genug Geld um dises Grundstück zu kaufen. " +
                                "Du benötigst " + RaidCraft.getEconomy().getFormattedAmount(region.getPrice()));
                        event.setCancelled(true);
                        return;
                    }
                    player.sendMessage(ChatColor.RED + "Bist du dir sicher, dass du dieses Grundstück für "
                            + RaidCraft.getEconomy().getFormattedAmount(region.getPrice()) + ChatColor.RED + " kaufen möchtest?");
                } else {
                    player.sendMessage(ChatColor.RED + "Bist du dir sicher, dass du dieses Grundstück erwerben möchtest?");
                }
                new QueuedCommand(player, this, "buyRegion", player, region, sign);
            }
            RegionUtil.updateSign(sign, region);
        } catch (WrongSignFormatException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
            BlockUtil.destroyBlock(sign.getBlock());
        } catch (UnknownDistrictException | UnknownRegionException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
        } catch (NoSuchMethodException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
        if (player.getGameMode() == GameMode.CREATIVE && !player.isSneaking()) {
            event.setCancelled(true);
        }
    }

    public void buyRegion(Player player, Region region, Sign sign) {

        // delegate
        plugin.getRegionManager().buyRegion(player, region);
        RegionUtil.updateSign(sign, region);
    }

    public void toggleRegionBuyableState(Player player, Region region, Sign sign) {

        // delegate
        plugin.getRegionManager().toggleRegionBuyableState(player, region);
        RegionUtil.updateSign(sign, region);
    }
}
