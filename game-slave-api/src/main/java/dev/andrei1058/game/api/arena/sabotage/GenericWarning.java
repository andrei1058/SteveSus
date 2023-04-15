package dev.andrei1058.game.api.arena.sabotage;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.server.GameSound;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class GenericWarning {

    private final int originalSeconds;
    private final BossBar bossBar;
    private boolean warningRefresh = true;
    private final GameArena gameArena;

    public GenericWarning(GameArena gameArena, int deadLineSeconds, String title){
        this.gameArena = gameArena;
        this.originalSeconds = deadLineSeconds;
        bossBar = Bukkit.createBossBar(title, BarColor.RED, BarStyle.SEGMENTED_20);
        bossBar.setProgress(1);
    }

    public void refreshWarning(int currentTime) {
        if (currentTime == 0){
            bossBar.removeAll();
            gameArena.getWorld().getWorldBorder().setWarningDistance(0);
        }
        bossBar.setProgress((double) currentTime / originalSeconds);
        if (warningRefresh) {
            gameArena.getWorld().getWorldBorder().setWarningDistance(Integer.MAX_VALUE);
            warningRefresh = false;
        } else {
            gameArena.getWorld().getWorldBorder().setWarningDistance(0);
            warningRefresh = true;
        }
        GameSound.SABOTAGE_COUNT_DOWN.playToPlayers(gameArena.getPlayers());
        GameSound.SABOTAGE_COUNT_DOWN.playToPlayers(gameArena.getSpectators());
    }

    public void removePlayer(Player player){
        bossBar.removePlayer(player);
    }

    public void addPlayer(Player player){
        bossBar.addPlayer(player);
    }

    public void sendBar(){
        gameArena.getPlayers().forEach(bossBar::addPlayer);
    }

    public void restore(){
        bossBar.setProgress(1);
        warningRefresh = true;
        bossBar.removeAll();
        gameArena.getWorld().getWorldBorder().setWarningDistance(0);
    }

    public void setBarName(String newName){
        bossBar.setTitle(newName);
    }

    public int getOriginalSeconds() {
        return originalSeconds;
    }
}
