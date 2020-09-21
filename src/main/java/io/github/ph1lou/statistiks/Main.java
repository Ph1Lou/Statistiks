package io.github.ph1lou.statistiks;

import io.github.ph1lou.werewolfapi.GetWereWolfAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class Main extends JavaPlugin implements Listener {


    GetWereWolfAPI ww ;

    GameReview currentGameReview;

    @Override
    public void onEnable() {

        ww = (GetWereWolfAPI) Bukkit.getPluginManager().getPlugin("WereWolfPlugin");

        Bukkit.getPluginManager().registerEvents(new Events(this),this);

        saveDefaultConfig();
        if(getConfig().getString("server_uuid").isEmpty()){
            getConfig().set("server_uuid", UUID.randomUUID().toString());
            saveConfig();
        }

        ww.getAddonsList().add(this);
    }


    public GameReview getCurrentGameReview() {
        return currentGameReview;
    }

    public GetWereWolfAPI getWereWolfAPI() {
        return ww;
    }

    public void setCurrentGameReview(GameReview currentGameReview) {
        this.currentGameReview = currentGameReview;
    }



}
