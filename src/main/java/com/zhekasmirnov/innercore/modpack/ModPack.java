package com.zhekasmirnov.innercore.modpack;

import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.modpack.installation.ModPackExtractionTarget;
import com.zhekasmirnov.innercore.modpack.installation.ModpackInstallationSource;
import com.zhekasmirnov.innercore.modpack.strategy.extract.DirectoryExtractStrategy;
import com.zhekasmirnov.innercore.utils.FileTools;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/*
 * desired mod pack structure:
 * root/
 *      -- core --
 *      mods/ <- recreated during update
 *      mod_assets/ <- recreated during update, used to store additional mod resources and resource overrides
 *      config/ <- merge during update
 *        keep/ <- original config files stored here to resolve changes during merge
 *        ... <- all json files here are merged, including static ids
 *      cache/ <- deleted during update, so all cache will be re-created
 *      modpack.json <- mod pack declaration
 *
 *      -- additional, recreated during update --
 *      texture_packs/ <- used to store custom texture packs, could be already selected and enabled
 *      resource_packs/ <- global resource packs
 *      behavior_packs/ <- global behavior packs
 */

public class ModPack {
    private final File rootDirectory;
    private final ModPackManifest manifest = new ModPackManifest();
    private final ModPackPreferences preferences;

    private final List<ModPackDirectory> defaultDirectories = new ArrayList<>();
    private final List<ModPackDirectory> declaredDirectories = new ArrayList<>();

    private final ModPackJsAdapter jsAdapter;

    public ModPack(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        this.preferences = new ModPackPreferences(this, "preferences.json");
        this.jsAdapter = new ModPackJsAdapter(this);
    }

