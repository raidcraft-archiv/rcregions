package com.raidcraft.rcregions.commands;

import com.raidcraft.rcregions.District;
import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.exceptions.PlayerException;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.BlockVector;
import de.raidcraft.RaidCraft;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 17.12.11 - 11:30
 *
 * @author Silthus
 */
public class RegionCommand {

    private RegionsPlugin plugin;

    public RegionCommand(RegionsPlugin plugin) {

        this.plugin = plugin;
    }

    @Command(
            aliases = {"rcr", "region"},
            desc = "Region Commands"
    )
    @NestedCommand(value = NestedCommands.class, executeBody = true)
    public void nested(CommandContext args, CommandSender sender) {

        Player player = (Player) sender;
        List<Region> regions = plugin.getRegionManager().getPlayerRegions(player);
        Set<District> uniqueDistricts = new HashSet<>();
        Map<String, District> districts = plugin.getDistrictManager().getDistricts();
        for (Region region : regions) {
            uniqueDistricts.add(region.getDistrict());
        }
        player.sendMessage(ChatColor.YELLOW + "|---------- " + ChatColor.GREEN + "Raid-Craft.de" + ChatColor.YELLOW + " -----------|");
        player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Regionen: " + ChatColor.YELLOW + regions.size() + " | "
                + ChatColor.GREEN + "Distrikte: " + ChatColor.YELLOW + uniqueDistricts.size() + "/" + districts.size());
        for (District district : uniqueDistricts) {
            ArrayList<String> list = new ArrayList<>();
            for (Region region : plugin.getRegionManager().getPlayerRegions(player, district)) {
                list.add(region.toString());
            }
            player.sendMessage(ChatColor.YELLOW + "| " + district.toString() + ": " + StringUtil.joinString(list, ", ", 0));
        }
    }

    public static class NestedCommands {

        private final RegionsPlugin plugin;

        public NestedCommands(RegionsPlugin plugin) {

            this.plugin = plugin;
        }

        @Command(
                aliases = {"warp"},
                desc = "Warps the region",
                min = 1,
                usage = "<region>"
        )
        @CommandPermissions("rcregions.region.warp")
        public void warp(CommandContext args, CommandSender sender) throws CommandException {

            try {
                Player player = (Player) sender;
                Region region = plugin.getRegionManager().getRegion(args.getString(0));
                BlockVector maximumPoint = region.getRegion().getMaximumPoint();
                Location location = new Location(player.getWorld(), maximumPoint.getX(), maximumPoint.getY(), maximumPoint.getZ());
                player.teleport(location);
                player.sendMessage(ChatColor.YELLOW + "Teleported to Region "
                        + ChatColor.GREEN + region.getName() + ChatColor.YELLOW + " owned by " + ChatColor.GREEN + region.getOwner());
            } catch (UnknownRegionException e) {
                throw new CommandException(e.getMessage());
            }
        }

        @Command(
                aliases = {"info"},
                desc = "Shows the region"
        )
        @CommandPermissions("rcregions.region.info")
        public void showRegionInfo(CommandContext args, CommandSender sender) throws CommandException {

            try {
                Player player = (Player) sender;
                RegionManager regionManager = plugin.getRegionManager();

                Region region;
                if (args.argsLength() > 0) {
                    region = regionManager.getRegion(args.getString(0));
                } else {
                    region = regionManager.getRegion(player.getLocation());
                }
                RegionCommand.showRegionInfo(player, region);
            } catch (UnknownRegionException e) {
                throw new CommandException(e.getMessage());
            }
        }

        @Command(
                aliases = {"buy", "claim"},
                desc = "Buys the region"
        )
        @CommandPermissions("rcregions.region.buy")
        public void buyRegion(CommandContext args, CommandSender sender) throws CommandException {

            Player player = (Player) sender;
            Region region;

            try {
                if (args.argsLength() > 0) {
                    region = plugin.getRegionManager().getRegion(args.getString(0));
                } else {
                    region = plugin.getRegionManager().getRegion(player.getLocation());
                }
                plugin.getRegionManager().buyRegion(player, region);
                sender.sendMessage(ChatColor.GREEN + "Neues Grundstück erworben: " + region.getName());
            } catch (UnknownRegionException | PlayerException | RegionException e) {
                throw new CommandException(e.getMessage());
            }
        }

        @Command(
                aliases = {"drop", "sell"},
                desc = "Sells the region"
        )
        @CommandPermissions("rcregions.region.drop")
        public void dropRegion(CommandContext args, CommandSender sender) throws CommandException {

            Player player = (Player) sender;
            Region region;

            try {
                if (args.argsLength() > 0) {
                    region = plugin.getRegionManager().getRegion(args.getString(0));
                } else {
                    region = plugin.getRegionManager().getRegion(player.getLocation());
                }
                plugin.getRegionManager().dropRegion(player, region);
                double refundValue = plugin.getRegionManager().getRefundValue(region);
                sender.sendMessage(ChatColor.RED + "Deine Region " + region.getName()
                        + " wurde an den Server verkauft. Erlöse: " + RaidCraft.getEconomy().getFormattedAmount(refundValue));
            } catch (UnknownRegionException | RegionException e) {
                throw new CommandException(e.getMessage());
            }
        }
    }

    public static void showRegionInfo(Player player, Region region) {

        RegionManager regionManager = RaidCraft.getComponent(RegionManager.class);
        District district = region.getDistrict();
        player.sendMessage(ChatColor.YELLOW + "|---------- " + ChatColor.GREEN + "Raid-Craft.de" + ChatColor.YELLOW + " -----------|");
        player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Region: " + ChatColor.YELLOW + region.toString() + " | "
                + ChatColor.GREEN + "Distrikt: " + ChatColor.YELLOW + district.toString());

        player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Besitzer: " + ChatColor.YELLOW + region.getOwner());
        player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Refund: " + ChatColor.YELLOW + regionManager.getRefundPercentage(region) * 100 + "%");
        player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Aktueller Preis: " + RaidCraft.getEconomy().getFormattedAmount(region.getPrice()));
        player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Basis Preis: " + RaidCraft.getEconomy().getFormattedAmount(region.getBasePrice()));
        player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Rückzahlung: " + RaidCraft.getEconomy().getFormattedAmount(regionManager.getRefundValue(region)));
    }
}
