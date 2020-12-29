package io.github.ph1lou.statistiks;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.github.ph1lou.werewolfapi.GetWereWolfAPI;
import io.github.ph1lou.werewolfapi.LoverAPI;
import io.github.ph1lou.werewolfapi.PlayerWW;
import io.github.ph1lou.werewolfapi.WereWolfAPI;
import io.github.ph1lou.werewolfapi.enums.RolesBase;
import io.github.ph1lou.werewolfapi.enums.StateGame;
import io.github.ph1lou.werewolfapi.enums.StatePlayer;
import io.github.ph1lou.werewolfapi.events.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Events implements Listener {

    private final Main main;
    private final GetWereWolfAPI ww;

    public Events(Main main){
        this.main=main;
        this.ww=main.getWereWolfAPI();
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameEnd(StopEvent event){

        if(main.getCurrentGameReview().getWinnerCampKey()==null) return;

        try {

            URL url = new URL ("http://ph1lou.fr:4567/infos");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            String jsonInputString = new Gson().toJson(main.getCurrentGameReview());

            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                TextComponent msg = new TextComponent( ww.getWereWolfAPI().translate("werewolf.message"));
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                        String.format("https://ph1lou.fr/werewolfstat/detailGame.php?id=%s",response.toString().replaceAll("\"",""))));

                Bukkit.getOnlinePlayers()
                        .forEach(player -> player.spigot().sendMessage(msg));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWin(WinEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("win",event.getPlayers(),api.getScore().getTimer(),event.getRole()));
        main.getCurrentGameReview().end(event.getRole(),event.getPlayers());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStart(StartEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.setCurrentGameReview(new GameReview(main,api)) ;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event){

        Player player=event.getEntity();
        WereWolfAPI api = ww.getWereWolfAPI();
        UUID playerUUID = player.getUniqueId();
        PlayerWW playerWW = api.getPlayerWW(playerUUID);

        if(playerWW==null) return;

        Player killer = player.getKiller();
        if(killer==null) return;

        UUID killerUUID = killer.getUniqueId();
        PlayerWW killerWW = api.getPlayerWW(killerUUID);

        if(main.getCurrentGameReview()==null) return;

        if(!api.isState(StateGame.GAME)) return;

        if(playerWW.isState(StatePlayer.DEATH)) return;

        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("kill",playerWW, killerWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFinalDeath(FinalDeathEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW killerWW = playerWW.getLastKiller();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("final_kill",playerWW, killerWW,api.getScore().getTimer()));
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onCustom(CustomEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction(event.getEvent(),event.getPlayerWW(), event.getPlayerWWS(),api.getScore().getTimer(),event.getExtraInfo(),event.getExtraInt()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInfection(InfectionEvent event){

        if(event.isCancelled()) return;


        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("infection",event.getPlayerWW(), event.getTargetWW(),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onReviveElder(ElderResurrectionEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW=event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("elder_revive",playerWW,api.getScore().getTimer(),event.isKillerAVillager()?"elder_kill_by_villager":"elder_not_kill_by_villager"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRevive(ResurrectionEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW=event.getPlayerWW();
        main.getCurrentGameReview()
                .addRegisteredAction(
                        new RegisteredAction("revive",playerWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWitchRevive(WitchResurrectionEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview()
                .addRegisteredAction(
                        new RegisteredAction("witch_revive",event.getPlayerWW(), event.getTargetWW(),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoverDeath(LoverDeathEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview()
                .addRegisteredAction(
                        new RegisteredAction("lover_death",event.getPlayerWW1(), event.getPlayerWW2(),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAmnesiacLoverDeath(AmnesiacLoverDeathEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview()
                .addRegisteredAction(
                        new RegisteredAction("amnesiac_lover_death",event.getPlayerWW1(), event.getPlayerWW2(),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCursedLoverDeath(CursedLoverDeathEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview()
                .addRegisteredAction(
                        new RegisteredAction("cursed_lover_death",event.getPlayerWW1(), event.getPlayerWW2(),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProtection(ProtectionEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW targetWW = event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("protection",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onModel(ModelEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW targetWW = event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("model",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCharmed(CharmEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW targetWW=event.getTargetWW();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("charmed",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnchanted(EnchantedEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        Set<PlayerWW> enchanted =event.getPlayerWWS();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("enchanted",playerWW,enchanted,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCursed(CurseEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW targetWW = event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("cursed",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDesignedLover(CupidLoversEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        Set<PlayerWW> lovers =event.getPlayerWWS();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("designed_lover",playerWW, lovers,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSteal(StealEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW thiefWW = event.getThiefWW();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("steal",thiefWW, playerWW,api.getScore().getTimer(),event.getRole()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGrowl(GrowlEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        Set<PlayerWW> growled =event.getPlayerWWS();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("growl",playerWW, growled,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBeginSniff(BeginSniffEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW targetWW=event.getTargetWW();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("begin_smell",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBeginCharm(BeginCharmEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW targetWW=event.getTargetWW();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("begin_charm",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSniff(SniffEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW targetWW=event.getTargetWW();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("sniff",playerWW, targetWW,api.getScore().getTimer(),event.isWereWolf()?"werewolf.role.fox.werewolf":"werewolf.role.fox.not_werewolf"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSee(SeerEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW targetWW=event.getTargetWW();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("see",playerWW, targetWW,api.getScore().getTimer(),event.getCamp()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTrack(TrackEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW targetWW=event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("track",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTrouble(TroubleMakerEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW targetWW=event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("trouble",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGiveBook(LibrarianRequestEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW targetWW=event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("give_book",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLibrarianDeath(LibrarianDeathEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("librarian_death",playerWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnquire(InvestigateEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        Set<PlayerWW> playerWWS =event.getPlayerWWs();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("enquire",playerWW, playerWWS,api.getScore().getTimer(),event.isSameCamp()?"werewolf.role.detective.same_camp":"werewolf.role.detective.opposing_camp"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTroubleDeath(TroubleMakerDeathEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("trouble_maker_death",playerWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInvisible(InvisibleEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("invisible",playerWW,api.getScore().getTimer(),event.isInvisible()?"invisible":"visible"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVoteSee(SeeVoteEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("see_vote",playerWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVoteCancel(CancelVoteEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("cancel_vote",playerWW, event.getVoteWW(),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVote(VoteEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW targetWW = event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("vote",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onNewWereWolf(NewWereWolfEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("new_werewolf",playerWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUseMask(UseMaskEvent event){

        if(event.isCancelled()) return;
        String[] masks={"mask_strength","mask_speed","mask_resistance"};
        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("mask",playerWW,api.getScore().getTimer(),masks[event.getMask()]));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWildChildTransformation(WildChildTransformationEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW modelWW =event.getModel();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("wild_child_transformation",playerWW, modelWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAmnesiacTransformation(AmnesiacTransformationEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW villager = event.getVillager();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("amnesiac_transformation",playerWW, villager,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVoteResult(VoteResultEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("vote_result",playerWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSuccubusResurrection(SuccubusResurrectionEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW targetWW = event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("succubus_resurrection",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSisterDeathName(SisterSeeNameEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();

        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("sister_see_name",event.getPlayerWW(), event.getTargetWW(),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSisterDeathRole(SisterSeeRoleEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();

        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("sister_role_name",event.getPlayerWW(), event.getTargetWW(),api.getScore().getTimer(),event.getTargetWW()==null?"pve":event.getTargetWW().getRole().getKey()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDay(DayEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("day",api.getScore().getTimer(),event.getNumber()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onNight(NightEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("night",api.getScore().getTimer(),event.getNumber()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoverReveal(RevealLoversEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        for(LoverAPI loverAPI:event.getLovers()){
            main.getCurrentGameReview().addRegisteredAction(new RegisteredAction(loverAPI.isKey(RolesBase.CURSED_LOVER.getKey())?"cursed_lover_revelation":"lover_revelation", Sets.newHashSet(loverAPI.getLovers()),api.getScore().getTimer()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStudLoverReveal(StudLoverEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("stud_lover",event.getPlayerWW(),event.getTargetWW(),api.getScore().getTimer()));

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAmnesiacLoverReveal(RevealAmnesiacLoversEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("amnesiac_lover_revelation",event.getPlayerWWS(),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onNewDisplayRole(NewDisplayRole event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("new_display_role",event.getPlayerWW(),api.getScore().getTimer(),event.getNewDisplayRole()));

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTarget(AngelTargetEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW=event.getPlayerWW();
        PlayerWW targetWW = event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("angel_target",playerWW, targetWW,api.getScore().getTimer()));
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onTargetDeath(AngelTargetDeathEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW=event.getPlayerWW();
        PlayerWW targetWW = event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("angel_target_death",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSKKill(SerialKillerEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW=event.getPlayerWW();
        PlayerWW targetWW = event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("sk_target_death",playerWW, targetWW,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFallenAngelKill(FallenAngelTargetDeathEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW=event.getPlayerWW();
        PlayerWW targetWW = event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("fallen_angel_kill_target",playerWW, targetWW,api.getScore().getTimer()));
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAngelChoice(AngelChoiceEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW=event.getPlayerWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("angel_choice",playerWW, api.getScore().getTimer(),event.getChoice().toString()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWereWolfList(WereWolfListEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("werewolf_list", api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRepartition(RepartitionEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("repartition", api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTroll(TrollEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("troll", api.getScore().getTimer(),api.getConfig().getTrollKey()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPVP(PVPEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("pvp", api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInvulnerability(InvulnerabilityEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("invulnerability", api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBorderStart(BorderStartEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("border_start",api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBorderStop(BorderStopEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("border_stop", api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDiggingEnd(DiggingEndEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("digging_end", api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDon(DonEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW =event.getPlayerWW();
        PlayerWW receiverWW =event.getReceiverWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("don",playerWW,receiverWW,api.getScore().getTimer(),event.getDon()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGiveBack(LibrarianGiveBackEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW targetWW = event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("give_back_book",playerWW, targetWW,api.getScore().getTimer(),event.getInfo()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAngelRegeneration(RegenerationEvent event){
        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();

        PlayerWW playerWW = event.getPlayerWW();
        PlayerWW targetWW = event.getTargetWW();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("angel_regeneration",playerWW, targetWW,api.getScore().getTimer()));

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRivalAnnouncement(RivalAnnouncementEvent event){
        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();


        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("rival_announcement",event.getPlayerWW(),new HashSet<>(event.getPlayerWWs()),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoverRivalDeath(RivalLoverDeathEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();

        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("rival_lover_death",event.getPlayerWW(),new HashSet<>(event.getPlayerWWs()),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRivalLover(RivalLoverEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();

        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("rival_lover",event.getPlayerWW(),event.getTargetWW(),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVillageIdiotResurrection(VillageIdiotEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();

        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("idiot_village",event.getPlayerWW(),event.getTargetWW(),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMysticalReveal(MysticalWerewolfRevelationEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();

        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("mystical_reveal",event.getPlayerWW(),event.getTargetWW(),api.getScore().getTimer(),event.getTargetWW().getRole().getKey()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWereWolfChat(WereWolfChatEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();

        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("werewolf_message",event.getPlayerWW(),api.getScore().getTimer(),event.getMessage()));

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPriestessSpec(PriestessEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();

        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("priestess_spec",event.getPlayerWW(), event.getTargetWW(), api.getScore().getTimer(),event.getCamp()));

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVillagerKit(VillagerKitEvent event){


        WereWolfAPI api = ww.getWereWolfAPI();

        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("villager_kit",event.getPlayerWW(),  api.getScore().getTimer(),event.getKit()));

    }
}
