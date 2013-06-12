package com.raidcraft.rcregions.listeners;

import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.api.Region;
import com.raidcraft.rcregions.exceptions.UnknownDistrictException;
import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import com.raidcraft.rcregions.exceptions.WrongSignFormatException;
import com.raidcraft.rcregions.util.RegionUtil;
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
import org.bukkit.event.player.PlayerInteractEvent;

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

        if (!SignUtil.isSign(event.getClickedBlock())) {
            return;
        }
        Sign sign = SignUtil.getSign(event.getClickedBlock());
        // check for a region sign
        if (!SignUtil.isLineEqual(sign.getLine(3), plugin.getMainConfig().sign_identitifer)) {
            return;
        }
        Player player = event.getPlayer();
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
                new QueuedCommand(player, this, "toggleRegionBuyableState", player, region, sign).run();
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
                new QueuedCommand(player, this, "buyRegion", player, region, sign).run();
            }
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
