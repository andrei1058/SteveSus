package com.andrei1058.amongusmc.arena.task;

import com.andrei1058.amongusmc.api.arena.Arena;

public class ArenaTaskPlaying implements Runnable{

    private final Arena arena;

    public ArenaTaskPlaying(Arena arena) {
        this.arena = arena;
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public void run() {

    }
}
