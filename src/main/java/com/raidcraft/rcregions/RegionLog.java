package com.raidcraft.rcregions;

import com.raidcraft.rcregions.util.Enums;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: Philip
 * Date: 02.10.12 - 20:28
 * Description:
 */
public class RegionLog {
    private String player;
    private String region;
    private String action;
    private double price;
    private double tax;
    private String time;

    public RegionLog(String player, String region, Enums.Action action, double price, double tax) {
        this.player = player;
        this.region = region;
        this.action = action.name();
        this.price = price;
        this.tax = tax;
        SimpleDateFormat df = new SimpleDateFormat( "dd-MM-yyy HH:mm:ss" );
        this.time = df.format((new Date()));
    }

    public RegionLog(String player, String region, String action, double price, double tax, String time) {
        this.player = player;
        this.region = region;
        this.action = action;
        this.price = price;
        this.tax = tax;
        this.time = time;
    }

    public String getPlayer() {
        return player;
    }

    public String getRegion() {
        return region;
    }

    public String getAction() {
        return action;
    }

    public double getPrice() {
        return price;
    }

    public double getTax() {
        return tax;
    }

    public String getTime() {
        return time;
    }
}