    public ModPack addDirectory(ModPackDirectory directory) {
        directory.assignToModPack(this);
        defaultDirectories.add(directory);
        return this;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public File getManifestFile() {
        return new File(rootDirectory, "modpack.json");
    }

    public File getIconFile() {
        return new File(rootDirectory, "pack_icon.png");
    }

    public ModPackManifest getManifest() {
        return manifest;
    }

    public ModPackPreferences getPreferences() {
        return preferences;
    }

    public ModPackJsAdapter getJsAdapter() {
        return jsAdapter;
    }

    public boolean reloadAndValidateManifest() {
        try {
            if (!getManifestFile().exists()) {
                return false;
            }
            declaredDirectories.clear();
            manifest.loadFile(getManifestFile());
            declaredDirectories.addAll(manifest.createDeclaredDirectoriesForModPack(this));
            return manifest.getPackName() != null;
        } catch (IOException | JSONException exception) {
            Logger.warning("ModPack.reloadAndValidateManifest()", exception);
            return false;
        }
    }

    public List<ModPackDirectory> getAllDirectories() {
        List<ModPackDirectory> directories = new ArrayList<>(defaultDirectories);
        directories.addAll(declaredDirectories);
        return directories;
    }

    public List<ModPackDirectory> getDirectoriesOfType(ModPackDirectory.DirectoryType type) {
        List<ModPackDirectory> result = new ArrayList<>();
        for (ModPackDirectory directory : defaultDirectories) {
            if (type == directory.getType()) {
                result.add(directory);
            }
        }
        for (ModPackDirectory directory : declaredDirectories) {
            if (type == directory.getType()) {
                result.add(directory);
            }
        }
        return result;
    }

    public ModPackDirectory getDirectoryOfType(ModPackDirectory.DirectoryType type) {
        List<ModPackDirectory> directories = getDirectoriesOfType(type);
        return directories.size() > 0 ? directories.get(0) : null;
    }

    public DirectorySetRequestHandler getRequestHandler(ModPackDirectory.DirectoryType type) {
        return new DirectorySetRequestHandler(getDirectoriesOfType(type));
    }

    public interface TaskReporter {
        void reportError(String logMessage, Exception exception, boolean isFatal) throws InterruptedException;

        void reportProgress(String logMessage, int stage, int progress, int maxProgress) throws InterruptedException;

        void reportResult(boolean success);
    }

    public static void interruptTask(Exception exception, String message) throws InterruptedException {
        throw (InterruptedException) new InterruptedException(message).initCause(exception);
    }

    public static void interruptTask(String message) throws InterruptedException {
        throw new InterruptedException(message);
    }

    public synchronized void installOrUpdate(ModpackInstallationSource source, TaskReporter reporter)
            throws InterruptedException {
        Enumeration<ModpackInstallationSource.Entry> entries = source.entries();

        try {
            File manifestFile = getManifestFile();
            manifestFile.getParentFile().mkdirs();
            FileTools.writeFileText(manifestFile, source.getManifestContent());
            manifest.loadFile(manifestFile);
            reporter.reportProgress("loaded manifest", 0, 1, 1);
        } catch (IOException | JSONException exception) {
            reporter.reportResult(false);
            reporter.reportError("failed to get pack manifest", exception, true);
            interruptTask(exception, "failed to get pack manifest");
        }

        List<ModPackDirectory> directories = new ArrayList<>(defaultDirectories);
        directories.addAll(manifest.createDeclaredDirectoriesForModPack(this));

        int progress = 0;
        for (ModPackDirectory directory : directories) {
            try {
                reporter.reportProgress("preparing directory " + directory, 1, ++progress, directories.size());
                directory.getUpdateStrategy().beginUpdate();
            } catch (IOException exception) {
                reporter.reportError("failed to begin installation for directory " + directory, exception, false);
            }
        }

        progress = 0;
        int entryCount = source.getEntryCount();
        while (entries.hasMoreElements()) {
            ModpackInstallationSource.Entry entry = entries.nextElement();
            String name = entry.getName();
            reporter.reportProgress("updating entry " + name, 2, ++progress, entryCount);
            for (ModPackDirectory directory : directories) {
                String localPath = directory.getLocalPathFromEntry(name);
                if (localPath != null) {
                    try (InputStream inputStream = entry.getInputStream()) {
                        directory.getUpdateStrategy().updateFile(localPath, inputStream);
                    } catch (IOException exception) {
                        reporter.reportError("failed to update entry " + name + " (local path " + localPath
                                + ") in directory " + directory, exception, false);
                    }
                }
            }
        }

        progress = 0;
        for (ModPackDirectory directory : directories) {
            try {
                reporter.reportProgress("completing directory " + directory, 3, ++progress, directories.size());
                directory.getUpdateStrategy().finishUpdate();
            } catch (IOException exception) {
                reporter.reportError("failed to complete installation for directory " + directory, exception, false);
            }
        }

        reporter.reportResult(true);
    }

    public synchronized void extract(ModPackExtractionTarget target, TaskReporter reporter)
            throws InterruptedException {
        reloadAndValidateManifest();

        try {
            reporter.reportProgress("extracting modpack", 0, 0, 1);
            target.writeFile("modpack.json", getManifestFile());
            File iconFile = getIconFile();
            if (iconFile.exists()) {
                target.writeFile("pack_icon.png", iconFile);
            }
        } catch (IOException exception) {
            reporter.reportError("failed to extract manifest", exception, true);
            interruptTask(exception, "failed to extract manifest");
        }

        List<ModPackDirectory> directories = new ArrayList<>(defaultDirectories);
        directories.addAll(declaredDirectories);

        int stage = 0;
        for (ModPackDirectory directory : directories) {
            DirectoryExtractStrategy extractStrategy = directory.getExtractStrategy();
            List<File> filesToExtract = extractStrategy.getFilesToExtract();

            int index = 0;
            stage++;
            for (File file : filesToExtract) {
                String name = extractStrategy.getFullEntryName(file);
                try {
                    reporter.reportProgress("extracting entry " + name, stage, ++index, filesToExtract.size());
                    target.writeFile(name, file);
                } catch (IOException exception) {
                    reporter.reportError("exception in extracting entry " + name, exception, false);
                }
            }
        }
    }
}
