package dev.andrei1058.game.server.bungee;

import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.language.LanguageManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyUser {
    private UUID uuid;
    private String partyOwnerOrSpectateTarget = null;
    private long timeOut;
    private int arenaIdentifier;
    private Locale language = null;

    private static final ConcurrentHashMap<UUID, ProxyUser> loaded = new ConcurrentHashMap<>();

    public ProxyUser(String uuid, int arenaIdentifier, String langIso, String partyOwnerOrSpectateTarget){
        if (ArenaManager.getINSTANCE().getArenaById(arenaIdentifier) == null) return;
        this.arenaIdentifier = arenaIdentifier;
        this.uuid = UUID.fromString(uuid);
        if (partyOwnerOrSpectateTarget != null){
            if (!partyOwnerOrSpectateTarget.isEmpty()) {
                this.partyOwnerOrSpectateTarget = partyOwnerOrSpectateTarget;
            }
        }
        this.timeOut = System.currentTimeMillis() + 7000;
        Locale l = LanguageManager.getINSTANCE().getLocale(langIso);
        if (l != null) language = l;

        loaded.put(this.uuid, this);
    }

    public static boolean isPreLoaded(UUID uuid){
        return loaded.containsKey(uuid);
    }

    public boolean isTimedOut() {
        return timeOut < System.currentTimeMillis();
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getArenaId() {
        return arenaIdentifier;
    }

    public void destroy(String reason){
        SteveSus.debug("Destroyed PreLoaded User: " + uuid + " Reason: " + reason);
        loaded.remove(uuid);
    }

    public Locale getLanguage() {
        return language;
    }

    public static ProxyUser getPreLoaded(UUID uuid){
        return loaded.get(uuid);
    }

    // if arena is started is used as staff teleport target
    public String getPartyOwnerOrSpectateTarget() {
        return partyOwnerOrSpectateTarget;
    }

    public static ConcurrentHashMap<UUID, ProxyUser> getLoaded() {
        return loaded;
    }

}
