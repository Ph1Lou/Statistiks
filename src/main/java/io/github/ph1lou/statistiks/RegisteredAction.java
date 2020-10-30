package io.github.ph1lou.statistiks;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegisteredAction {


    private final String event;
    @Nullable
    private final UUID uuid;
    @Nullable
    private final List<UUID> uuidS=new ArrayList<>();
    private final int timer;
    private String extraInfo;
    private int extraInt;

    public RegisteredAction(String event, @Nullable UUID uuid, List<UUID> uuidS, int timer){
        this.event = event;
        this.uuid=uuid;
        if(uuidS!=null){
            this.uuidS.addAll(uuidS);
        }
        this.timer=timer;
    }

    public RegisteredAction(String event, UUID uuid, List<UUID> uuidS, int timer, String extraInfo){
        this(event,uuid,uuidS,timer);
        this.extraInfo=extraInfo;
    }

    public RegisteredAction(String event, UUID uuid, List<UUID> uuidS, int timer, String extraInfo, int extraInt){
        this(event,uuid,uuidS,timer);
        this.extraInfo=extraInfo;
        this.extraInt=extraInt;
    }

    public RegisteredAction(String event, UUID uuid, List<UUID> uuidS, int timer, int extraInt){
        this(event,uuid,uuidS,timer);
        this.extraInt=extraInt;
    }

    public String getEvent() {
        return event;
    }

    public @Nullable UUID getUuid() {
        return uuid;
    }

    public @Nullable List<UUID> getUuidS() {
        return uuidS;
    }

    public int getTimer() {
        return timer;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public int getExtraInt() {
        return extraInt;
    }
}
