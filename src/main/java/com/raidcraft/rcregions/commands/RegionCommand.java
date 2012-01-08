package com.raidcraft.rcregions.commands;

import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.exceptions.PlayerException;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.silthus.raidcraft.util.RCCommandManager;
import com.silthus.raidcraft.util.RCMessaging;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 17.12.11 - 11:30
 *
 * @author Silthus
 */
public class RegionCommand implements CommandExecutor {

    private final RCCommandManager cmd = new RCCommandManager();
    private CommandSender sender;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmdLabel, String[] args) {
        String label;
        this.sender = sender;
        if (args.length > 0) {
            label = args[0];
            // buys or claims the region the player is standing on
            // [/rcr claim]
            if (cmd.is(label, "buy", "claim", "-b")) {
                if (sender.hasPermission("rcregions.region.buy") && sender instanceof Player) {
                    buyRegion();
                } else {
                    RCMessaging.noPermission(sender);
                }
            }
        }
        return true;
    }

    private void buyRegion() {
        try {
            Player player = cmd.getPlayerOfSender(sender);
            Location location = player.getLocation();
            RegionManager regionManager = RegionManager.get();
            Region region = regionManager.getRegion(location);
            regionManager.buyRegion(player, region);
        } catch (UnknownRegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        } catch (RegionException e) {
            RCMessaging.warn(sender, e.getMessage());
        } catch (PlayerException e) {
            RCMessaging.warn(sender, e.getMessage());
        }
    }
}
