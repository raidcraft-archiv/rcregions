package com.raidcraft.rcregions.commands;

import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.WorldGuardManager;
import com.raidcraft.rcregions.api.District;
import com.raidcraft.rcregions.api.Region;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldedit.BlockVector;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.commands.QueuedCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
    @NestedCommand(value = NestedCommands.class)
    public void nested(CommandContext args, CommandSender sender) {

    }

    public static class NestedCommands {

        private final RegionsPlugin plugin;

        public NestedCommands(RegionsPlugin plugin) {

            this.plugin = plugin;
        }

        @Command(
                aliases = {"reload"},
                desc = "Reloads the plugin"
        )
        @CommandPermissions("rcregions.reload")
        public void reload(CommandContext args, CommandSender sender) {

            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "Es wurden alle Regionen und Distrikte erfolgreich neugeladen.");
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
                BlockVector maximumPoint = WorldGuardManager.getRegion(region.getName()).getMaximumPoint();
                Location location = new Location(player.getWorld(), maximumPoint.getX(), maximumPoint.getY(), maximumPoint.getZ());
                player.teleport(location);
                player.sendMessage(ChatColor.YELLOW + "Teleported to Region "
                        + ChatColor.GREEN + region.getName() + ChatColor.YELLOW + " owned by " + ChatColor.GREEN + region.getOwner());
            } catch (UnknownRegionException | UnknownDistrictException e) {
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
            } catch (UnknownRegionException | UnknownDistrictException e) {
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
                if (!region.isBuyable() || (region.getOwner() != null && region.getOwner().equalsIgnoreCase(sender.getName()))) {
                    throw new CommandException("Du kannst diese Region nicht kaufen.");
                }
                if (region.getDistrict().getMaxRegionCount() >= plugin.getRegionManager().getPlayerRegionCount(player, region.getDistrict())) {
                    throw new CommandException("Du hast bereits die maximale Anzahl an Grundstücken in diesem Distrikt erworben.");
                }
                if (region.getPrice() > 0) {
                    if (!RaidCraft.getEconomy().hasEnough(player.getName(), region.getPrice())) {
                        throw new CommandException(ChatColor.RED + "Du hast nicht genug Geld um dises Grundstück zu kaufen. " +
                                "Du benötigst " + RaidCraft.getEconomy().getFormattedAmount(region.getPrice()));
                    }
                    player.sendMessage(ChatColor.RED + "Bist du dir sicher, dass du dieses Grundstück für "
                            + RaidCraft.getEconomy().getFormattedAmount(region.getPrice()) + ChatColor.RED + " kaufen möchtest?");
                } else {
                    player.sendMessage(ChatColor.RED + "Bist du dir sicher, dass du dieses Grundstück erwerben möchtest?");
                }
                new QueuedCommand(player, this, "buyRegion", player, region);
            } catch (RegionException e) {
                throw new CommandException(e.getMessage());
            } catch (NoSuchMethodException e) {
                plugin.getLogger().warning(e.getMessage());
                e.printStackTrace();
            }
        }

        @Command(
                aliases = {"drop", "sell"},
                desc = "Drops the Region making it available"
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
                if (!sender.hasPermission("rcregions.admin") &&
                        (region.getOwner() == null || !region.getOwner().equalsIgnoreCase(sender.getName()))) {
                    throw new CommandException("Du bist nicht der Besitzer dieser Region.");
                }
                new QueuedCommand(player, this, "dropRegion", player, region);
            } catch (RegionException e) {
                throw new CommandException(e.getMessage());
            } catch (NoSuchMethodException e) {
                plugin.getLogger().warning(e.getMessage());
                e.printStackTrace();
            }
        }

        public void buyRegion(Player player, Region region) {

            // delegate
            plugin.getRegionManager().buyRegion(player, region);
        }

        public void dropRegion(Player player, Region region) {

            // delegate
            plugin.getRegionManager().dropRegion(player, region);
        }
    }

    public static void showRegionInfo(Player player, Region region) {

        District district = region.getDistrict();
        player.sendMessage(ChatColor.YELLOW + "|---------- " + ChatColor.GREEN + "Raid-Craft.de" + ChatColor.YELLOW + " -----------|");
        player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Region: " + ChatColor.YELLOW + region.toString() + " | "
                + ChatColor.GREEN + "Distrikt: " + ChatColor.YELLOW + district.toString());

        player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Besitzer: " + ChatColor.YELLOW + region.getOwner());
        player.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Preis: " + RaidCraft.getEconomy().getFormattedAmount(region.getPrice()));
    }
}
