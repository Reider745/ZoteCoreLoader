package com.zhekasmirnov.innercore.mod.build;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zheka on 30.07.2017.
 */

public class BuildHelper {
    private static void addFileTree(File file, ArrayList<File> files) {
        if (file.isFile()) {
            if (!".includes".equals(file.getName())) {
                files.add(file);
            }
        } else if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                addFileTree(child, files);
            }
        }
    }

    public static ArrayList<File> readIncludesFile(File dir) throws IOException {
        File includes = new File(dir, ".includes");
        String content = FileTools.readFileText(includes.getAbsolutePath());
        String[] lines = content.split("\n");

        ArrayList<File> includedFiles = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.length() == 0 || line.startsWith("//") || line.startsWith("#"))
                continue;

            File includedFile = new File(dir, line);
            if (includedFile.exists()) {
                if (includedFile.isFile()) {
                    includedFiles.add(includedFile);
                } else {
                    if (!line.endsWith("/.")) {
                        ICLog.i("ERROR", "directories in .includes should end with '/.' - " + line);
                    }
                    addFileTree(includedFile, includedFiles);
                }
            }
            else {
                ICLog.d(ModBuilder.LOGGER_TAG, "failed to include file due it does not exist: " + line);
            }
        }

        return includedFiles;
    }

    public static void buildDir(File dir, File target, File parentDir) {
        if (!dir.exists() || !dir.isDirectory()) {
            ICLog.d(ModBuilder.LOGGER_TAG, "failed to build dir " + FileTools.getPrettyPath(parentDir, dir) + " it does not exist or not a directory.");
            return;
        }

        try {
            ArrayList<File> includedFiles = readIncludesFile(dir);
            String buildedCode = "/*\n" + "BUILD INFO:\n" + "  dir: " + FileTools.getPrettyPath(parentDir, dir) + "\n" + "  target: " + FileTools.getPrettyPath(parentDir, target) + "\n  files: " + includedFiles.size() + "\n*/\n\n\n\n";

            for (int i = 0; i < includedFiles.size(); i++) {
                File file = includedFiles.get(i);
                String fileText = FileTools.readFileText(file.getAbsolutePath());
                buildedCode += "// file: " + FileTools.getPrettyPath(dir, file) + "\n\n" + fileText + "\n\n\n\n";
            }

            FileTools.assureFileDir(target);
            FileTools.writeFileText(target.getAbsolutePath(), buildedCode);

            ICLog.d(ModBuilder.LOGGER_TAG, "directory build succeeded: dir=" + FileTools.getPrettyPath(parentDir, dir) + " target=" + FileTools.getPrettyPath(parentDir, target));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void buildDir(String parentDir, BuildConfig.BuildableDir buildableDir) {
        buildDir(new File(parentDir, buildableDir.dir), new File(parentDir, buildableDir.targetSource), new File(parentDir));
    }

}
