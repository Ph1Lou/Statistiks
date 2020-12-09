package io.github.ph1lou.statistiks;

import io.github.ph1lou.werewolfapi.PlayerWW;
import io.github.ph1lou.werewolfapi.WereWolfAPI;

import java.util.*;
import java.util.stream.Collectors;

public class GameReview {

    private final UUID gameUUID;

    private Set<UUID> winners;

    private String winnerCampKey;

    private final List<PlayerReview> players=new ArrayList<>();

    private int duration;

    private final int playerSize;

    private final transient WereWolfAPI api;

    private String name;

    private final List<RegisteredAction> registeredActions=new ArrayList<>();

    private final UUID serverUUID;


    public GameReview(Main main, WereWolfAPI api) {
            this.api=api;
            this.gameUUID=api.getGameUUID();
            this.playerSize=api.getScore().getPlayerSize();
            this.serverUUID=UUID.fromString(Objects.requireNonNull(main.getConfig().getString("server_uuid")));
    }

    public void end(String winnerCampKey, Set<PlayerWW> winners){
        this.winnerCampKey=winnerCampKey;
        this.winners=winners.stream().map(PlayerWW::getUUID).collect(Collectors.toSet());
        this.name=api.getGameName();
        for(PlayerWW playerWW:api.getPlayerWW()){
            PlayerReview playerReview= new PlayerReview(playerWW);
            players.add(playerReview);
        }
        this.duration=api.getScore().getTimer();
    }

    public void addRegisteredAction(RegisteredAction registeredAction){
        this.registeredActions.add(registeredAction);
    }

    public UUID getGameUUID() {
        return gameUUID;
    }

    public Set<UUID> getWinners() {
        return winners;
    }

    public String getWinnerCampKey() {
        return winnerCampKey;
    }

    public List<PlayerReview> getPlayers() {
        return players;
    }

    public int getDuration() {
        return duration;
    }

    public WereWolfAPI getApi() {
        return api;
    }

    public String getName() {
        return name;
    }

    public List<RegisteredAction> getRegisteredActions() {
        return registeredActions;
    }

    public int getPlayerSize() {
        return playerSize;
    }

    public UUID getServerUUID() { return serverUUID; }
}
