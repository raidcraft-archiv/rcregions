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
        try {
            String regionName = RegionUtil.parseRegionName(sign);
            Region region = plugin.getRegionManager().getRegion(regionName);
            // lets check if the player already owns the region
            // if yes toggle the buyable status
            if (region.getOwner().equalsIgnoreCase(event.getPlayer().getName())) {
                if (region.isBuyable()) {
                    event.getPlayer().sendMessage(ChatColor.RED
                            + "Bist du dir sicher, dass du dieses Grundstück nicht mehr zum Verkauf anbieten willst?");
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED
                            + "Bist du dir sicher, dass du dieses Grundstück zum Verkauf anbieten willst?");
                }
                new QueuedCommand(event.getPlayer(), this, "toggleRegionBuyableState", event.getPlayer(), region).run();
            } else if (!region.isBuyable()) {
                event.getPlayer().sendMessage(ChatColor.RED + "Du kannst dieses Grundstück nicht kaufen.");
            } else {
                if (region.getPrice() > 0) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Bist du dir sicher, dass du dieses Grundstück für "
                            + RaidCraft.getEconomy().getFormattedAmount(region.getPrice()) + ChatColor.RED + " kaufen möchtest?");
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "Bist du dir sicher, dass du dieses Grundstück erwerben möchtest?");
                }
                new QueuedCommand(event.getPlayer(), this, "buyRegion", event.getPlayer(), region).run();
            }
        } catch (WrongSignFormatException e) {
            event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
            BlockUtil.destroyBlock(sign.getBlock());
        } catch (UnknownDistrictException | UnknownRegionException e) {
            event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
        } catch (NoSuchMethodException e) {
            RaidCraft.LOGGER.warning(e.getMessage());
            e.printStackTrace();
        }
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE && !event.getPlayer().isSneaking()) {
            event.setCancelled(true);
        }
    }

    public void buyRegion(Player player, Region region) {

        // delegate
        plugin.getRegionManager().buyRegion(player, region);
    }

    public void toggleRegionBuyableState(Player player, Region region) {

        // delegate
        plugin.getRegionManager().toggleRegionBuyableState(player, region);
    }
}
