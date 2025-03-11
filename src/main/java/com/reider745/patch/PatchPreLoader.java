package com.reider745.patch;

import com.reider745.Main;
import com.reider745.api.hooks.JarEditor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PatchPreLoader {
    private final File patches;
    private final ClassLoader loader;

    public PatchPreLoader(String path) throws MalformedURLException {
        this.patches = new File(path);
        if(patches.exists()) {
            final List<URL> urls = new ArrayList<>();

            for(File patch : Objects.requireNonNull(patches.listFiles((dir, name) -> name.endsWith(".jar")))) {
                urls.add(patch.toURL());
            }

            loader = new URLClassLoader(urls.toArray(new URL[0]));
        } else {
            loader = PatchPreLoader.class.getClassLoader();
        }
    }

    public ClassLoader getPatchLoader() {
        return loader;
    }

    public void init() {
        for(File patch : Objects.requireNonNull(patches.listFiles((dir, name) -> name.endsWith(".jar")))) {
            try {
                final URLClassLoader mainLoader = new URLClassLoader(new URL[]{patch.toURL()});
                final JSONObject description = new JSONObject(new String(mainLoader.getResourceAsStream("patch.json").readAllBytes()));

                final JSONArray list = description.optJSONArray("hooks", null);
                if(list != null)
                    for(int i = 0;i < list.length();i++)
                        Main.INSTANCE.add(list.optString(i));

                final String main = description.optString("main", null);
                if(main != null) {
                    Class<?> clazz = mainLoader.loadClass(main);
                    clazz.getMethod("onPreLoader").invoke(clazz.getConstructor().newInstance());

                    clazz = Main.INSTANCE.loader.getClassLoader().loadClass(main);
                    clazz.getMethod("onMainLoader").invoke(clazz.getConstructor().newInstance());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
