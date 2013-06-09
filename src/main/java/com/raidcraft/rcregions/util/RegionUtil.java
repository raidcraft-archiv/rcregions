package com.raidcraft.rcregions.util;

import com.raidcraft.rcregions.exceptions.UnknownRegionException;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Silthus
 */
public class RegionUtil {

    public static final int REGION_ID_LINE_INDEX = 1;
    public static final Pattern REGION_ID_PATTERN = Pattern.compile("^(Region|Id): ([a-zA-Z0-9]+)$");

    public static String parseRegionName(Sign sign) throws UnknownRegionException {

        String line = ChatColor.stripColor(sign.getLine(REGION_ID_LINE_INDEX));
        Matcher matcher = REGION_ID_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        throw new UnknownRegionException("Das Schild an Position "
                + sign.getX() + "," + sign.getY() + "," + sign.getZ() + " ist ungültig formattiert.");
    }
}
