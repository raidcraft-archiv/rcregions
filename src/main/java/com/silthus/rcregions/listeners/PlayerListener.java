/*
 * Copyright (C) 2011 RaidCraft <http://www.raid-craft.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.silthus.rcregions.listeners;

import com.silthus.raidcraft.util.RCMessaging;
import com.silthus.raidcraft.util.RCUtils;
import com.silthus.rcregions.RCRegionManager;
import com.silthus.rcregions.Region;
import com.silthus.rcregions.UnknownRegionException;
import com.silthus.rcregions.bukkit.RCRegionsPlugin;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.silthus.raidcraft.util.RCUtils.getSign;
import static com.silthus.rcregions.util.RegionUtils.isRegionSign;

/**
 *
 * 30.09.11 - 15:39
 * @author Silthus
 */
public class PlayerListener extends org.bukkit.event.player.PlayerListener {

    private final RCRegionsPlugin plugin;

    
    public PlayerListener(RCRegionsPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // check if the clicked block is a region sign
            if (isRegionSign(event.getClickedBlock())) {
                buyRegion(event);
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // update the sign and print information about the region
            if (isRegionSign(event.getClickedBlock())) {
                printRegionInfo(event);
            }
        }
    }

    /**
     *
     * @param event
     */
    private void printRegionInfo(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Sign sign = getSign(event.getClickedBlock());
        try {
            Region region = RCRegionManager.getRegion(sign.getLine(1));
            RCMessaging.send(player, region.toDetailedString());
            
        } catch (UnknownRegionException e) {
            RCMessaging.send(player, RCMessaging.red(e.getMessage()));
            RCUtils.destroyBlock(sign.getBlock());
        }

    }

    /**
     * Tries to buy the region for the player
     * @param event BlockRightClick Event
     */
    private void buyRegion(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Sign sign = getSign(event.getClickedBlock());
        try {
            // get the region
            Region region = RCRegionManager.getRegion(sign.getLine(1));
            // try to buy the region and abort if player has not enough money
            if (RCRegionManager.buyRegion(player, region)) {
                // update the region sign
                RCUtils.updateRegionSign(sign);
                RCMessaging.send(player, "You just bought " + RCMessaging.yellow(region.getId())
                    + " for " + RCMessaging.yellow(region.getPrice() + "") + " Coins.");
            }
            RCMessaging.send(player, "You dont have enough Coins to buy this region. " +
                    "You need " + RCRegionManager.getTotalRegionCost(player, region));
        } catch (UnknownRegionException e) {
            RCMessaging.send(player, RCMessaging.red(e.getMessage()));
            RCUtils.destroyBlock(sign.getBlock());
        }
    }
}
