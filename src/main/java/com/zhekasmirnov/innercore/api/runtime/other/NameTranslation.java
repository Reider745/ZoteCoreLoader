package com.zhekasmirnov.innercore.api.runtime.other;

import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.utils.FileTools;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by zheka on 20.09.2017.
 */

public class NameTranslation {
    private static HashMap<String, HashMap<Integer, String>> allLanguageTranslations = new HashMap<>();
    private static HashMap<Integer, String> currentLanguageTranslations = new HashMap<>();
    private static HashMap<Integer, String> defaultLanguageTranslations = new HashMap<>();

    private static HashMap<Integer, String> getTranslationMap(String language) {
        if (allLanguageTranslations.containsKey(language)) {
            return allLanguageTranslations.get(language);
        } else {
            HashMap<Integer, String> map = new HashMap<>();
            allLanguageTranslations.put(language, map);
            return map;
        }
    }

    private static String toShortName(String lang) {
        int index = lang.indexOf('_');
        if (index == -1) {
            return lang;
        } else {
            return lang.substring(0, index);
        }
    }

    private static String language = "en";

    public static void loadBuiltinTranslations() {
        try {
            JSONObject translations = FileTools.getAssetAsJSON("innercore/builtin_translations.json");
            for (Iterator<String> it = translations.keys(); it.hasNext();) {
                String name = it.next();
                JSONObject languages = translations.getJSONObject(name);
                for (Iterator<String> it2 = languages.keys(); it2.hasNext();) {
                    String lang = it2.next();
                    String translation = languages.getString(lang);
                    addSingleTranslation(lang, name, translation);
                }
            }
        } catch (JSONException e) {
            ICLog.e("TRANSLATION", "failed to load builtin translations", e);
        }
    }

    public static void setLanguage(String lang) {
        language = toShortName(lang);
        currentLanguageTranslations = getTranslationMap(language);
        defaultLanguageTranslations = getTranslationMap("en");
        ICLog.d("TRANSLATION", "set game language to " + language + " (full name is " + lang + ")");
    }

    public static String getLanguage() {
        return language;
    }

    public static void addSingleTranslation(String lang, String origin, String translation) {
        HashMap<Integer, String> map = getTranslationMap(toShortName(lang));
        map.put(origin.hashCode(), translation);
    }

    public static void addTranslation(String origin, HashMap<String, String> translations) {
        Set<String> langs = translations.keySet();
        for (String lang : langs) {
            addSingleTranslation(lang, origin, translations.get(lang));
        }
    }

    public static void addTranslation(String origin, ScriptableObject translations) {
        Object[] keys = translations.getAllIds();
        HashMap<String, String> map = new HashMap<>();
        for (Object key : keys) {
            if (key instanceof String) {
                map.put((String) key, "" + translations.get(key));
            }
        }
        addTranslation(origin, map);
    }

    public static String translate(String str) {
        if (str == null) {
            return null;
        }
        String result = currentLanguageTranslations.get(str.hashCode());
        if (result != null) {
            return result;
        }
        result = defaultLanguageTranslations.get(str.hashCode());
        if (result != null) {
            return result;
        }
        return str;
    }

    private static final HashMap<Integer, String> namesToGenerateCache = new HashMap<>();

    public static void refresh(boolean sendOverrideCache) {
        File file = new File(FileTools.DIR_MINECRAFT + "minecraftpe/", "options.txt");
        try {
            String content = FileTools.readFileText(file.getAbsolutePath());
            String[] lines = content.split("\n");
            for (String line : lines) {
                String[] opts = line.split(":");
                if (opts[0].equals("game_language")) {
                    if (opts.length == 2) {
                        setLanguage(opts[1]);
                    } else {
                        refreshFromNative();
                    }
                    return;
                }
            }
            refreshFromNative();
        } catch (Throwable e) {
            refreshFromNative();
        }

        if (sendOverrideCache) {
            synchronized (namesToGenerateCache) {
                for (Integer idData : namesToGenerateCache.keySet()) {
                    String name = namesToGenerateCache.get(idData);
                    if (name != null) {
                        NativeAPI.sendCachedItemNameOverride(idData / 16, idData % 16, translate(name));
                    }
                }
            }
        }
    }

    private static void refreshFromNative() {
        ICLog.d("TRANSLATION", "failed to get language settings from file, trying native method");
        String language = NativeAPI.getGameLanguage();
        if (language != null) {
            setLanguage(language);
        } else {
            ICLog.d("TRANSLATION", "failed to get language settings");
            setLanguage("en_US");
        }
    }

    public static void sendNameToGenerateCache(int id, int data, String name) {
        synchronized (namesToGenerateCache) {
            data = Math.min(15, Math.max(0, data));
            namesToGenerateCache.put(id * 16 + data, name);
        }
    }

    public static boolean isAscii(String str) {
        return StandardCharsets.US_ASCII.newEncoder().canEncode(str);
    }

    public static String fixUnicodeIfRequired(String nameId, String name) {
        if (name.length() == 0) {
            return "error.blank_name";
        }
        if (isAscii(name)) {
            return name;
        }
        String alias = nameId + ".name";
        addSingleTranslation("en", alias, name);
        return alias;
    }
}
