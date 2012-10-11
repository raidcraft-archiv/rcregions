package com.raidcraft.rcregions.spout;

import com.raidcraft.rcregions.Region;
import com.raidcraft.rcregions.RegionManager;
import com.raidcraft.rcregions.bukkit.RegionsPlugin;
import com.silthus.raidcraft.spout.RCButton;
import com.silthus.raidcraft.spout.RCLabel;
import com.silthus.raidcraft.spout.SpoutPlayerPopup;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * Created with IntelliJ IDEA.
 * User: Keiler
 * Date: 18.04.12
 * Time: 20:41
 */
public class SpoutRegionInfo extends SpoutPlayerPopup{
    private SpoutPlayer player;
    private Region region;

    //Buttons

    public SpoutRegionInfo(Player player, Region region){
        super("RCRegions Grundstück Info", RegionsPlugin.get(), (SpoutPlayer)player);
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

        //-> Refund Labels
        RCLabel lbl_refund = new RCLabel("Erstattung:").textColor(yellow).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(50)
                .shiftX(-this.getWidth()-20).shiftY(-(player.getMainScreen().getHeight()/2) + 110).dirty(true);
        RCLabel lbl_refundVar = new RCLabel(""+RegionManager.getInstance().getRefundPercentage(region)*100 + "% ").textColor(green).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(region.getName().length()*10)
                .shiftX(40).shiftY(-(player.getMainScreen().getHeight()/2) + 110).dirty(true);
        //<- Refund Labels

        //-> Price Labels
        RCLabel lbl_price = new RCLabel("Preis:").textColor(yellow).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(50)
                .shiftX(-this.getWidth()-20).shiftY(-(player.getMainScreen().getHeight()/2) + 130).dirty(true);
        RCLabel lbl_priceVar = new RCLabel(""+region.getPrice()).textColor(green).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(region.getName().length()*10)
                .shiftX(40).shiftY(-(player.getMainScreen().getHeight()/2) + 130).dirty(true);
        //<- Price Labels

        //-> Baseprice Labels
        RCLabel lbl_basePrice = new RCLabel("Basispreis:").textColor(yellow).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(50)
                .shiftX(-this.getWidth()-20).shiftY(-(player.getMainScreen().getHeight()/2) + 150).dirty(true);
        RCLabel lbl_basePriceVar = new RCLabel(""+region.getBasePrice()).textColor(green).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(region.getName().length()*10)
                .shiftX(40).shiftY(-(player.getMainScreen().getHeight()/2) + 150).dirty(true);
        //<- Baseprice Labels

        //-> Refundvalue Labels
        RCLabel lbl_refundValue = new RCLabel("Rückzahlung:").textColor(yellow).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(50)
                .shiftX(-this.getWidth()-20).shiftY(-(player.getMainScreen().getHeight()/2) + 170).dirty(true);
        RCLabel lbl_refundValueVar = new RCLabel(""+RegionManager.getInstance().getRefundValue(region)).textColor(green).anchor(WidgetAnchor.CENTER_CENTER).auto(true).height(10).width(region.getName().length()*10)
                .shiftX(40).shiftY(-(player.getMainScreen().getHeight()/2) + 170).dirty(true);
        //<- Refundvalue Labels


        //Buttons
        RCButton bt_cancel = new RCButton("Abbrechen"){
            @Override
            public void onButtonClick(ButtonClickEvent event) {
                hide();
            }
        }.color(white).hoverColor(red).anchor(WidgetAnchor.CENTER_CENTER).auto(true)
              .height(20).width(90).shiftX(-(this.getWidth() / 2) - 20).shiftY(+(player.getMainScreen().getHeight() / 2) - (this.getHeight()));

        //Adds
        attachWidgets(lbl_name, lbl_nameVar, lbl_district, lbl_districtVar, lbl_owner, lbl_ownerVar, lbl_refund, lbl_refundVar, lbl_price, lbl_priceVar, lbl_basePrice, lbl_basePriceVar, lbl_refundValue, lbl_refundValueVar, bt_cancel);
    }

}