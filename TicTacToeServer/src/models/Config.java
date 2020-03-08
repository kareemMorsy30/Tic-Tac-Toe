package models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kareem
 */
public class Config {
    /* Create connection url. */
    private String mysqlConnUrl;

    /* db user name. */
    private String mysqlUserName;

    /* db password. */
    private String mysqlPassword;
    private JsonElement ports;
    public int gamePort;
    public int serverPort;
    
    public Config(){
        try{
            FileReader reader = new FileReader("src/configuration/env.json");
            System.out.println(reader);
            JsonParser parser = new JsonParser();
            JsonObject jsonResponse = parser.parse(reader).getAsJsonObject();
            mysqlConnUrl = "jdbc:mysql://localhost:"+jsonResponse.get("DBport").getAsString()+"/tictactoe?serverTimezone=UTC&amp;useSSL=false";
            mysqlUserName = jsonResponse.get("UserName").getAsString();
            mysqlPassword = jsonResponse.get("Password").getAsString();
            ports = jsonResponse.get("ports");
            gamePort = ports.getAsJsonObject().get("gamePort").getAsInt();
            serverPort = ports.getAsJsonObject().get("serverPort").getAsInt();
        } catch (FileNotFoundException ex) {
            mysqlConnUrl = "jdbc:mysql://localhost:3306/tictactoe?serverTimezone=UTC&amp;useSSL=false";
            mysqlUserName = "root";
            mysqlPassword = "";
            gamePort = 7488;
            serverPort = 7487;
        }
        
    }

    public void setMysqlConnUrl(String mysqlConnUrl) {
        this.mysqlConnUrl = mysqlConnUrl;
    }
    
    public void setMysqlUserName(String mysqlUserName) {
        this.mysqlUserName = mysqlUserName;
    }

    public void setMysqlPassword(String mysqlPassword) {
        this.mysqlPassword = mysqlPassword;
    }

    public String getMysqlConnUrl() {
        return mysqlConnUrl;
    }

    public String getMysqlUserName() {
        return mysqlUserName;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }
}
