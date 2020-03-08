/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kareem
 */
public class Config {
    public String ipAddress;
    public int port;
    public int gamePort;
    
    public Config(){
        FileReader reader = null;
        try {
            reader = new FileReader("src/configuration/env.json");
            JsonParser parser = new JsonParser();
            JsonObject jsonResponse = parser.parse(reader).getAsJsonObject();
            
            ipAddress = jsonResponse.get("ipAddress").getAsString();
            port = jsonResponse.get("port").getAsInt();
            gamePort = jsonResponse.get("gamePort").getAsInt();
            System.out.println(port);
        } catch (FileNotFoundException ex) {
            ipAddress = "127.0.0.1";
            port = 7487;
            gamePort = 7488;
        }
    }
}
