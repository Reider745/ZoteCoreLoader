package com.zhekasmirnov.innercore.modpack.installation;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.zhekasmirnov.innercore.modpack.ModPack;
import com.zhekasmirnov.innercore.modpack.ModPackContext;
import com.zhekasmirnov.innercore.modpack.ModPackDirectory;
import com.zhekasmirnov.innercore.modpack.ModPackFactory;
import com.zhekasmirnov.innercore.modpack.ModPackStorage;
import com.zhekasmirnov.innercore.modpack.strategy.extract.DirectoryExtractStrategy;
import com.zhekasmirnov.innercore.utils.FileTools;

public class DirectoryInstallationSource extends ModpackInstallationSource implements Closeable {
    private final ModPack modpack;
    private Collection<Entry> extractEntries = null;

    public DirectoryInstallationSource(File directory) throws FileNotFoundException {
        if (!directory.isDirectory()) {
            throw new FileNotFoundException(directory.getPath() + " should be existing directory!");
        }
        ModPackStorage storage = ModPackContext.getInstance().getStorage();
        ModPack modpack = storage.getModPackByDirectory(directory);
        if (modpack == null) {
            modpack = ModPackFactory.getInstance().createFromDirectory(directory);
        }
        this.modpack = modpack;
    }

    public DirectoryInstallationSource(ModPack modpack) {
        if (modpack == null) {
            throw new NullPointerException("modpack == null");
        }
        this.modpack = modpack;
    }

    @Override
    public String getManifestContent() throws IOException {
        File manifestFile = modpack.getManifestFile();
        return FileTools.readFileText(manifestFile.getPath());
    }

    @Override
    public String getExternalServersContent() throws IOException {
        File externalServersFile = modpack.getExternalServersFile();
        return FileTools.readFileText(externalServersFile.getPath());
    }

    private Entry instantiateEntry(File file, DirectoryExtractStrategy extractStrategy) {
        return new Entry() {
            @Override
            public String getName() {
                return extractStrategy != null ? extractStrategy.getFullEntryName(file) : file.getName();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new FileInputStream(file);
            }
        };
    }

    public synchronized Collection<Entry> getExtractEntries() {
        if (extractEntries == null) {
            extractEntries = new ArrayList<>();
            for (ModPackDirectory directory : modpack.getAllDirectories()) {
                DirectoryExtractStrategy extractStrategy = directory.getExtractStrategy();
                List<File> filesToExtract = extractStrategy.getFilesToExtract();
                for (File file : filesToExtract) {
                    extractEntries.add(instantiateEntry(file, extractStrategy));
                }
            }
            File iconFile = modpack.getIconFile();
            if (iconFile.isFile()) {
                extractEntries.add(instantiateEntry(iconFile, null));
            }
        }
        return extractEntries;
    }

    @Override
    public int getEntryCount() {
        return getExtractEntries().size();
    }

    @Override
    public Enumeration<Entry> entries() {
        return Collections.enumeration(getExtractEntries());
    }

    @Override
    public void close() throws IOException {
        extractEntries = null;
    }
}
