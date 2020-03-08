/***
 * Handle Game Thread for each 2 players
 */
package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Config;
import tictactoeserver.TicTacToeServer;
public class GameHandler{
    Player xPlayer = null;
    Player oPlayer = null;
    Socket xSocket = null;
    Socket oSocket = null;
    Config config;
    static ServerSocket serverSocket = null;
    public GameHandler(Player x, Player o){
        this.xPlayer = x;
        this.oPlayer = o;
        try {
            if(serverSocket == null){
                serverSocket = new ServerSocket(TicTacToeServer.config.gamePort); //new listen port for running games to avoid conflicts
            }
            while(xSocket == null || oSocket == null){
                if(xSocket == null){
                    xSocket = serverSocket.accept();
                }else{
                    oSocket = serverSocket.accept();
                }
            }
            
            
//            String savedData = Auth.getSavedGame(1,1);
            
            
            
            
            //start thread after both players join
            Thread xThread = new Thread(new Runnable(){
                @Override
                public void run() {
                    handleXPlayer();
                }
            });
            Thread oThread = new Thread(new Runnable(){
                @Override
                public void run() {
                    handleOPlayer();
                }
            });
            xThread.start();
            oThread.start();
            
            System.err.println("XO THREAD STARTED");
        } catch (IOException ex) {
            System.err.println("GameHandler ERR: "+ ex);
        }
    }
//    public void setBoard(){
//        
//    }
//    public void saveGame(){
//        
//    }
    
    private void playerHandle(DataOutputStream jsonWrite, DataInputStream jsonRead) throws IOException{
                
                String response = jsonRead.readUTF();
                JsonParser parser = new JsonParser();
                JsonObject jObject = parser.parse(response).getAsJsonObject();
                if (jObject.get("type").getAsString().equals("gameBoard")) {
         
                    jsonWrite.writeUTF(response + '\n');
                    jsonWrite.flush();
                }
                if (jObject.get("type").getAsString().equals("savedGame")) {
                    
                    //{"type": "savedGame", "xplayer": 7, "oplayer": 5, "cells":["","O","X","","X","X","O","","O"], "isTurnx": false}
                    jObject.addProperty("xplayer", this.xPlayer.id);
                    jObject.addProperty("oplayer", this.oPlayer.id);
                    
                    if(Auth.saveGame(jObject.toString(), this.xPlayer.id, this.oPlayer.id)){
                        System.out.println("game saved");
                    }
//                    
//                    jsonWrite.writeUTF(response + '\n');
//                    jsonWrite.flush();
                }
                
                if (jObject.get("type").getAsString().equals("chat") || jObject.get("type").getAsString().equals("exit")) {
                    jsonWrite.writeUTF(response + '\n');
                    jsonWrite.flush();
                }
    }
    
    private void handleXPlayer(){ //read from x and send to o
        
        boolean isPlayerConnected = true;
        while(isPlayerConnected){
            try{
                DataOutputStream jsonWrite = new DataOutputStream(oSocket.getOutputStream());
                DataInputStream jsonRead = new DataInputStream(xSocket.getInputStream());
                playerHandle(jsonWrite, jsonRead);
            }
            catch (IOException ex) {
                System.err.println("Player Exited Game: "+ ex);
                isPlayerConnected = false;
            }
        }
    }
    private void handleOPlayer(){ //read from o and send to x
        boolean isPlayerConnected = true;
        while(isPlayerConnected){
            try {
                DataOutputStream jsonWrite = new DataOutputStream(xSocket.getOutputStream());
                DataInputStream jsonRead = new DataInputStream(oSocket.getInputStream());
                playerHandle(jsonWrite, jsonRead);
            } catch (IOException ex) {
                System.err.println("Player O exited game: "+ ex);
                isPlayerConnected = false;
            }
        }
    }
}
