package com.andrei1058.stevesus.worldmanager;

import co.aikar.taskchain.TaskChain;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.api.world.WorldAdapter;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.setup.SetupManager;
import com.andrei1058.stevesus.worldmanager.generator.VoidChunkGenerator;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ExcludeFileFilter;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class InternalWorldAdapter implements WorldAdapter {
    /*
    private enum VoidGenerator {

        LEGACY_GENERATOR_SETTINGS("1;0;1"),
        V1_13_GENERATOR_SETTINGS("{\"layers\": [{\"block\": \"air\", \"height\": 1}, {\"block\": \"air\", \"height\": 1}], \"biome\":\"plains\"}"),
        V1_16_GENERATOR_SETTINGS("{\"biome\":\"minecraft:plains\",\"layers\":[{\"block\":\"minecraft:air\",\"height\":1}],\"structures\":{\"structures\":{}}}");

        private final String generator;

        VoidGenerator(String generator) {
            this.generator = generator;
        }

        public String get() {
            return generator;
        }
    }
    */

    private final File backupFolder = new File(SteveSus.getInstance().getDataFolder(), "Worlds");
    //private static VoidGenerator voidGenerator;

    public InternalWorldAdapter() {
        // setup void generator
        //String serverVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
        //int version = Integer.parseInt(serverVersion.split("_")[1]);
        /*if (version >= 13 && version < 16) {
            voidGenerator = VoidGenerator.V1_13_GENERATOR_SETTINGS;
        } else if (version >= 16) {
            voidGenerator = VoidGenerator.V1_16_GENERATOR_SETTINGS;
        } else {
            voidGenerator = VoidGenerator.LEGACY_GENERATOR_SETTINGS;
        }*/

        // create backup folder
        if (!backupFolder.exists()) {
            if (!backupFolder.mkdir()) {
                SteveSus.getInstance().getLogger().severe("Could not create directory: " + backupFolder.getPath());
            }
        }
    }

    @Override
    public String getAdapterName() {
        return "Bukkit";
    }

    @Override
    public boolean hasWorld(String name) {
        return getWorlds().contains(name) || new File(backupFolder, name + ".zip").exists();
    }

    @Override
    public void onAdapterInitialize() {
        File dir = Bukkit.getWorldContainer();
        if (dir.exists()) {
            SteveSus.getInstance().getLogger().warning("[InternalWorldAdapter] Cleaning obsolete temp worlds...");
            File[] fls = dir.listFiles();
            for (File fl : Objects.requireNonNull(fls)) {
                if (fl.isDirectory()) {
                    if (fl.getName().contains(ArenaHandler.WORLD_NAME_SEPARATOR)) {
                        deleteFolder(fl);
                    }
                }
            }
        }
    }

    @Override
    public void onArenaEnableQueue(String worldToClone, Arena arena) {
        TaskChain<?> chain = SteveSus.newChain();

        String gameSessionWorld = worldToClone + ArenaHandler.WORLD_NAME_SEPARATOR + arena.getGameId();
        File worldTemplateFolder = new File(Bukkit.getWorldContainer(), worldToClone);
        File zipTemplate = new File(backupFolder, worldToClone + ".zip");
        File worldCloneFolder = new File(Bukkit.getWorldContainer(), gameSessionWorld);

        // remove old game instance world
        chain.async(() -> {
            if (!gameSessionWorld.equals(worldToClone)) {
                deleteFolder(worldCloneFolder);
            }
        });

        // Create backup file from original world
        if (!zipTemplate.exists()) {
            if (worldTemplateFolder.exists()) {
                chain.async(() -> {
                    try {
                        SteveSus.getInstance().getLogger().info("Zipping world (backup): " + worldToClone);
                        createZip(zipTemplate, worldTemplateFolder);
                    } catch (ZipException e) {
                        e.printStackTrace();
                        SteveSus.getInstance().getLogger().severe("Could not create zip cache for " + worldToClone + "!");
                        SteveSus.newChain().sync(() -> ArenaHandler.getINSTANCE().removeFromEnableQueue(gameSessionWorld)).execute();
                        chain.abortChain();
                    }
                });
            } else {
                SteveSus.getInstance().getLogger().severe(worldToClone + " world was requested by an arena but it was not found!");
                ArenaHandler.getINSTANCE().removeFromEnableQueue(gameSessionWorld);
                return;
            }
        }

        // UnZip world cache
        chain.delay(20).async(() -> {
            try {
                SteveSus.getInstance().getLogger().info("Unzipping world backup(" + worldToClone + ") into " + gameSessionWorld + ".");
                if (!unZip(worldToClone, gameSessionWorld)) {
                    SteveSus.getInstance().getLogger().severe("Could not unzip cache of  " + worldToClone + "!");
                    SteveSus.newChain().sync(() -> ArenaHandler.getINSTANCE().removeFromEnableQueue(gameSessionWorld)).execute();
                    chain.abortChain();
                }
            } catch (Exception e) {
                e.printStackTrace();
                SteveSus.getInstance().getLogger().severe("Could not unzip cache of  " + worldToClone + "!");
                SteveSus.newChain().sync(() -> ArenaHandler.getINSTANCE().removeFromEnableQueue(gameSessionWorld)).execute();
                chain.abortChain();
            }
        });

        if (Bukkit.getWorld(gameSessionWorld) == null) {
            WorldCreator worldCreator = new WorldCreator(gameSessionWorld);
            //worldCreator.type(WorldType.FLAT);
            //worldCreator.generatorSettings(voidGenerator.get());
            //worldCreator.generateStructures(false);
            worldCreator.generator(new VoidChunkGenerator());

            chain.delay(10).sync(() -> {
                try {
                    Bukkit.createWorld(worldCreator);
                } catch (Exception ex) {
                    SteveSus.getInstance().getLogger().severe("Could not load world: " + gameSessionWorld + " (" + worldToClone + ").");
                    SteveSus.newChain().sync(() -> ArenaHandler.getINSTANCE().removeFromEnableQueue(gameSessionWorld)).execute();
                    ArenaHandler.getINSTANCE().removeFromEnableQueue(gameSessionWorld);
                    chain.abortChain();
                }
            });
        } else {
            // If the world is already loaded initialize the arena
            arena.init(Bukkit.getWorld(worldToClone));
            ArenaHandler.getINSTANCE().removeFromEnableQueue(gameSessionWorld);
            chain.abortChain();
            return;
        }

        // Arena#init must be called in Bukkit WorldLoadEvent
        chain.execute();
    }

    @Override
    public void onArenaRestart(Arena arena) {
        if (arena.getWorld() == null) return;
        SteveSus.newChain().sync(() -> Bukkit.unloadWorld(arena.getWorld(), false)).execute();
    }

    @Override
    public void onArenaDisable(Arena arena) {
        if (arena.getWorld() == null) return;
        SteveSus.newChain().sync(() -> Bukkit.unloadWorld(arena.getWorld(), false)).execute();
    }

    @Override
    public void onSetupSessionStart(String worldName, SetupSession setupSession) {
        TaskChain<?> chain = SteveSus.newChain();

        // if world is loaded unload
        if (Bukkit.getWorld(worldName) != null) {
            chain.sync(() -> Bukkit.unloadWorld(worldName, true));
        }

        File templateWorld = new File(Bukkit.getWorldContainer(), worldName);

        // Check if there is a backup of the requested world and unzip it
        File backupFile = new File(backupFolder, worldName + ".zip");
        if (backupFile.exists()) {
            chain.async(() -> {
                deleteFolder(templateWorld);
                try {
                    //noinspection ResultOfMethodCallIgnored
                    templateWorld.mkdir();
                    SteveSus.getInstance().getLogger().info("Unzipping world backup: " + worldName);
                    if (!unZip(worldName, setupSession.getWorldName())) {
                        SteveSus.getInstance().getLogger().severe("Could not unzip cache for " + worldName + "!");
                        SetupManager.getINSTANCE().removeSession(setupSession);
                        chain.abortChain();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SteveSus.getInstance().getLogger().severe("Could not unzip cache for " + worldName + "!");
                    SetupManager.getINSTANCE().removeSession(setupSession);
                    chain.abortChain();
                }
            });
        }
        WorldCreator worldCreator = new WorldCreator(worldName);
        //worldCreator.type(WorldType.FLAT);
        //worldCreator.generatorSettings(voidGenerator.get());
        //worldCreator.generateStructures(false);
        worldCreator.generator(new VoidChunkGenerator());

        chain.sync(() -> {
            try {
                Bukkit.createWorld(worldCreator);
            } catch (Exception ex) {
                SteveSus.getInstance().getLogger().severe("Could not load world: " + worldName);
                SetupManager.getINSTANCE().removeSession(setupSession);
                chain.abortChain();
            }
        });
        chain.execute();
    }

    @Override
    public void onSetupSessionClose(SetupSession setupSession) {
        TaskChain<?> chain = SteveSus.newChain();

        File zipTemplateFile = new File(backupFolder, setupSession.getWorldName() + ".zip");
        File worldFolder = new File(Bukkit.getWorldContainer(), setupSession.getWorldName());

        chain.sync(() -> {
            // unload world
            Bukkit.unloadWorld(setupSession.getWorldName(), true);
        }).async(() -> {

            // delete old backup
            if (zipTemplateFile.exists()) {
                deleteFolder(zipTemplateFile);
            }

            // create backup
            if (worldFolder.exists()) {
                try {
                    SteveSus.getInstance().getLogger().info("Zipping world (backup): " + setupSession.getWorldName());
                    createZip(zipTemplateFile, worldFolder);
                } catch (ZipException e) {
                    e.printStackTrace();
                    SteveSus.getInstance().getLogger().severe("Could not create zip cache for " + setupSession.getWorldName() + "!");
                    chain.abortChain();
                }
            }
        }).async(() -> {
            if (worldFolder.exists()) {
                deleteFolder(worldFolder);
            }
        }).execute();
    }

    @Override
    public boolean isAutoImport() {
        return true;
    }

    @Override
    public List<String> getWorlds() {
        List<String> worlds = new ArrayList<>();
        File dir = Bukkit.getWorldContainer();
        if (dir.exists()) {
            File[] fls = dir.listFiles();
            for (File fl : Objects.requireNonNull(fls)) {
                if (fl.isDirectory()) {
                    File dat = new File(fl.getName() + "/region");
                    if (dat.exists() && !fl.getName().contains(ArenaHandler.WORLD_NAME_SEPARATOR)) {
                        if (fl.getName().equals(fl.getName().toLowerCase())) {
                            // prevent usage of the main world
                            if (Bukkit.getWorlds().get(0).getName().equals(fl.getName())) {
                                continue;
                            }
                            worlds.add(fl.getName());
                        }
                    }
                }
            }
            if (backupFolder.exists()) {
                for (String file : backupFolder.list()) {
                    if (file.endsWith(".zip")) {
                        String world = file.replace(".zip", "");
                        if (!worlds.contains(world) && world.equals(world.toLowerCase())) {
                            worlds.add(world);
                        }
                    }
                }
            }
        }
        return worlds;

    }

    @Override
    public void deleteWorld(String name) {
        deleteFolder(new File(Bukkit.getWorldContainer(), name));
        deleteFolder(new File(backupFolder, name + ".zip"));
    }

    /**
     * Make sure to use this async.
     */
    private void deleteFolder(File index) {
        if (!index.exists()) return;
        if (index.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            index.delete();
            return;
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            Files.walk(index.getAbsoluteFile().toPath(), FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .peek(System.out::println)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createZip(File zipTemplate, File worldTemplateFolder) throws ZipException {
        ExcludeFileFilter excludeFileFilter = (f) -> {
            if (f.isDirectory()) {
                for (String folder : new String[]{"advancements", "playerdata", "stats", "serverconfig"}) {
                    if (new File(worldTemplateFolder, folder).equals(f)) {
                        return true;
                    }
                }
            } else if (f.isFile()) {
                for (String file : new String[]{"uid.dat", "icon.png"}) {
                    if (new File(worldTemplateFolder + File.separator + file).equals(f)) {
                        return true;
                    }
                }
            }
            return false;
        };
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setExcludeFileFilter(excludeFileFilter);
        ZipFile zipFile = new ZipFile(zipTemplate);
        /*for (File f : worldTemplateFolder.listFiles()) {
            if (f != null) {
                if (f.isDirectory()) {
                    zipFile.addFolder(f, zipParameters);
                } else {
                    zipFile.addFile(f, zipParameters);
                }
            }
        }*/
        zipFile.addFolder(worldTemplateFolder, zipParameters);
    }

    // unzip to bukkit world container
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean unZip(String worldTemplate, String worldCloneFolder) throws IOException {
        File zipBackup = new File(backupFolder, worldTemplate + ".zip");
        if (!zipBackup.exists()) {
            SteveSus.getInstance().getLogger().warning("Cannot find " + worldTemplate + " folder in " + zipBackup.getAbsolutePath());
            return false;
        }

        ZipFile zipFile = new ZipFile(new File(backupFolder, worldTemplate + ".zip"));

        String worldNameInZip = null;
        for (FileHeader fh : zipFile.getFileHeaders()) {
            String[] path = fh.getFileName().split("/");
            if (fh.isDirectory() && (path.length == 2 && path[1].equals("region"))) {
                worldNameInZip = fh.getFileName().split("/")[0];
                break;
            }
        }

        FileHeader levelFile = zipFile.getFileHeader("level.dat");
        if (worldNameInZip == null && levelFile == null) {
            SteveSus.getInstance().getLogger().warning("This doesn't look like a map" + zipBackup.getAbsolutePath());
            return false;
        }
        File destination = new File(Bukkit.getWorldContainer(), worldCloneFolder);
        if (levelFile != null) {
            zipFile.extractAll(destination.getAbsolutePath());
            deleteFolder(new File(worldCloneFolder + File.separator + "uid.dat"));
        }
        if (worldNameInZip != null) {
            zipFile.extractAll(Bukkit.getWorldContainer().getAbsolutePath());
            if (worldCloneFolder.equals(worldTemplate)) {
                deleteFolder(new File(worldTemplate + File.separator + "uid.dat"));
                return true;
            }
            File toRename = new File(Bukkit.getServer().getWorldContainer(), worldTemplate);
            if (toRename.exists()) {
                deleteFolder(new File(worldTemplate + File.separator + "uid.dat"));
                return toRename.renameTo(new File(Bukkit.getWorldContainer(), worldCloneFolder));
            } else {
                return false;
            }
        }
        return true;
    }
}
