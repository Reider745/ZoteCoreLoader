package com.reider745.event;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginDescription;
import cn.nukkit.plugin.PluginLoader;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.Utils;

public class InnerCorePlugin extends PluginBase implements PluginLoader {
    private File configFile;
    private Config config;

    @Override
    public Config getConfig() {
        if (this.config == null) {
            this.reloadConfig();
        }

        return this.config;
    }

    protected File getConfigFile() {
        if (this.configFile == null) {
            if (!isInitialized()) {
                throw new UnsupportedOperationException("Plugin should be initialized before getting config file!");
            }
            this.configFile = new File(getDataFolder(), "zotecore.yml");
        }

        return this.configFile;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void reloadConfig() {
        final InputStream configStream = getResource("zotecore.yml");
        if (configStream != null) {
            final LoadSettings settings = LoadSettings.builder().setParseComments(false).build();
            final Load yaml = new Load(settings);

            try {
                this.config = new Config(getConfigFile(), -1,
                        (LinkedHashMap<String, Object>) yaml.loadFromString(Utils.readFile(getConfigFile())));
            } catch (IOException exc) {
                Server.getInstance().getLogger().logException(exc);
            }
        }
        if (this.config == null) {
            this.config = new Config(getConfigFile());
        }
    }

    @Override
    public void saveDefaultConfig() {
        final File configFile = getConfigFile();
        if (!configFile.exists()) {
            saveResource("zotecore.yml", configFile.getName(), false);
        }
    }

    @Override
    public PluginLoader getPluginLoader() {
        return this;
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        if (plugin.equals(this)) {
            setEnabled(false);
            return;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        if (plugin.equals(this)) {
            setEnabled();
            return;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public PluginDescription getPluginDescription(String name) {
        if (getName().equals(name)) {
            return getDescription();
        }
        return null;
    }

    @Override
    public PluginDescription getPluginDescription(File file) {
        if (getFile().equals(file)) {
            return getDescription();
        }
        return null;
    }

    @Override
    public Pattern[] getPluginFilters() {
        return new Pattern[0];
    }

    @Override
    public Plugin loadPlugin(String name) throws Exception {
        if (getName().equals(name)) {
            return this;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Plugin loadPlugin(File file) throws Exception {
        if (getFile().equals(file)) {
            return this;
        }
        throw new UnsupportedOperationException();
    }
}
