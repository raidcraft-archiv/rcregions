package com.raidcraft.rcregions.util;

import com.raidcraft.rcregions.RegionsPlugin;
import com.raidcraft.rcregions.api.Region;
import com.raidcraft.rcregions.exceptions.WrongSignFormatException;
import de.raidcraft.RaidCraft;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.SignUtil;
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

    public static String parseRegionName(Sign sign) throws WrongSignFormatException {

        return parseRegionName(sign.getLines());
    }

    public static String parseRegionName(String[] lines) throws WrongSignFormatException {

        String line = ChatColor.stripColor(lines[REGION_ID_LINE_INDEX]);
        Matcher matcher = REGION_ID_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(2);
        } else if (SignUtil.isLineEqual(lines[0], RaidCraft.getComponent(RegionsPlugin.class).getMainConfig().sign_identitifer)) {
            return line.toLowerCase();
        }
        throw new WrongSignFormatException("Das Schild ist ein ungültig formattiertes Regions Schild. Bitte setzte es neu!");
    }

    public static void updateSign(Sign sign, Region region) {

        String[] lines = formatSign(region);
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, lines[i]);
        }
        sign.update(true, false);
    }

    public static String[] formatSign(Region region) {

        String[] lines = new String[4];
        StringBuilder sb = new StringBuilder();
        if (region.getPrice() > 0) {
            sb.append(CustomItemUtil.getSellPriceString(region.getPrice()));
        } else {
            sb.append(ChatColor.GREEN).append("Kostenlos");
        }
        lines[0] = sb.toString();

        sb = new StringBuilder("Id: ");
        if (region.isBuyable()) {
            sb.append(ChatColor.RED);
        } else {
            sb.append(ChatColor.GREEN);
        }
        sb.append(region.getName());
        lines[1] = sb.toString();

        sb = new StringBuilder();
        if (region.hasOwner()) {
            sb.append(region.getOwner());
        } else {
            sb.append("Server");
        }
        lines[2] = sb.toString();

        sb = new StringBuilder();
        if (region.isBuyable()) {
            sb.append(ChatColor.RED);
        } else {
            sb.append(ChatColor.GREEN);
        }
        sb.append(RaidCraft.getComponent(RegionsPlugin.class).getMainConfig().sign_identitifer);
        lines[3] = sb.toString();

        return lines;
    }
}
