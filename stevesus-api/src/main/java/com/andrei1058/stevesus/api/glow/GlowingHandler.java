package com.andrei1058.stevesus.api.glow;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface GlowingHandler {

    void setGlowing(@NotNull Entity entity, @NotNull Player receiver, GlowColor color);

    void removeGlowing(@NotNull Entity entity, @NotNull Player receiver);
}
