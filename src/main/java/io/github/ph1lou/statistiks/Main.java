package io.github.ph1lou.statistiks;

import io.github.ph1lou.werewolfapi.GetWereWolfAPI;
import io.github.ph1lou.werewolfapi.enums.Sounds;
import io.github.ph1lou.werewolfapi.enums.UniversalMaterial;
import io.github.ph1lou.werewolfapi.registers.AddonRegister;
import io.github.ph1lou.werewolfapi.registers.RegisterManager;
import io.github.ph1lou.werewolfapi.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {


    GetWereWolfAPI ww ;

    GameReview currentGameReview;

    @Override
    public void onEnable() {

        ww = (GetWereWolfAPI) Bukkit.getPluginManager().getPlugin("WereWolfPlugin");

        List<String> credit = Arrays.asList("werewolf.stats.desc0","werewolf.stats.desc1","werewolf.stats.desc2");

        RegisterManager registerManager = ww.getRegisterManager();

        registerManager
                .registerAddon(new AddonRegister("werewolf.stat",
                                "fr",
                                this)
                        .setItem(UniversalMaterial.SIGN.getStack())
                        .setLoreKey(credit)
                        .setAction((player, inventory) -> Sounds.CAT_MEOW.play(player))
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
