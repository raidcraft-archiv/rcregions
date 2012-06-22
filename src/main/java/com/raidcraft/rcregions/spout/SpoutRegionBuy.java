package com.raidcraft.rcregions.spout;

import com.silthus.raidcraft.spout.RCButton;
import com.silthus.raidcraft.spout.RCLabel;
import com.silthus.raidcraft.spout.SpoutPlayerPopup;
import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.raidcraft.rcregions.exceptions.PlayerException;
import com.raidcraft.rcregions.exceptions.RegionException;
import com.silthus.raidcraft.util.RCMessaging;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.*;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * Created with IntelliJ IDEA.
 * User: Keiler
 * Date: 24.04.2012
 * Time: 21:41
 * */
public class SpoutRegionBuy extends SpoutPlayerPopup{
    private SpoutPlayer player;
    private Region region;

    public SpoutRegionBuy(Player player, Region region){
        super("RCRegions Grundstück Kaufen", RegionsPlugin.get(), (SpoutPlayer)player);
        this.player = (SpoutPlayer) player;
        this.region = region;
        createRegionInfo();
        super.show();
    }

    private void createRegionInfo(){

        //-> Name Labels
        RCLabel lbl_name = new RCLabel("Name der Region:").textColor(yellow).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(50)
                .shiftX(-this.getWidth()-20).shiftY(-(player.getMainScreen().getHeight()/2) + 50).dirty(true);
        RCLabel lbl_nameVar = new RCLabel(""+region.getName()).textColor(green).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(region.getName().length()*10)
                .shiftX(40).shiftY(-(player.getMainScreen().getHeight()/2) + 50).dirty(true);
        //<- Name Labels

        //-> District Labels
        RCLabel lbl_district = new RCLabel("Name des Distrikts:").textColor(yellow).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(50)
                .shiftX(-this.getWidth()-20).shiftY(-(player.getMainScreen().getHeight()/2) + 70).dirty(true);
        RCLabel lbl_districtVar = new RCLabel(""+region.getDistrict().getName()).textColor(green).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(region.getName().length()*10)
                .shiftX(40).shiftY(-(player.getMainScreen().getHeight()/2) + 70).dirty(true);
        //<- District Labels

        //-> Owner Labels
        RCLabel lbl_owner = new RCLabel("Owner:").textColor(yellow).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(50)
                .shiftX(-this.getWidth()-20).shiftY(-(player.getMainScreen().getHeight()/2) + 90).dirty(true);
        RCLabel lbl_ownerVar = new RCLabel(""+region.getOwner()).textColor(green).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(region.getName().length()*10)
                .shiftX(40).shiftY(-(player.getMainScreen().getHeight()/2) + 90).dirty(true);
        //<- Owner Labels

        //-> Price Labels
        RCLabel lbl_price = new RCLabel("Preis:").textColor(yellow).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(50)
                .shiftX(-this.getWidth()-20).shiftY(-(player.getMainScreen().getHeight()/2) + 110).dirty(true);
        RCLabel lbl_priceVar = new RCLabel(""+region.getPrice()).textColor(green).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(region.getName().length()*10)
                .shiftX(40).shiftY(-(player.getMainScreen().getHeight()/2) + 110).dirty(true);
        //<- Price Labels

        //-> Baseprice Labels
        RCLabel lbl_baseName = new RCLabel("Basispreis:").textColor(yellow).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(50)
                .shiftX(-this.getWidth()-20).shiftY(-(player.getMainScreen().getHeight()/2) + 130).dirty(true);
        RCLabel lbl_baseNameVar = new RCLabel(""+region.getBasePrice()).textColor(green).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(region.getName().length()*10)
                .shiftX(40).shiftY(-(player.getMainScreen().getHeight()/2) + 130).dirty(true);
        //<- Baseprice Labels

        //-> Taxprice Labels
        RCLabel lbl_taxPrice = new RCLabel("Steuern:").textColor(yellow).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(50)
                .shiftX(-this.getWidth()-20).shiftY(-(player.getMainScreen().getHeight()/2) + 150).dirty(true);
        RCLabel lbl_taxPriceVar = new RCLabel(""+ RegionManager.get().getTaxes(player, region.getDistrict())*100 + "% ").textColor(green).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(region.getName().length()*10)
                .shiftX(40).shiftY(-(player.getMainScreen().getHeight()/2) + 150).dirty(true);
        //<- Taxprice Labels

        //-> Taxprice Labels
        RCLabel lbl_endPrice = new RCLabel("Endpreis:").textColor(yellow).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(50)
                .shiftX(-this.getWidth()-20).shiftY(-(player.getMainScreen().getHeight()/2) + 170).dirty(true);
        RCLabel lbl_endPriceVar = new RCLabel(""+RegionManager.get().getFullPrice(player, region)).textColor(green).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(region.getName().length()*10)
                .shiftX(40).shiftY(-(player.getMainScreen().getHeight()/2) + 170).dirty(true);
        //<- Taxprice Labels

        //-> Disabledbuy Labels
        RCLabel lbl_disabledBuy = new RCLabel("Nicht kaufbar").textColor(red).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(50)
                .shiftX(this.getWidth()/2).shiftY((player.getMainScreen().getHeight()/2)-(this.getHeight())+7).dirty(true);
        //<- Disabledbuy Labels

        //Buttons
        RCButton bt_buy = new RCButton("Kaufen"){
            @Override
            public void onButtonClick(ButtonClickEvent event) {
                try {
                    RegionManager.get().buyRegion(player, region);
                    player.sendNotification(ChatColor.GREEN + "Grundstück erworben:", ChatColor.YELLOW + region.getName(), Material.PAPER);
                    hide();
                } catch (PlayerException e){
                    RCMessaging.warn(player, e.getMessage());
                } catch (RegionException e) {
                    RCMessaging.warn(player, e.getMessage());
                }
            }
        }.color(white).hoverColor(green).anchor(WidgetAnchor.CENTER_CENTER).auto(true)
                .height(20).width(60).shiftX(this.getWidth()/2).shiftY(+(player.getMainScreen().getHeight() / 2) - (this.getHeight()));

        RCButton bt_cancel = new RCButton("Abbrechen"){
            @Override
            public void onButtonClick(ButtonClickEvent event) {
                hide();
            }
        }.color(white).hoverColor(red).anchor(WidgetAnchor.CENTER_CENTER).auto(true)
                .height(20).width(90).shiftX(-this.getWidth() - 20).shiftY(+(player.getMainScreen().getHeight() / 2) - (this.getHeight()));

        //Adds
        attachWidgets(lbl_header, lbl_name, lbl_nameVar, lbl_district, lbl_districtVar, lbl_owner, lbl_ownerVar, lbl_price, lbl_priceVar, lbl_baseName, lbl_baseNameVar, lbl_taxPrice, lbl_taxPriceVar, lbl_endPrice, lbl_endPriceVar, bt_cancel);
        if(!(player.getName() == region.getOwner()))
            attachWidgets(bt_buy);
        else
            attachWidgets(lbl_disabledBuy);
    }

}