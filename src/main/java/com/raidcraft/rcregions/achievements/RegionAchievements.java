package com.raidcraft.rcregions.achievements;

import com.raidcraft.rcregions.District;
import com.raidcraft.rcregions.DistrictManager;
import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import de.raidcraft.rcachievements.api.player.AchievementPlayer;
import de.raidcraft.rcachievements.api.plugin.ClassName;
import de.raidcraft.rcachievements.api.plugin.TaggedAchievement;
import org.getspout.commons.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Silthus
 */
@ClassName("player")
public class RegionAchievements implements TaggedAchievement {

	@MethodName("all-districts")
	public static boolean hasAllDistricts(AchievementPlayer player) {
		return getDistrictCount(player) == DistrictManager.get().getDistricts().size();
	}

	@MethodName("region-count")
	public static int getRegionCount(AchievementPlayer player) {
		return RegionManager.get().getPlayerRegionCount(player.getPlayer());
	}

	@MethodName("district-count")
	public static int getDistrictCount(AchievementPlayer player) {
		return getDistricts(player).size();
	}

	@MethodName("districts")
	public static Collection getDistricts(AchievementPlayer player) {
		List<Region> regions = RegionManager.get().getPlayerRegions(player.getPlayer());
		Set<String> uniqueDistricts = new HashSet<String>();
		for (Region region : regions) {
			uniqueDistricts.add(region.getDistrict().getIdentifier());
		}
		return uniqueDistricts;
	}

	@MethodName("regions")
	public static Collection getRegions(AchievementPlayer player) {
		return RegionManager.get().getPlayerRegions(player.getPlayer());
	}
}
