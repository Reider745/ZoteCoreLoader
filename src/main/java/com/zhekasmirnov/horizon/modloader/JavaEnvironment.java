package com.zhekasmirnov.horizon.modloader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.zhekasmirnov.horizon.modloader.java.JavaDirectory;
import com.zhekasmirnov.horizon.modloader.java.JavaLibrary;
import com.zhekasmirnov.horizon.runtime.logger.Logger;

public class JavaEnvironment {
    private final Set<JavaDirectory> javaDirectories = new HashSet<>();

    public void addDirectory(JavaDirectory directory) {
        if (directory != null) {
            this.javaDirectories.add(directory);
        }
    }

    public void build() {
        List<JavaLibrary> javaLibraries = new ArrayList<>();
        for (JavaDirectory javaDir : this.javaDirectories) {
            try {
                javaLibraries.add(javaDir.addToExecutionDirectory());
            } catch (Throwable err) {
                Logger.error("BUILD", "details: lang=java dir=" + javaDir.directory, err);
            }
        }
        for (JavaLibrary javaLibrary : javaLibraries) {
            if (!javaLibrary.isInitialized()) {
                try {
                    javaLibrary.initialize();
                } catch (Throwable err) {
                    Logger.error("LOAD", "failed to load java library " + javaLibrary.getDirectory(), err);
                }
            }
        }
    }
}
