package com.zhekasmirnov.innercore.modpack.installation;

import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class ExternalZipFileInstallationSource extends ZipFileInstallationSource implements Closeable {
    public ExternalZipFileInstallationSource(InputStream inputStream) throws IOException {
        super();
        File tmpFile = new File(FileTools.DIR_WORK, "temp/modpack_tmp");
        tmpFile.getParentFile().mkdirs();
        tmpFile.delete();

        FileTools.unpackInputStream(inputStream, tmpFile.getAbsolutePath());
        setFile(new ZipFile(tmpFile));
    }

    @Override
    public void close() throws IOException {
    }
}
