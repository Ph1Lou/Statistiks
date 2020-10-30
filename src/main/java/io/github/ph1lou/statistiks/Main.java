package io.github.ph1lou.statistiks;

import io.github.ph1lou.werewolfapi.AddonRegister;
import io.github.ph1lou.werewolfapi.GetWereWolfAPI;
import io.github.ph1lou.werewolfapi.RegisterManager;
import io.github.ph1lou.werewolfapi.enumlg.Sounds;
import io.github.ph1lou.werewolfapi.enumlg.UniversalMaterial;
import io.github.ph1lou.werewolfapi.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {


    GetWereWolfAPI ww ;

    GameReview currentGameReview;

    @Override
    public void onEnable() {

        ww = (GetWereWolfAPI) Bukkit.getPluginManager().getPlugin("WereWolfPlugin");

        RegisterManager registerManager = ww.getRegisterManager();

        registerManager
                .registerAddon(new AddonRegister("werewolf.stat",
                                "fr",
                                this,
                                new ItemBuilder(UniversalMaterial.SIGN.getType())
                                        .setDisplayName("§dStatistiks")
                                        .setLore(Arrays.asList("Envoie les récapitulatifs","des parties sur","https://ph1lou.fr/werewolfstat","Fait par Ph1lou"))
                                        .build())
                                .setAction(Sounds.CAT_MEOW::play)
                                .addAuthors("Ph1Lou",
                                        UUID.fromString("056be797-2a0b-4807-9af5-37faf5384396")));
                        Bukkit.getPluginManager().registerEvents(new Events(this),this);

        saveDefaultConfig();
        if(getConfig().getString("server_uuid").isEmpty()){
            getConfig().set("server_uuid", UUID.randomUUID().toString());
            saveConfig();
        }


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
