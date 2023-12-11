package com.zhekasmirnov.innercore.modpack.installation;

import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileInstallationSource extends ModpackInstallationSource {
    protected ZipFile file;
    protected String manifestContent;

    public ZipFileInstallationSource(ZipFile file) {
        setFile(file);
    }

    public ZipFileInstallationSource() {
    }

    public void setFile(ZipFile file) {
        this.file = file;
    }

    public ZipFile getFile() {
        return file;
    }

    @Override
    public String getManifestContent() throws IOException {
        if (manifestContent == null) {
            ZipEntry entry = file.getEntry("modpack.json");
            if (entry == null) {
                throw new IOException("modpack zip file does not contain modpack.json");
            }
            manifestContent = FileTools.convertStreamToString(file.getInputStream(entry));
        }
        return manifestContent;
    }

    @Override
    public int getEntryCount() {
        return file.size();
    }

    @Override
    public Enumeration<Entry> entries() {
        Enumeration<? extends ZipEntry> entries = file.entries();
        return new Enumeration<Entry>() {
            @Override
            public boolean hasMoreElements() {
                return entries.hasMoreElements();
            }

            @Override
            public Entry nextElement() {
                ZipEntry entry = entries.nextElement();
                return new Entry() {
                    @Override
                    public String getName() {
                        return entry.getName();
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return file.getInputStream(entry);
                    }
                };
            }
        };
    }
}
