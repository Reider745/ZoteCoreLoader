package com.zhekasmirnov.innercore.mod.executable.library;

import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.ui.LoadingUI;
import org.mozilla.javascript.Scriptable;

import java.util.*;

/**
 * Created by zheka on 24.02.2018.
 */

public class LibraryRegistry {
    private static final List<Library> allLibraries = new ArrayList<>();
    private static final List<Library> builtInLibraries = new ArrayList<>();
    private static final Map<Mod, ArrayList<Library>> libraryMap = new HashMap<>();

    public static void addLibrary(Library library) {
        Mod mod = library.getParentMod();

        if (libraryMap.containsKey(mod)) {
            libraryMap.get(mod).add(library);
        } else {
            ArrayList<Library> list = new ArrayList<>();
            list.add(library);
            libraryMap.put(mod, list);
        }

        allLibraries.add(library);
    }

    private static Library resolveDependencyInList(Collection<Library> libs, LibraryDependency dependency) {
        Library result = null;
        for (Library library : libs) {
            if (!library.isInvalid() && dependency.isMatchesLib(library)) {
                if (result == null || result.getVersionCode() < library.getVersionCode()) {
                    result = library;
                }
            }
        }
        return result;
    }

    private static Library resolveSharedDependency(LibraryDependency dependency) {
        return resolveDependencyInList(allLibraries, dependency);
    }

    private static Library resolveLocalDependency(LibraryDependency dependency) {
        Mod mod = dependency.getParentMod();
        if (mod != null) {
            ArrayList<Library> localLibs = new ArrayList<>(builtInLibraries);
            ArrayList<Library> libsForMod = libraryMap.get(mod);
            if (libsForMod != null) {
                localLibs.addAll(libsForMod);
            }
            return resolveDependencyInList(localLibs, dependency);
        }
        return null;
    }

    public static Library resolveDependency(LibraryDependency dependency) {
        Library lib = resolveLocalDependency(dependency);
        if (lib != null) {
            if (lib.isShared()) {
                lib = resolveSharedDependency(dependency);
            }
        } else {
            lib = resolveSharedDependency(dependency);
        }

        return lib;
    }

    public static Library resolveDependencyAndLoadLib(LibraryDependency dependency) {
        LoadingUI.setTip("Resolving dependency: " + dependency);

        Library library = resolveDependency(dependency);
        if (library != null) {
            if (!library.isLoaded()) {
                if (!library.isLoadingInProgress()) {
                    library.load();
                } else {
                    ICLog.i("ERROR",
                            "DEPENDENCY RECURSION DETECTED! it may be caused by recursive library dependencies or recursive imports, recursion detected at: "
                                    + dependency);
                    return null;
                }
            }
            if (library.isInvalid()) {
                ICLog.i("ERROR",
                        "incorrectly loaded library found for dependency " + dependency + " searching other matches");
                return resolveDependencyAndLoadLib(dependency);
            }
        }

        LoadingUI.setTip("");
        return library;
    }

    private static void importLibraryInternal(Scriptable scope, Library library, LibraryDependency dependency,
            String exportName) {
        if (exportName.equals("*")) {
            Collection<String> exports = library.getExportNames();
            for (String export : exports) {
                importLibraryInternal(scope, library, dependency, export);
            }
        } else {
            LibraryExport export = library.getExportForDependency(dependency, exportName);
            if (export != null) {
                scope.put(export.name, scope, export.value);
            } else {
                ICLog.i("ERROR", "failed to import value " + exportName + " from " + dependency
                        + ", library does not have value with this name to import");
            }
        }
    }

    public static void importLibrary(Scriptable scope, LibraryDependency dependency, String exportName) {
        Library library = resolveDependencyAndLoadLib(dependency);
        if (library != null) {
            importLibraryInternal(scope, library, dependency, exportName);
        } else {
            ICLog.i("ERROR", "failed to import library " + dependency + ", it does not exist or failed to load");
        }
    }

    public static void addBuiltInLibrary(Library library) {
        allLibraries.add(library);
        builtInLibraries.add(library);
    }

    public static void loadAllBuiltInLibraries() {
    }

    public static void prepareAllLibraries() {
        for (Library library : allLibraries) {
            if (!library.isInvalid() && !library.isPrepared()) {
                library.initialize();
                if (!library.isInitialized()) {
                    ICLog.i("ERROR", "library failed to initialize for some reason: " + library.getLibName());
                }
            }
        }
    }
}
