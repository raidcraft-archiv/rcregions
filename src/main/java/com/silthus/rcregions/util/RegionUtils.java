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

package com.silthus.rcregions.util;

import com.silthus.rcregions.config.RegionsConfig;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

/**
 * 03.10.11 - 19:27
 *
 * @author Silthus
 */
public class RegionUtils {

    /**
     * Checks if the block is a sign and if it
     * has the unique plugin identifier on the first line.
     * @param block to check
     * @return true if region sign
     */
    public static boolean isRegionSign(Block block) {
        return block.getState() instanceof Sign && isRegionSign((Sign) block.getState());
    }

    /**
     * Checks if the block has the unique identifier
     * on the first line
     * @param sign to check
     * @return true if region sign
     */
    public static boolean isRegionSign(Sign sign) {
        return isRegionSign(sign.getLine(0));
    }

    /**
     * Checks if the text is the identifier for a region sign
     * @param text to check
     * @return true if it is a region text
     */
    public static boolean isRegionSign(String text) {
        return text.equalsIgnoreCase(RegionsConfig.getUniqueLine());
    }
}
