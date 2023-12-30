package com.reider745.hooks;

import cn.nukkit.permission.BanList;
import com.reider745.api.ReflectHelper;
import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;

@Hooks(className = "cn.nukkit.permission.BanList")
public class GlobalBanList implements HookClass {
    private static final String[] banList;

    static {
        JSONArray list = new JSONArray();
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL("https://api.github.com/repos/Reider745/libs/contents/global_ban_list.json?ref=main").openConnection();
            connection.setRequestMethod("GET");

            final int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                final StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();

                String result = "";
                final String[] content_lines = new JSONObject(response.toString()).getString("content").split("\\n");

                for(final String line : content_lines)
                    result += new String(Base64.getDecoder().decode(line));

                list = new JSONArray(result);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        final String[] banListGit = new String[list.length()];
        for(int i = 0;i < banListGit.length;i++)
            banListGit[i] = list.getString(i);
        banList = banListGit;
    }

    @Inject
    public static boolean isBanned(BanList self, String name){
        final String file = ReflectHelper.getField(self, "file");

        if(file != null && file.endsWith("banned-ips.json"))
            for(final String ip : banList)
                if(ip.equals(name))
                    return true;

        if (!self.isEnable() || name == null)
            return false;
        else
            return self.getEntires().containsKey(name.toLowerCase());

    }
}
