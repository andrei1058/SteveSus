package com.andrei1058.amongusmc.api.server;

import com.andrei1058.amongusmc.api.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public enum GameSound {

    /**
     * You will hear this sound when you join the game (waiting/ starting state).
     */
    JOIN_SOUND_SELF(false, "join-player-self", "ENTITY_CHICKEN_EGG", "BLOCK_BEACON_ACTIVATE", 1, 1),

    /**
     * Played when you join a game but to the current players (waiting/ starting state).
     * You will receive {@link #JOIN_SOUND_SELF}.
     */
    JOIN_SOUND_CURRENT(false, "join-player-current", "ENTITY_CHICKEN_EGG", "BLOCK_BEACON_ACTIVATE", 1, 1),

    /**
     * Played when you join a game as spectator.
     */
    JOIN_SPECTATOR_SOUND_SELF(false, "join-spectator-self", "ENTITY_CHICKEN_EGG", "BLOCK_BEACON_ACTIVATE", 1, 1),

    /**
     * Played to all players when a player leaves the game at waiting/ starting.
     */
    LEAVE_SOUND_CURRENT(false, "leave-player-current", "ENTITY_SNOWBALL_THROW", "ENTITY_PHANTOM_FLAP", 1, 1),

    /**
     * Played to all players in a game if countdown is interrupted.
     */
    LEAVE_SOUND_START_INTERRUPTED_CURRENT(false, "leave-player-countdown-stop-current", "ENTITY_BLAZE_SHOOT", "ENTITY_BLAZE_SHOOT", 1.4f, 1),
    /**
     * Play game starting tick sound.
     * This is a path and can be used using {@link #playManual(String, Player)}.
     */
    COUNT_DOWN_TICK_(true, "count-down-tick-", "ENTITY_CHICKEN_EGG", "ENTITY_CHICKEN_EGG", 1, 1);

    // server version
    static byte SERVER_VERSION;

    /**
     * Get server version.
     *
     * @return version before 1. Like 12, 13 etc.
     */
    public static int getServerVersion() {
        if (SERVER_VERSION == 0) {
            SERVER_VERSION = Byte.parseByte(Bukkit.getServer().getClass().getName().split("\\.")[3].split("_")[1]);
        }
        return SERVER_VERSION;
    }

    // config path
    private final String ymlPath;
    // sound volume
    private float volume;
    // sound pitch
    private float pitch;
    // loaded sound
    private Sound sound;
    // if needs manual saving to yml
    private final boolean manual;

    /**
     * Prepare a sound.
     *
     * @param ymlPath  config path.
     * @param sound_12 sound for 1.12 server. Bukkit sound.
     * @param sound_13 sound alternative for 1.13+ server. Bukkit sound.
     * @param volume   sound volume.
     * @param pitch    sound pitch.
     */
    GameSound(boolean manual, String ymlPath, @Nullable String sound_12, @Nullable String sound_13, float volume, float pitch) {
        this.ymlPath = ymlPath;
        this.volume = volume;
        this.pitch = pitch;
        this.manual = manual;
        try {
            sound = getServerVersion() == 12 ? (sound_12 == null || sound_12.isEmpty() ? null : Sound.valueOf(sound_12)) : (sound_13 == null || sound_13.isEmpty() ? null : Sound.valueOf(sound_13));
        } catch (IllegalArgumentException e) {
            sound = null;
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return getYmlPath();
    }

    /**
     * Play this sound at a given location.
     * Will be played for all players.
     *
     * @param location location where to play that sound.
     */
    @SuppressWarnings("unused")
    public void playAtLocation(@NotNull Location location) {
        if (sound == null) return;
        if (location.getWorld() == null) return;
        location.getWorld().playSound(location, sound, volume, pitch);
    }

    /**
     * Play this sound at a given location.
     * Will be played for a target list of players.
     *
     * @param receivers players that will hear the sound.
     * @param location  location where to play that sound.
     */
    @SuppressWarnings("unused")
    public void playAtLocation(@NotNull Location location, List<Player> receivers) {
        if (sound == null) return;
        if (location.getWorld() == null) return;
        receivers.forEach(player -> player.playSound(location, sound, volume, pitch));
    }

    /**
     * Play sound to target player.
     * Will be played at his location.
     *
     * @param player player to receive the sound.
     */
    @SuppressWarnings("unused")
    public void playToPlayer(@NotNull Player player) {
        if (sound == null) return;
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Play sound to target player.
     * Will be played at a given location.
     *
     * @param location location where to play the sound.
     * @param player   player to receive the sound.
     */
    @SuppressWarnings("unused")
    public void playToPlayer(@NotNull Player player, @NotNull Location location) {
        if (sound == null) return;
        player.playSound(location, sound, volume, pitch);
    }

    /**
     * Play sound to target player list.
     * Will be played at each one's location.
     *
     * @param players player to receive the sound.
     */
    @SuppressWarnings("unused")
    public void playToPlayers(@NotNull List<Player> players) {
        if (sound == null) return;
        players.forEach(player -> player.playSound(player.getLocation(), sound, volume, pitch));
    }

    /**
     * Get yml path for this sound.
     */
    public String getYmlPath() {
        return ymlPath;
    }

    /**
     * Get config default value.
     */
    @NotNull
    public String getDefault() {
        return (sound == null ? "NONE" : sound.name()) + "," + getVolume() + "," + getPitch();
    }

    /**
     * If requires manual saving to config.
     */
    public boolean isManual() {
        return manual;
    }

    /**
     * Get sound.
     */
    @Nullable
    public Sound getSound() {
        return sound;
    }

    /**
     * Volume.
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Pitch.
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Change sound value.
     */
    public void setSound(@Nullable String sound) {
        if (sound != null && !sound.isEmpty()) {
            try {
                this.sound = Sound.valueOf(sound);
            } catch (Exception ignored) {
                this.sound = null;
            }
            return;
        }
        this.sound = null;
    }

    /**
     * Change volume value.
     */
    public void setVolume(float volume) {
        this.volume = volume;
    }

    /**
     * Change pitch value.
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    private static YamlConfiguration yml;

    /**
     * Initialize sounds file from the given directory.
     *
     * @param soundsDirectory directory where to get sounds file from.
     */
    public static void init(File soundsDirectory) {
        // create or load configuration
        File soundFile = new File(soundsDirectory, "sounds.yml");
        if (!soundFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                soundFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        yml = YamlConfiguration.loadConfiguration(soundFile);
        yml.options().copyDefaults(true);

        // save default sounds
        for (GameSound sound : values()) {
            if (!sound.isManual()) {
                yml.addDefault(sound.getYmlPath(), sound.getDefault());
            }
        }

        // save manual sounds
        yml.addDefault(COUNT_DOWN_TICK_.getYmlPath() + "from-10-to-1", COUNT_DOWN_TICK_.getDefault());
        yml.addDefault(COUNT_DOWN_TICK_.getYmlPath() + "20", COUNT_DOWN_TICK_.getDefault());

        // save
        try {
            yml.save(soundFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // load changes
        for (GameSound sound : values()) {
            if (!sound.isManual()) {
                String[] data = yml.getString(sound.getYmlPath()).split(",");
                if (data.length == 3) {
                    try {
                        sound.setVolume(Float.parseFloat(data[1]));
                        sound.setPitch(Float.parseFloat(data[2]));
                    } catch (Exception ignored) {
                    }
                    sound.setSound(data[0]);
                } else {
                    sound.setSound(null);
                }
            }
        }

        // load manuals
        for (String entry : yml.getConfigurationSection("").getKeys(false)) {
            if (entry.startsWith(GameSound.COUNT_DOWN_TICK_.toString())) {
                String[] data = entry.split("-");
                if (data[data.length - 2].equals("to") && data[data.length - 4].equals("from")) {
                    CountdownSoundCache.cache(entry);
                }
            }
        }
    }

    /**
     * This is used to play sounds that nedds to be handled manually.
     * It will not work with regular sounds.
     */
    public void playManual(String pathAddition, Player player) {
        if (!isManual()) return;
        String data = yml.getString(this.getYmlPath() + pathAddition);
        if (data == null) return;
        String[] values = data.split(",");
        Sound sound;
        try {
            sound = Sound.valueOf(values[0]);
        } catch (Exception ignored) {
            return;
        }
        float volume = Float.parseFloat(values[1]);
        float pitch = Float.parseFloat(values[2]);
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * This is used to play sounds that nedds to be handled manually.
     * It will not work with regular sounds.
     */
    public void playManual(String pathAddition, List<Player> players) {
        if (!isManual()) return;
        String data = yml.getString(this.getYmlPath() + pathAddition);
        if (data == null) return;
        String[] values = data.split(",");
        Sound sound;
        try {
            sound = Sound.valueOf(values[0]);
        } catch (Exception ignored) {
            return;
        }
        float volume = Float.parseFloat(values[1]);
        float pitch = Float.parseFloat(values[2]);
        players.forEach(player -> player.playSound(player.getLocation(), sound, volume, pitch));
    }

    public static class CountdownSoundCache {

        /**
         * Stores the sound range.
         */
        private static class RangedSound {
            private final int start;
            private final int end;
            String pathAdditions;

            protected RangedSound(int x, int y, String pathAdditions) {
                start = Math.min(x, y);
                end = Math.max(x, y);
                this.pathAdditions = pathAdditions;
            }
        }

        // actual cache
        private static final LinkedList<RangedSound> cachedRange = new LinkedList<>();

        /**
         * Cache a sound by path.
         */
        public static void cache(String path) {
            if (path.trim().isEmpty()) return;
            String[] range = path.split("-");
            int x;
            int y;
            try {
                x = Integer.parseInt(range[range.length - 1]);
                y = Integer.parseInt(range[range.length - 3]);
            } catch (Exception ignored) {
                return;
            }
            cachedRange.add(new RangedSound(x, y, path.replace(GameSound.COUNT_DOWN_TICK_.toString(), "")));
        }

        public static void playForSecond(Arena arena, int second) {
            String path = yml.getString(GameSound.COUNT_DOWN_TICK_.getYmlPath() + second);
            if (path != null) {
                COUNT_DOWN_TICK_.playManual("" + second, arena.getPlayers());
                return;
            }
            cachedRange.forEach(sound -> {
                if (sound.start <= second && sound.end >= second) {
                    COUNT_DOWN_TICK_.playManual(sound.pathAdditions, arena.getPlayers());
                }
            });
        }
    }
}
