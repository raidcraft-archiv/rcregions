package com.raidcraft.rcregions.achievements;

import com.raidcraft.rcregions.District;
import com.raidcraft.rcregions.DistrictManager;
import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import de.raidcraft.rcachievements.api.player.AchievementPlayer;
import de.raidcraft.rcachievements.api.plugin.ClassName;
import de.raidcraft.rcachievements.api.plugin.TaggedAchievement;

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

	@MethodName("regions")
	public static int getRegionCount(AchievementPlayer player) {
		return RegionManager.get().getPlayerRegionCount(player.getPlayer());
	}

	@MethodName("districts")
	public static int getDistrictCount(AchievementPlayer player) {
		List<Region> regions = RegionManager.get().getPlayerRegions(player.getPlayer());
		Set<District> uniqueDistricts = new HashSet<District>();
		for (Region region : regions) {
			uniqueDistricts.add(region.getDistrict());
		}
		return uniqueDistricts.size();
	}
}
