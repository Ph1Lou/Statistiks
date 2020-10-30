package io.github.ph1lou.statistiks;

import java.util.List;
import java.util.UUID;

public class PlayerReview {


    private final UUID uuid;
    private final String role;
    private final UUID amnesiacLover;
    private final List<UUID> lovers;
    private final UUID cursedLover;
    private final int deathTime;
    private final List<UUID> killers;
    private final int nbKill;
    private final boolean infected;
    private final String name;

    public PlayerReview(UUID uuid, String display, List<UUID> lovers, UUID amnesiacLoverUUID, UUID cursedLovers, int deathTime, List<UUID> killers, int nbKill, boolean infected,String name, boolean isThief) {
        this.uuid=uuid;
        if(isThief){
            this.role="werewolf.role.thief.display";
        }
        else this.role=display;
        this.lovers=lovers;
        this.amnesiacLover=amnesiacLoverUUID;
        this.cursedLover=cursedLovers;
        this.deathTime=deathTime;
        this.killers=killers;
        this.nbKill=nbKill;
        this.infected=infected;
        this.name=name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() { return this.name;}

    public String getRole() {
        return role;
    }

    public UUID getAmnesiacLover() {
        return amnesiacLover;
    }

    public List<UUID> getLovers() {
        return lovers;
    }

    public UUID getCursedLover() {
        return cursedLover;
    }

    public int getDeathTime() {
        return deathTime;
    }

    public List<UUID> getKillers() {
        return killers;
    }

    public int getNbKill() {
        return nbKill;
    }

    public boolean isInfected() {
        return infected;
    }
}
