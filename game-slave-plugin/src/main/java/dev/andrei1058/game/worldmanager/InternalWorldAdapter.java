package dev.andrei1058.game.worldmanager;

import co.aikar.taskchain.TaskChain;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.setup.SetupSession;
import dev.andrei1058.game.api.world.WorldAdapter;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.setup.SetupManager;
import dev.andrei1058.game.worldmanager.generator.VoidChunkGenerator;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ExcludeFileFilter;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class InternalWorldAdapter implements WorldAdapter {

    private final File backupFolder = new File(SteveSus.getInstance().getDataFolder(), "Worlds");
    private final Queue<LoadQueue> queue = new LinkedList<>();

    public InternalWorldAdapter() {
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
                    if (fl.getName().contains(ArenaManager.WORLD_NAME_SEPARATOR)) {
                        deleteFolder(fl);
                    }
                }
            }
        }
    }

    @Override
    public void onArenaEnableQueue(String worldToClone, Arena arena) {
        TaskChain<?> chain = SteveSus.newChain();

        String gameSessionWorld = worldToClone + ArenaManager.WORLD_NAME_SEPARATOR + arena.getGameId();
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
                        SteveSus.newChain().sync(() -> ArenaManager.getINSTANCE().removeFromEnableQueue(gameSessionWorld)).execute();
                        chain.abortChain();
                        nextInQueue();
                    }
                });
            } else {
                SteveSus.getInstance().getLogger().severe(worldToClone + " world was requested by an arena but it was not found!");
                ArenaManager.getINSTANCE().removeFromEnableQueue(gameSessionWorld);
                return;
            }
        }

        // UnZip world cache
        chain.delay(20).async(() -> {
            try {
                SteveSus.getInstance().getLogger().info("Unzipping world backup(" + worldToClone + ") into " + gameSessionWorld + ".");
                if (!unZip(worldToClone, gameSessionWorld)) {
                    SteveSus.getInstance().getLogger().severe("Could not unzip cache of  " + worldToClone + "!");
                    SteveSus.newChain().sync(() -> ArenaManager.getINSTANCE().removeFromEnableQueue(gameSessionWorld)).execute();
                    chain.abortChain();
                    nextInQueue();
                }
            } catch (Exception e) {
                e.printStackTrace();
                SteveSus.getInstance().getLogger().severe("Could not unzip cache of  " + worldToClone + "!");
                SteveSus.newChain().sync(() -> ArenaManager.getINSTANCE().removeFromEnableQueue(gameSessionWorld)).execute();
                chain.abortChain();
                nextInQueue();
            }
        });

        if (Bukkit.getWorld(gameSessionWorld) == null) {
            WorldCreator worldCreator = new WorldCreator(gameSessionWorld);
            worldCreator.generator(new VoidChunkGenerator());

            chain.delay(10).sync(() -> {
                try {
                    Bukkit.createWorld(worldCreator);
                } catch (Exception ex) {
                    SteveSus.getInstance().getLogger().severe("Could not load world: " + gameSessionWorld + " (" + worldToClone + ").");
                    SteveSus.newChain().sync(() -> ArenaManager.getINSTANCE().removeFromEnableQueue(gameSessionWorld)).execute();
                    ArenaManager.getINSTANCE().removeFromEnableQueue(gameSessionWorld);
                    chain.abortChain();
                    nextInQueue();
                }
            });
        } else {
            // If the world is already loaded initialize the arena
            arena.init(Bukkit.getWorld(worldToClone));
            ArenaManager.getINSTANCE().removeFromEnableQueue(gameSessionWorld);
            chain.abortChain();
            nextInQueue();
            return;
        }

        queue(gameSessionWorld, chain);
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
                        nextInQueue();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SteveSus.getInstance().getLogger().severe("Could not unzip cache for " + worldName + "!");
                    SetupManager.getINSTANCE().removeSession(setupSession);
                    chain.abortChain();
                    nextInQueue();
                }
            });
        }
        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.generator(new VoidChunkGenerator());

        chain.sync(() -> {
            try {
                Bukkit.createWorld(worldCreator);
            } catch (Exception ex) {
                SteveSus.getInstance().getLogger().severe("Could not load world: " + worldName);
                SetupManager.getINSTANCE().removeSession(setupSession);
                chain.abortChain();
                nextInQueue();
            }
        });
        queue(worldName, chain);
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
                    if (dat.exists() && !fl.getName().contains(ArenaManager.WORLD_NAME_SEPARATOR)) {
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

    public void nextInQueue() {
        if (!queue.isEmpty()) {
            queue.remove();
        }
        if (!queue.isEmpty()) {
            LoadQueue loadQueue = queue.peek();
            loadQueue.getTaskChain().execute();
        }
    }

    public static class LoadQueue {

        private final String expectedWorldName;
        private final TaskChain<?> taskChain;

        public LoadQueue(String expectedWorldName, TaskChain<?> taskChain) {
            this.expectedWorldName = expectedWorldName;
            this.taskChain = taskChain;
        }

        public String getExpectedWorldName() {
            return expectedWorldName;
        }

        public TaskChain<?> getTaskChain() {
            return taskChain;
        }
    }

    public Queue<LoadQueue> getQueue() {
        return queue;
    }

    private void queue(String world, TaskChain<?> taskChain) {
        // de queued on world load
        queue.add(new LoadQueue(world, taskChain));
        //chain.execute();
        if (queue.size() == 1) {
            LoadQueue loadQueue = queue.peek();
            loadQueue.getTaskChain().execute();
        }
    }
}
