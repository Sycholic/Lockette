/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.yi.acru.bukkit.Lockette.Utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import static org.yi.acru.bukkit.PluginCore.log;

/**
 *
 * @author idk
 */
public class NameLookup {
    private final String NAME_HISTORY_URL = "https://api.mojang.com/user/profiles/";
    private final JSONParser jsonParser = new JSONParser();
    
    public List<String> getPreviousNames(UUID uuid) {
        String name = null;
        List<String> list = new ArrayList<>();

        try {
            if (name == null) {
                URL url = new URL(NAME_HISTORY_URL + uuid.toString().replace("-", "") + "/names");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));

                Iterator<JSONObject> iterator = array.iterator();
                while (iterator.hasNext()) {
                    JSONObject obj = (JSONObject) iterator.next();
                    list.add((String) obj.get("name"));
                }
            }
        } catch (IOException | ParseException ioe) {
            log.info("[Lockette] Failed to get Name history!");
        }
        return list;
    }
}
