package io.github.ph1lou.statistiks;

import com.google.gson.Gson;
import io.github.ph1lou.werewolfapi.GetWereWolfAPI;
import io.github.ph1lou.werewolfapi.WereWolfAPI;
import io.github.ph1lou.werewolfapi.enumlg.StateLG;
import io.github.ph1lou.werewolfapi.events.*;
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
import java.util.Collections;
import java.util.List;
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
                System.out.println(response.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWin(WinEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("win",null, event.getPlayers(),api.getScore().getTimer(),event.getRole()));
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
        UUID uuid = player.getUniqueId();

        if(!api.getPlayersWW().containsKey(uuid)) return;


        Player killer = player.getKiller();
        if(killer==null) return;

        UUID killerUUID = killer.getUniqueId();

        if(!api.getPlayersWW().containsKey(killerUUID)) {
            killerUUID=null;
        }

        if(main.getCurrentGameReview()==null) return;

        if(!api.isState(StateLG.GAME)) return;

        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("kill",uuid, Collections.singletonList(killerUUID),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFinalDeath(FinalDeathEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getUuid();
        UUID killer = api.getPlayersWW().get(uuid).getLastKiller();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("final_kill",uuid, Collections.singletonList(killer),api.getScore().getTimer()));
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onCustom(CustomEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction(event.getEvent(),event.getPlayerUUID(), event.getTargetUUIDs(),api.getScore().getTimer(),event.getExtraInfo(),event.getExtraInt()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInfection(InfectionEvent event){

        if(event.isCancelled()) return;


        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("infection",event.getPlayerUUID(), Collections.singletonList(event.getInfectionUUID()),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onReviveElder(ElderResurrectionEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid=event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("elder_revive",uuid, null,api.getScore().getTimer(),event.isKillerAVillager()?1:0));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRevive(ResurrectionEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid=event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("revive",uuid, null,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWitchRevive(WitchResurrectionEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID witch=event.getWitchUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("witch_revive",witch, Collections.singletonList(uuid),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoverDeath(LoverDeathEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid1=event.getLover1Uuid();
        UUID uuid2 = event.getLover2Uuid();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("lover_death",uuid1, Collections.singletonList(uuid2),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAmnesiacLoverDeath(AmnesiacLoverDeathEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid1=event.getLover1Uuid();
        UUID uuid2 = event.getLover2Uuid();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("amnesiac_lover_death",uuid1, Collections.singletonList(uuid2),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCursedLoverDeath(CursedLoverDeathEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid1=event.getLover1Uuid();
        UUID uuid2 = event.getLover2Uuid();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("cursed_lover_death",uuid1, Collections.singletonList(uuid2),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProtection(ProtectionEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID witch=event.getPlayerUUID();
        UUID uuid = event.getTargetUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("protection",witch, Collections.singletonList(uuid),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onModel(ModelEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID model=event.getModelUUID();
        UUID uuid = event.getWildChildUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("model",uuid, Collections.singletonList(model),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCharmed(CharmEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID target=event.getTargetUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("charmed",uuid, Collections.singletonList(target),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnchanted(EnchantedEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        List<UUID> enchanted =event.getPlayersUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("enchanted",uuid,enchanted,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCursed(CurseEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID cursed=event.getTargetUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("cursed",uuid, Collections.singletonList(cursed),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDesignedLover(CupidLoversEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        List<UUID> lovers =event.getPlayersUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("designed_lover",uuid, lovers,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSteal(StealEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID steal=event.getPlayer();
        UUID uuid = event.getKiller();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("steal",uuid, Collections.singletonList(steal),api.getScore().getTimer(),event.getRole()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGrowl(GrowlEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        List<UUID> growled =event.getPlayersUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("growl",uuid, growled,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBeginSniff(BeginSniffEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID target=event.getTargetUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("begin_smell",uuid, Collections.singletonList(target),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBeginCharm(BeginCharmEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID target=event.getTargetUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("begin_charm",uuid, Collections.singletonList(target),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSniff(SniffEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID target=event.getTargetUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("sniff",uuid, Collections.singletonList(target),api.getScore().getTimer(),event.isWereWolf()?"werewolf.role.fox.werewolf":"werewolf.role.fox.not_werewolf"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSee(SeerEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID target=event.getTargetUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("see",uuid, Collections.singletonList(target),api.getScore().getTimer(),event.getCamp()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTrack(TrackEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID target=event.getTargetUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("track",uuid, Collections.singletonList(target),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTrouble(TroubleMakerEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID target=event.getTargetUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("trouble",uuid, Collections.singletonList(target),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGiveBook(LibrarianRequestEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID target=event.getTargetUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("give_book",uuid, Collections.singletonList(target),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnquire(InvestigateEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        List<UUID> growled =event.getPlayersUUID();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("enquire",uuid, growled,api.getScore().getTimer(),event.isSameCamp()?"werewolf.role.detective.same_camp":"werewolf.role.detective.opposing_camp"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTroubleDeath(TroubleMakerDeathEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("trouble_maker_death",uuid,null,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInvisible(InvisibleEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("invisible",uuid,null,api.getScore().getTimer(),event.isInvisible()?1:0));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVoteSee(SeeVoteEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("see_vote",uuid,null,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVoteCancel(CancelVoteEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("cancel_vote",uuid, Collections.singletonList(event.getVoteUUID()),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVote(VoteEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getPlayerUUID();
        UUID vote = event.getTargetUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("vote",uuid, Collections.singletonList(vote),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onNewWereWolf(NewWereWolfEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getUuid();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("new_werewolf",uuid,null,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUseMask(UseMaskEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("mask",uuid,null,api.getScore().getTimer(),event.getMask()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWildChildTransformation(WildChildTransformationEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getUuid();
        UUID model = event.getMaster();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("wild_child_transformation",uuid, Collections.singletonList(model),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAmnesiacTransformation(AmnesiacTransformationEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getUuid();
        UUID villager = event.getVillager();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("amnesiac_transformation",uuid, Collections.singletonList(villager),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVoteResult(VoteResultEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getPlayerVoteUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("vote_result",uuid,null,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSuccubusResurrection(SuccubusResurrectionEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getPlayerUUID();
        UUID charmed = event.getCharmedUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("succubus_resurrection",uuid, Collections.singletonList(charmed),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSisterDeath(SisterDeathEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid = event.getSister();
        UUID killer = event.getKiller();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("sister_death",uuid, Collections.singletonList(killer),api.getScore().getTimer(),String.valueOf(event.getKiller())));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDay(DayEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("day",null,null,api.getScore().getTimer(),event.getNumber()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onNight(NightEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("night",null,null,api.getScore().getTimer(),event.getNumber()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoverReveal(RevealLoversEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        for(List<UUID> list:event.getPlayersUUID()){
            main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("lover_revelation",null,list,api.getScore().getTimer()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCursedLoverReveal(RevealCursedLoversEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        for(List<UUID> list:event.getPlayersUUID()){
            main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("cursed_lover_revelation",null,list,api.getScore().getTimer()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAmnesiacLoverReveal(RevealAmnesiacLoversEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("amnesiac_lover_revelation",null,event.getPlayersUUID(),api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onNewDisplayRole(NewDisplayRole event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("new_display_role",event.getPlayerUUID(),null,api.getScore().getTimer(),event.getNewDisplayRole()));

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTarget(AngelTargetEvent event){

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid1=event.getPlayerUUID();
        UUID target = event.getTargetUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("angel_target",uuid1, Collections.singletonList(target),api.getScore().getTimer()));
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onTargetDeath(AngelTargetDeathEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid1=event.getPlayerUUID();
        UUID target = event.getTargetUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("angel_target_death",uuid1, Collections.singletonList(target),api.getScore().getTimer()));
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAngelChoice(AngelChoiceEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid=event.getPlayerUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("angel_choice",uuid, null,api.getScore().getTimer(),event.getChoice().toString()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWereWolfList(WereWolfListEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("werewolf_list",null, null,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRepartition(RepartitionEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("repartition",null, null,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTroll(TrollEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("troll",null, null,api.getScore().getTimer(),api.getConfig().getTrollKey()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPVP(PVPEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("pvp",null, null,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInvulnerability(InvulnerabilityEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("invulnerability",null, null,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBorderStart(BorderStartEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("border_start",null, null,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBorderStop(BorderStopEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("border_stop",null, null,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDiggingEnd(DiggingEndEvent event){
        WereWolfAPI api = ww.getWereWolfAPI();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("digging_end",null, null,api.getScore().getTimer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDon(DonEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid =event.getDonnerUUID();
        UUID receipt =event.getReceiptUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("don",uuid, Collections.singletonList(receipt),api.getScore().getTimer(),event.getDon()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGiveBack(LibrarianGiveBackEvent event){

        if(event.isCancelled()) return;

        WereWolfAPI api = ww.getWereWolfAPI();
        UUID uuid =event.getPlayerUUID();
        UUID receipt =event.getTargetUUID();
        main.getCurrentGameReview().addRegisteredAction(new RegisteredAction("give_back_book",uuid, Collections.singletonList(receipt),api.getScore().getTimer(),event.getInfo()));
    }
}
