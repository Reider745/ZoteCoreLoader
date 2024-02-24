package com.zhekasmirnov.innercore.api.mod.adaptedscript;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.zhekasmirnov.innercore.api.InnerCoreConfig;
import com.zhekasmirnov.innercore.api.annotations.APIStaticModule;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.mod.build.ExtractionHelper;
import com.zhekasmirnov.innercore.mod.build.Mod;
import com.zhekasmirnov.innercore.mod.build.ModLoader;
import com.zhekasmirnov.innercore.mod.executable.Compiler;
import com.zhekasmirnov.innercore.utils.FileTools;
import com.zhekasmirnov.innercore.utils.IMessageReceiver;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by zheka on 13.08.2017.
 */

public class PreferencesWindowAPI extends AdaptedScriptAPI {
    @Override
    public String getName() {
        return "PrefsWinAPI";
    }

    @JSStaticFunction
    public static void log(String str) {
        ICLog.d("PREFS", str);
    }

    @APIStaticModule
    public static class Prefs {
        @JSStaticFunction
        public static ArrayList<Mod> getModList() {
            return ModLoader.instance.modsList;
        }

        @JSStaticFunction
        public static boolean compileMod(Object mod, Object logger) {
            return Compiler.compileMod((Mod) Context.jsToJava(mod, Mod.class),
                    (IMessageReceiver) Context.jsToJava(logger, IMessageReceiver.class));
        }

        @JSStaticFunction
        public static com.zhekasmirnov.innercore.mod.build.Config getGlobalConfig() {
            return InnerCoreConfig.config;
        }

        @JSStaticFunction
        public static ArrayList<String> installModFile(String path, Object _log) {
            IMessageReceiver log = (IMessageReceiver) Context.jsToJava(_log, IMessageReceiver.class);
            return ExtractionHelper.extractICModFile(new File(path), log, null);
        }
    }

    @APIStaticModule
    public static class Network {
        @JSStaticFunction
        public static String getURLContents(String sURL) {
            try {
                URL url = new URL(sURL);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                StringBuilder result = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    result.append(inputLine);
                }
                in.close();

                return result.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @JSStaticFunction
        @Deprecated(since = "Zote")
        public static String downloadIcon(String sURL) {
            try {
                URL url = new URL(sURL);

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inScaled = false;
                Bitmap bmp = BitmapFactory.decodeStream(url.openStream());

                String name = "web_icn_" + sURL;
                if (bmp != null) {
                    com.zhekasmirnov.innercore.api.mod.ui.TextureSource.instance.put(name, bmp);
                }
                return name;
            } catch (IOException | OutOfMemoryError e) {
                e.printStackTrace();
                return "missing_texture";
            }
        }

        interface IDownloadHandler {
            void progress(float progress);

            void message(String message);

            boolean isCancelled();
        }

        @JSStaticFunction
        public static String downloadFile(String sUrl, Object _handler) {
            IDownloadHandler handler = (IDownloadHandler) Context.jsToJava(_handler, IDownloadHandler.class);

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            File outputFile = new File(FileTools.DIR_WORK,
                    "temp/download/" + sUrl.replaceAll("[/\\\\ :.]", "_") + ".icmod");
            FileTools.assureFileDir(outputFile);

            if (outputFile.exists()) {
                outputFile.delete();
            }

            try {
                URL url = new URL(sUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    handler.message("Server returned HTTP " + connection.getResponseCode() + " "
                            + connection.getResponseMessage());
                    return null;
                }

                int fileLength = connection.getContentLength();

                input = connection.getInputStream();
                output = new FileOutputStream(outputFile);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (handler.isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    if (fileLength > 0) {
                        handler.progress(total / (float) fileLength);
                    }
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                handler.message(e.toString());
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }

            return outputFile.getAbsolutePath();
        }
    }

    public static class WorkbenchRecipeListBuilder
            extends com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchRecipeListBuilder {
        public WorkbenchRecipeListBuilder(long player,
                com.zhekasmirnov.apparatus.api.container.ItemContainer container) {
            super(player, container);
        }
    }

    public static class WorkbenchRecipeListProcessor
            extends com.zhekasmirnov.innercore.api.mod.recipes.workbench.WorkbenchRecipeListProcessor {
        public WorkbenchRecipeListProcessor(ScriptableObject target) {
            super(target);
        }
    }
}
