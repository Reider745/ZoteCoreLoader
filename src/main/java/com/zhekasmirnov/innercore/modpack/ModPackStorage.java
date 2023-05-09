package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.horizon.util.FileUtils;
import com.zhekasmirnov.innercore.modpack.installation.ExternalZipFileInstallationSource;
import com.zhekasmirnov.innercore.modpack.installation.ModpackInstallationSource;
import com.zhekasmirnov.innercore.modpack.installation.ZipFileExtractionTarget;
import com.zhekasmirnov.innercore.modpack.installation.ZipFileInstallationSource;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipFile;

public class ModPackStorage {
    private final ModPackContext context = ModPackContext.getInstance();
    private final ModPackFactory factory = ModPackFactory.getInstance();

    private final File packsDirectory;
    private final File packsArchiveDirectory;
    private final File defaultModPackDirectory;

    private final ModPack defaultModPack;
    private final List<ModPack> modPacks = new ArrayList<>();

    public ModPackStorage(File packsDirectory, File packsArchiveDirectory, File defaultModPackDirectory) {
        this.packsDirectory = packsDirectory;
        this.packsArchiveDirectory = packsArchiveDirectory;
        this.defaultModPackDirectory = defaultModPackDirectory;
        packsDirectory.mkdirs();

        defaultModPack = factory.createDefault(defaultModPackDirectory);
    }

    public synchronized void rebuildModPackList() {
        modPacks.clear();
        File[] filesInDir = packsDirectory.listFiles();
        if (filesInDir != null) {
            for (File packDir : filesInDir) {
                if (packDir.isDirectory()) {
                    ModPack modPack = factory.createFromDirectory(packDir);
                    if (modPack.reloadAndValidateManifest()) {
                        modPacks.add(modPack);
                    }
                }
            }
        }
    }


    public ModPackContext getContext() {
        return context;
    }

    public File getPacksDirectory() {
        return packsDirectory;
    }

    public File getPacksArchiveDirectory() {
        return packsArchiveDirectory;
    }

    public ModPack getDefaultModPack() {
        return defaultModPack;
    }

    public File getDefaultModPackDirectory() {
        return defaultModPackDirectory;
    }

    public List<ModPack> getNonDefaultModPacks() {
        return modPacks;
    }

    public List<ModPack> getAllModPacks() {
        List<ModPack> result = new ArrayList<>();
        result.add(defaultModPack);
        result.addAll(getNonDefaultModPacks());
        return result;
    }

    public boolean isDefaultModPack(ModPack modPack) {
        return defaultModPackDirectory.equals(modPack.getRootDirectory());
    }


    private static String normalizeFileName(String name) {
        if (name == null || name.equals("")) {
            return "unnamed";
        }
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private static String getAvailablePackFileName(ModPackManifest manifest, Predicate<String> checkAvailable) {
        String name = normalizeFileName(manifest.getPackName());
        if (!checkAvailable.test(name)) {
            name += "-" + normalizeFileName(manifest.getDisplayedName());
            if (!checkAvailable.test(name)) {
                for (int index = 0;; index++) {
                    if (checkAvailable.test(name + "-" + index)) {
                        name += "-" + index;
                        break;
                    }
                }
            }
        }
        return name;
    }

    public ModPack installNewModPack(ModpackInstallationSource source, ModPack.TaskReporter taskReporter) throws InterruptedException {
        ModPackManifest manifest;
        try {
            manifest = source.getTempManifest();
        } catch (IOException | JSONException exception) {
            taskReporter.reportError("failed to get manifest from installation source", exception, true);
            ModPack.interruptTask(exception, "failed to get manifest");
            return null; // this should never happen, so function is non-null
        }


        File packDirectory = new File(packsDirectory, getAvailablePackFileName(manifest, name -> !new File(packsDirectory, name).exists()));
        packDirectory.mkdirs();

        if (!packDirectory.isDirectory()) {
            taskReporter.reportError("failed to create pack directory", new IOException(), true);
            ModPack.interruptTask("failed to create pack directory");
        }

        ModPack modPack = ModPackFactory.getInstance().createFromDirectory(packDirectory);
        modPack.installOrUpdate(source, taskReporter);

        if (modPack.reloadAndValidateManifest()) {
            modPacks.add(modPack);
        } else {
            rebuildModPackList();
        }

        return modPack;
    }

    public ModPack installNewModPack(InputStream inputStream, ModPack.TaskReporter taskReporter) throws InterruptedException {
        try (ExternalZipFileInstallationSource installationSource = new ExternalZipFileInstallationSource(inputStream)) {
            return installNewModPack(installationSource, taskReporter);
        } catch (IOException exception) {
            taskReporter.reportError("failed to create installation source", exception, false);
            ModPack.interruptTask(exception, "failed to create installation source");
            return null;
        }
    }

    public File archivePack(ModPack modPack, ModPack.TaskReporter taskReporter) throws InterruptedException {
        if (!modPack.reloadAndValidateManifest() || modPack.getManifest().getPackName() == null) {
            ModPack.interruptTask("failed to load pack manifest");
        }

        File archiveFile = new File(packsArchiveDirectory, getAvailablePackFileName(modPack.getManifest(), name -> !new File(packsArchiveDirectory, name + ".zip").exists()) + ".zip");
        archiveFile.getParentFile().mkdirs();

        try (ZipFileExtractionTarget extractionTarget = new ZipFileExtractionTarget(archiveFile)) {
            modPack.extract(extractionTarget, taskReporter);
        } catch (IOException exception) {
            archiveFile.delete();
            ModPack.interruptTask(exception, "failed to create extraction target");
        }

        taskReporter.reportResult(true);

        return archiveFile;
    }

    public void deletePack(ModPack modPack) {
        if (isDefaultModPack(modPack)) {
            throw new IllegalArgumentException("default modpack cannot be deleted");
        }

        modPacks.remove(modPack);
        //FileUtils.clearFileTree(modPack.getRootDirectory(), true);
    }

    public File archiveAndDeletePack(ModPack modPack, ModPack.TaskReporter taskReporter) throws InterruptedException {
        if (isDefaultModPack(modPack)) {
            throw new IllegalArgumentException("default modpack cannot be deleted");
        }

        File archive = archivePack(modPack, taskReporter);
        //FileUtils.clearFileTree(modPack.getRootDirectory(), true);
        return archive;
    }

    public ModPack unarchivePack(File archivedPackFile, ModPack.TaskReporter taskReporter, boolean deleteArchiveFile) throws InterruptedException {
        ZipFileInstallationSource installationSource;
        try {
            installationSource = new ZipFileInstallationSource(new ZipFile(archivedPackFile));
        } catch (IOException exception) {
            taskReporter.reportError("failed to create installation source", exception, true);
            ModPack.interruptTask(exception, "failed to create installation source");
            return null;
        }

        ModPack modPack = installNewModPack(installationSource, taskReporter);
        if (deleteArchiveFile) {
            archivedPackFile.delete();
        }
        return modPack;
    }

    public List<File> getAllArchivedPacks() {
        List<File> result = new ArrayList<>();
        File[] archivedFiles = packsArchiveDirectory.listFiles();
        if (archivedFiles != null) {
            result.addAll(Arrays.asList(archivedFiles));
        }
        return result;
    }
}
