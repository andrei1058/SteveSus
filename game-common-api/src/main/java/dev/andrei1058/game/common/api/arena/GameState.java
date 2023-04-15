package dev.andrei1058.game.common.api.arena;

import dev.andrei1058.game.common.api.locale.CommonMessage;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public enum GameState {

    LOADING(4, CommonMessage.ARENA_STATUS_ENABLING_NAME),
    WAITING(0, CommonMessage.ARENA_STATUS_WAITING_NAME),
    STARTING(1, CommonMessage.ARENA_STATUS_STARTING_NAME),
    IN_GAME(2, CommonMessage.ARENA_STATUS_IN_GAME_NAME),
    ENDING(3, CommonMessage.ARENA_STATUS_ENDING_NAME);

    private final CommonMessage translatePath;
    private final int stateCode;

    GameState(int stateCode, CommonMessage path) {
        this.translatePath = path;
        this.stateCode = stateCode;
    }

    public CommonMessage getTranslatePath() {
        return translatePath;
    }

    public int getStateCode() {
        return stateCode;
    }

    @Nullable
    public static GameState getByCode(int stateCode){
        return Arrays.stream(values()).filter(status -> status.stateCode == stateCode).findFirst().orElse(null);
    }

    @Nullable
    public static GameState getByNickName(String gameState){
        GameState result = null;
        switch (gameState){
            case "loading":
            case "l":
            case "enabling":
                result = LOADING;
                break;
            case "waiting":
            case "w":
            case "wait":
            case "lobby":
                result = WAITING;
                break;
            case "starting":
            case "s":
            case "start":
                result = STARTING;
                break;
            case "in_game":
            case "ingame":
            case "started":
            case "ig":
            case "g":
            case "playing":
            case "p":
                result = IN_GAME;
                break;
            case "end":
            case "ending":
            case "done":
            case "finishing":
            case "finish":
            case "e":
                result = ENDING;
                break;
        }
        return result;
    }
}
