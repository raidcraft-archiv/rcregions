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

import com.silthus.raidcraft.util.RCLogger;
import com.silthus.raidcraft.util.RCMessaging;
import com.silthus.raidcraft.util.RCUtils;
import com.silthus.rcregions.RCRegionManager;
import com.silthus.rcregions.Region;
import com.silthus.rcregions.bukkit.RCRegionsPlugin;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import static com.silthus.raidcraft.util.RCUtils.isSign;
import static com.silthus.rcregions.util.RegionUtils.isRegionSign;

/**
 *
 * 29.09.11 - 17:14
 * @author Silthus
 */
public class BlockListener extends org.bukkit.event.block.BlockListener {

    private final RCRegionsPlugin plugin;

    public BlockListener(RCRegionsPlugin instance) {
        this.plugin = instance;
    }

    /**
     * This event is fired when someone places or changes a sign
     * @param event fired on sign change
     */
    @Override
    public void onSignChange(SignChangeEvent event) {
        // check the first line for the unique marker
        if (isRegionSign(event.getLine(0))) {
            RCLogger.debug("Region sign found! Canceling event... " + event.toString());
            // cancel the event first and uncancel if everything was a success
            event.setCancelled(true);
            // check the permission
            if (event.getPlayer().hasPermission("rcregions.sign.create")) {
                // creates a new SalesSign
                createSalesSign(event);
                RCLogger.debug("Sales Sign created successfully: " + event.toString());
            } else {
                RCMessaging.noPermission(event.getPlayer());
            }
        }
    }

    /**
     * Cancels the block break event on signs if it is
     * a RCRegions sign and the player is not the owner
     * @param event BlockBreakEvent
     */
    public void onBlockBreak(BlockBreakEvent event) {
        // check if the block is a sign
        if (isSign(event.getBlock())) {
            Sign sign = (Sign) event.getBlock().getState();
            // checks if the first line text matches
            if (isRegionSign(sign)) {
                RCLogger.debug("Region sign found! Canceling event... " + event.toString());
                // cancel the event first and uncancel later
                event.setCancelled(true);
                // destroy sales sign
                destroySalesSign(sign, event);
                RCLogger.debug("Sales Sign destroyed successfully: " + event.toString());
            }
        }
    }

    /**
     * Tries to create a new Sales Sign checking everything
     * @param event SignChangeEvent
     */
    private void createSalesSign(SignChangeEvent event) {
        // our player object
        Player player = event.getPlayer();
        // capture the lines to reuse them later
        String regionName = event.getLine(1);
        String price = event.getLine(2);
        // try to get the given region
        try {
            Region region = RCRegionManager.getRegion(regionName);
            // checks if the one who is placing the sign is the main owner
            if (region.isMainOwner(player.getName()) || player.hasPermission("rcregions.admin")) {
                if (RCUtils.isNumber(price)) {
                    // set a new price to the region
                    region.setPrice(Double.parseDouble(price));
                    RCMessaging.send(player, "The price for " + RCMessaging.yellow(region.getId())
                            + " has beed updated to " + RCMessaging.yellow(region.getPrice() + "") + " Coins.");
                }
                RCLogger.debug("Uncanceling event... " + event.toString() + " and creating sign.");
                // uncancel the event
                event.setCancelled(false);
                // update the sign text
                setSignText(event, region);
                // Ask the player if he really wants to sell this region?
                /*if (!region.isForSale() && RCUtils.askPlayer(player, "Do you want to sell this region?")) {
                    // TODO: implement questionier check for player interact
                }*/
                RCLogger.debug("Creating sign for " + region.toString() + " succeded.");
            } else {
                RCMessaging.send(player, RCMessaging.red("You are not the owner of this region!"));
            }
        } catch (Exception ure) {
            RCMessaging.send(player, RCMessaging.red(ure.getMessage()));
        }
    }

    /**
     * Destroys a Region Sales Sign and asks the user if he
     * wants to stop selling the region
     * @param sign to destroy
     * @param event BlockBreakEvent to cancel
     */
    private void destroySalesSign(Sign sign, BlockBreakEvent event) {
        try {
            Region region = RCRegionManager.getRegion(sign.getLine(1));
            Player player = event.getPlayer();
            if (region.isMainOwner(player.getName()) || player.hasPermission("rcregions.admin")) {
                RCLogger.debug("Uncanceling event... " + event.toString() + " and destroying sign.");
                event.setCancelled(false);
                if (region.isForSale()) {
                    /*if (RCUtils.askPlayer(player, "Do you want to stop selling that region?")) {
                        // TODO: implement questionier check for player interact
                    }*/
                }
            } else {
                RCMessaging.noPermission(player);
            }
        } catch (Exception e) {
            event.setCancelled(false);
        }
    }

    /**
     * Sets the Region Sign Text and updates the sign
     * @param event SignChangeEvent
     * @param region setting the text from
     */
    private void setSignText(SignChangeEvent event, Region region) {
        event.setLine(0, "[" + RCMessaging.green(event.getLine(0).toUpperCase()) + "]");
        event.setLine(1, RCMessaging.red(region.getId()));
        event.setLine(2, RCMessaging.yellow(region.getPrice() + "") + " Coins");
        event.setLine(3, RCMessaging.green(region.getMainOwner()));
        RCLogger.debug("Sign updated: " + event.toString());
    }
}
