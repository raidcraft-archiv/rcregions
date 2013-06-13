package com.raidcraft.rcregions.commands;

import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.WorldGuardManager;
import com.raidcraft.rcregions.api.District;
import com.raidcraft.rcregions.api.Region;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.tables.TRegion;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.BlockVector;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.commands.QueuedCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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

        showPlayerInfo(sender, sender.getName());
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
                aliases = {"regions", "list"},
                desc = "Shows all regions of the given player",
                usage = "[player]"
        )
        @CommandPermissions("rcregions.region.list")
        public void showRegions(CommandContext args, CommandSender sender) throws CommandException {

            if (args.argsLength() > 0 && !sender.hasPermission("rcregions.admin")) {
                throw new CommandException("Du hast keine Rechte dir die Regionen von anderen Spielern anzeigen zu lassen!");
            }
            String player;
            if (args.argsLength() > 0) {
                player = args.getString(0);
                Player bukkitPlayer = Bukkit.getPlayer(player);
                if (bukkitPlayer != null) {
                    player = bukkitPlayer.getName();
                }
            } else {
                player = sender.getName();
            }
            showPlayerInfo(sender, player);
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
                aliases = {"give"},
                desc = "Gives the region to the player",
                min = 1,
                usage = "<player> [region]"
        )
        @CommandPermissions("rcregions.region.give")
        public void giveRegion(CommandContext args, CommandSender sender) throws CommandException {

            Player owner = (Player) sender;
            OfflinePlayer newOwner = Bukkit.getPlayer(args.getString(0));
            // lets check in the db if the player exists or if he is online
            if (newOwner == null) {
                List<TRegion> player = plugin.getDatabase().find(TRegion.class).where().eq("player", args.getString(0)).findList();
                if (player.isEmpty()) {
                    throw new CommandException("Der Spieler muss online sein oder bereits ein Grundstück besitzen.");
                }
                newOwner = Bukkit.getOfflinePlayer(args.getString(0));
            }

            Region region;
            try {
                if (args.argsLength() > 1) {
                    region = plugin.getRegionManager().getRegion(args.getString(1));
                } else {
                    region = plugin.getRegionManager().getRegion(owner.getLocation());
                }
                if (!sender.hasPermission("rcregions.admin") && !region.getOwner().equalsIgnoreCase(owner.getName())) {
                    throw new CommandException("Du musst der Besitzer des Grundstücks sein um es an andere Spieler zu vergeben.");
                }
                new QueuedCommand(sender, this, "giveRegion", newOwner, region);
            } catch (UnknownRegionException | UnknownDistrictException e) {
                throw new CommandException(e.getMessage());
            } catch (NoSuchMethodException e) {
                plugin.getLogger().warning(e.getMessage());
                e.printStackTrace();
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
                if (region.getDistrict().getMaxRegionCount() <= plugin.getRegionManager().getPlayerRegionCount(player, region.getDistrict())) {
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

        public void giveRegion(OfflinePlayer player, Region region) {

            region.claim(player);
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

    public static void showPlayerInfo(CommandSender sender, String player) {

        RegionsPlugin plugin = RaidCraft.getComponent(RegionsPlugin.class);
        List<Region> regions = plugin.getRegionManager().getPlayerRegions(player);
        Set<District> uniqueDistricts = new HashSet<>();
        Map<String, District> districts = plugin.getDistrictManager().getDistricts();
        for (Region region : regions) {
            uniqueDistricts.add(region.getDistrict());
        }
        sender.sendMessage(ChatColor.YELLOW + "|---------- " + ChatColor.GREEN + "Raid-Craft.de" + ChatColor.YELLOW + " -----------|");
        sender.sendMessage(ChatColor.YELLOW + "| " + ChatColor.GREEN + "Regionen: " + ChatColor.YELLOW + regions.size() + " | "
                + ChatColor.GREEN + "Distrikte: " + ChatColor.YELLOW + uniqueDistricts.size() + "/" + districts.size());
        for (District district : uniqueDistricts) {
            ArrayList<String> list = new ArrayList<>();
            for (Region region : plugin.getRegionManager().getPlayerRegions(player, district)) {
                list.add(region.toString());
            }
            sender.sendMessage(ChatColor.YELLOW + "| " + district.toString() + ": " + StringUtil.joinString(list, ", ", 0));
        }
    }
}
