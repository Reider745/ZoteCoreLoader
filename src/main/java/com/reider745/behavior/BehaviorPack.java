package com.reider745.behavior;

import com.reider745.behavior.entities.EntitiesContent;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.horizon.util.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BehaviorPack {
    private static final ArrayList<BehaviorContent> contents_api = new ArrayList<>();

    static {
        contents_api.add(new EntitiesContent());
    }

    private final String path;
    public String name = "Not-load";

    public BehaviorPack(String path){
        this.path = path + "/";
    }

    protected void loadManifest2(JSONObject json){
        loadManifest1(json);
    }

    protected void loadManifest1(JSONObject json){
        final JSONObject header = json.getJSONObject("header");

        name = header.getString("name");
    }

    protected void loadManifest(){
        File file = new File(this.path+"manifest.json");
        if(file.exists()){
            try {
                final JSONObject json = FileUtils.readJSON(file);
                final int format = json.getInt("format_version");
                switch (format){
                    case 1 -> loadManifest1(json);
                    case 2 -> loadManifest2(json);//Отличий не нашел
                    default -> throw new RuntimeException("Not support manifest version "+format);
                }
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    public void load(){
        loadManifest();

        Logger.warning("Loaded behavior-pack "+name);

        final File contentsFile = new File(path + "contents.json");
        if(contentsFile.exists()) {
            try {
                final JSONObject contents = FileUtils.readJSON(contentsFile);
                if (contents.has("content")) {
                    JSONArray content = contents.getJSONArray("content");

                    for (int i = 0; i < content.length(); i++) {
                        JSONObject json = content.getJSONObject(i);

                        loadFileContext(this.path + json.getString("path"));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for(BehaviorContent content : contents_api){
            final String pathFolder = this.path + content.getDefaultDirectory();
            final String[] files = new File(pathFolder).list();
            for(String file : files)
                loadFileContext(pathFolder+"/"+file);
        }
    }


    public void loadFileContext(String path){
        try{
            final JSONObject contents = FileUtils.readJSON(new File(path));

            for(BehaviorContent content : contents_api){
                if(contents.has(content.getTagName())){
                    content.load(contents);
                    return;
                }
            }

            throw new RuntimeException("Not support "+path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
