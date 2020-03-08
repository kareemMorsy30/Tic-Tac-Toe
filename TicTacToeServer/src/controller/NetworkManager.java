package controller;

import com.google.gson.*;
import java.util.Vector;
import java.io.*;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Config;
import tictactoeserver.ServerFXMLController;
import tictactoeserver.TicTacToeServer;

public class NetworkManager implements Runnable {

    Auth auth = new Auth();
    public PlayerSocket xPlayer = null;
    public PlayerSocket oPlayer = null;
    public static int counter = 0;
    public static Vector<PlayerSocket> playerSocket = new Vector<PlayerSocket>();
    public static ServerSocket ss = null;
    OutputStream output;
    DataOutputStream jsonWrite;
    Thread readJson;
    public static Vector<GameHandler> runningGames = new Vector<GameHandler>();
    Player player;

    public NetworkManager() {
        getPlayersDB();
        System.out.println("Player socket:" + playerSocket);

        //keep track of signed in and signed out players
        Thread updatePlayers = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        sleep(3000); //sleep 3 seconds
                    } catch (InterruptedException ex) {
                        System.out.println("Sleep err >> " + ex);
                    }

                    for (PlayerSocket ps : playerSocket) {
                        if (ps.myPlayer.isSigned) {
                            if (ps.socket != null) {
                                try {
                                    output = ps.socket.getOutputStream();
                                    jsonWrite = new DataOutputStream(output);

                                    Gson gson = new Gson();
                                    String objectString = gson.toJson(PlayerSocket.connectedPlayers);
                                    
                                    if(ps.myPlayer.id == player.id && ps.myPlayer.score != player.score){
                                        ps.myPlayer.score = player.score;
                                    }

                                    JsonObject objectJson = new JsonObject();
                                    objectJson.addProperty("type", "playersVector");
                                    objectJson.addProperty("data", objectString);
                                    jsonWrite.writeUTF(objectJson.toString());

                                    System.out.println("Updated list");
                                } catch (IOException ex) {
                                    System.err.println("Player lost connection: " + ps.myPlayer.id + '\n' + ex);
                                    ps.myPlayer.isSigned = false;
                                    playerSignedOutNotification(ps.myPlayer.name);

                                }
                            } else {
                                System.out.println("Player lost socket connection: " + ps.myPlayer.id);
                                ps.myPlayer.isSigned = false;
                                playerSignedOutNotification(ps.myPlayer.name);
                            }
                        }
                    }

                }

            }

            private void playerSignedOutNotification(String name) {
                for (PlayerSocket p : playerSocket) {
                    if (p.myPlayer.isSigned) {
                        try {
                            //send notification to online players except the one who signed in
                            JsonObject signIn = new JsonObject();
                            signIn.addProperty("type", "SignOutNotification");
                            signIn.addProperty("name", name);
                            jsonWrite = new DataOutputStream(p.socket.getOutputStream());
                            jsonWrite.writeUTF(signIn.toString());
                        } catch (IOException ex) {
                            System.out.println("Can't send signout notification: " + ex);
                        }
                    }
                }
            }
        });
        updatePlayers.start();
        try {
            this.ss = new ServerSocket(TicTacToeServer.config.serverPort);
        } catch (IOException ex) {
            System.err.println("Listen Error: " + ex);
        }
    }

    public void getPlayersDB() {
        Vector<Player> playerVector = auth.getPlayers();
        for (Player p : playerVector) {
            playerSocket.add(new PlayerSocket(p, null));
            System.out.println("Added: " + p.id + ", " + p.name);
        }
    }

    public void sendPlayerList() {
//        for (PlayerSocket player : playerSocket) {
//            try {
//                output = player.socket.getOutputStream();
//                
//                //objectOutput = new ObjectOutputStream(output);
//                //objectOutput.writeObject((Vector<Player>) PlayerSocket.connectedPlayers);
//                
//                
//            } catch (IOException ex) {
//                System.err.println("Socket Closed! " + ex);
//                player.remove();
//            }
//        }

    }

    public void createClientHandlerThread(Socket socket) {

        readJson = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isConnected = true; //player is connected
                while (isConnected) {
                    try {
                        DataInputStream jsonRead = new DataInputStream(socket.getInputStream());
                        System.out.println("Reading : " + jsonRead);
                        String request = jsonRead.readUTF();
                        System.out.println("REQUEST: " + request);

                        JsonParser parser = new JsonParser();
                        JsonObject jsonResponse = parser.parse(request).getAsJsonObject();

//                                String email = user.get("email").toString();
//                    if(auth.register(email, password, username)){
//                        response = "true";
                        if (jsonResponse.get("type").getAsString().equals("signup")) {
                            String user = jsonResponse.get("username").getAsString();
                            String pass = jsonResponse.get("password").getAsString();
                            String email = jsonResponse.get("email").getAsString();
                            PlayerSocket myPlayerSocket = null;
                            player = auth.register(email, pass, user);
                            if (player.isSigned) {
                                myPlayerSocket = new PlayerSocket(++counter);
                                myPlayerSocket.socket = socket;
                                playerSocket.add(myPlayerSocket);
                                myPlayerSocket.addPlayerData(player);
//                                myPlayerSocket.myPlayer.isSigned = true;
//                                myPlayerSocket.myPlayer.setName(user);
//                                myPlayerSocket.myPlayer.password = pass;
//                                myPlayerSocket.myPlayer.email = email;
                            }
                            //send created player object to client
                            try {
                                output = socket.getOutputStream();
                                jsonWrite = new DataOutputStream(output);
                                JsonObject sendJson = new JsonObject();
                                if (player.isSigned) {
                                    sendJson.addProperty("type", "loginSuccess");
                                    Gson gson = new Gson();
                                    String playerJson = gson.toJson(myPlayerSocket.myPlayer);
                                    sendJson.addProperty("data", playerJson);
                                } else {
                                    sendJson.addProperty("type", "loginFailed");
                                }
                                jsonWrite.writeUTF(sendJson.toString());
                                //objectOutput = new ObjectOutputStream(output);
                                //objectOutput.writeObject((Player) myPlayerSocket.myPlayer);
                            } catch (IOException ex) {
                                System.err.println("Cant send current player: " + ex);
                            }
                        }

                        if (jsonResponse.get("type").getAsString().equals("login")) {
                            //check if user exists in db and create player object
                            Auth auth = new Auth();
                            String user = jsonResponse.get("username").getAsString();
                            String pass = jsonResponse.get("password").getAsString();
                            PlayerSocket myPlayerSocket = null;
                            player = auth.SignIn(user, pass);

                            if (player.isSigned) {
                                for (PlayerSocket ps : playerSocket) {
                                    if (ps.myPlayer.id == player.id) {
                                        ps.socket = socket;
                                        ps.myPlayer.isSigned = true;
                                        myPlayerSocket = ps;
                                    }
                                }
//                                myPlayerSocket = new PlayerSocket(++counter);
//                                myPlayerSocket.socket = socket;
//                                playerSocket.add(myPlayerSocket);
//                                myPlayerSocket.addPlayerData(player);
                            }

                            //send created player object to client
                            try {
                                output = socket.getOutputStream();
                                jsonWrite = new DataOutputStream(output);
                                JsonObject sendJson = new JsonObject();
                                if (player != null) {
                                    if (player.isSigned) {
                                        sendJson.addProperty("type", "loginSuccess");
                                        Gson gson = new Gson();
                                        String playerJson = gson.toJson(myPlayerSocket.myPlayer);
                                        sendJson.addProperty("data", playerJson);
                                        for (PlayerSocket p : playerSocket) {
                                            if (p.myPlayer.id != player.id && p.myPlayer.isSigned) { //send notification to online players except the one who signed in
                                                JsonObject signIn = new JsonObject();
                                                signIn.addProperty("type", "SignInNotification");
                                                signIn.addProperty("name", player.name);
                                                jsonWrite = new DataOutputStream(p.socket.getOutputStream());
                                                jsonWrite.writeUTF(signIn.toString());
                                            }
                                        }
                                    } else {
                                        sendJson.addProperty("type", "loginFailed");
                                    }
                                } else {
                                    sendJson.addProperty("type", "loginFailed");
                                }
                                jsonWrite = new DataOutputStream(output);
                                jsonWrite.writeUTF(sendJson.toString());
                                //objectOutput = new ObjectOutputStream(output);
                                //objectOutput.writeObject((Player) myPlayerSocket.myPlayer);
                            } catch (IOException ex) {
                                System.err.println("Cant send current player: " + ex);
                            }
                        }

                        if (jsonResponse.get("type").getAsString().equals("invitation")) {
                            System.out.println("Invitation to client: ");
                            for (PlayerSocket player : playerSocket) {
                                if (player.myPlayer.id == jsonResponse.get("to").getAsInt()) {
                                    try {
                                        output = player.socket.getOutputStream();
                                        DataOutputStream dos = new DataOutputStream(output);
                                        dos.writeUTF(request);

                                    } catch (IOException ex) {
                                        System.err.println("Socket Closed! " + ex);
                                        player.remove();
                                    }
                                }
                            }
                        }

                        if (jsonResponse.get("type").getAsString().equals("invitationAccepted")) {
                            xPlayer = null;
                            oPlayer = null;

                            int to_id = jsonResponse.get("to").getAsInt();
                            int from_id = jsonResponse.get("from").getAsInt();
                            String savedData = Auth.getSavedGame(to_id, from_id);

                            if (savedData != null) {
                                //There's saved game between two players continue it
                                JsonParser parse = new JsonParser();
                                JsonObject savedGame = parse.parse(savedData).getAsJsonObject();
                                int x_id = savedGame.get("xplayer").getAsInt();
                                int o_id = savedGame.get("oplayer").getAsInt();
                                
                                for (PlayerSocket player : playerSocket) {
                                    if (xPlayer != null && oPlayer != null) {
                                        break;
                                    }
                                    if (player.myPlayer.id == x_id) {
                                        xPlayer = player;
                                    } else if (player.myPlayer.id == o_id) {
                                        oPlayer = player;
                                    }

                                }

                            } else {
                                System.out.println("STARTING NEW GAME: ");
                                for (PlayerSocket player : playerSocket) {
                                    if (xPlayer != null && oPlayer != null) {
                                        break;
                                    }
                                    if (player.myPlayer.id == jsonResponse.get("to").getAsInt()) {
                                        xPlayer = player;
                                    } else if (player.myPlayer.id == jsonResponse.get("from").getAsInt()) {
                                        oPlayer = player;
                                    }

                                }
                            }

                            System.err.println("players from for: " + xPlayer.myPlayer.name + ", " + oPlayer.myPlayer.name);
                            if (xPlayer != null && oPlayer != null) {

                                System.err.println("EXECUTION222222 >>>>>>>>>>>>>>>>");

                                Thread gameListener = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            runningGames.add(new GameHandler(xPlayer.myPlayer, oPlayer.myPlayer));
                                        } catch (Exception ex) {
                                            System.err.println("Can't create game handler " + ex);
                                        }
                                    }
                                });
                                gameListener.start();
                                System.err.println("EXECUTION >>>>>>>>>>>>>>>>");
                                try {
                                    DataOutputStream xdos = new DataOutputStream(xPlayer.socket.getOutputStream());
                                    DataOutputStream odos = new DataOutputStream(oPlayer.socket.getOutputStream());
                                    JsonObject gameJson = new JsonObject();
                                    gameJson.addProperty("type", "gameStarted");
                                    if(savedData != null){
                                        gameJson.addProperty("resumeGame", true);
                                        gameJson.addProperty("resumeData", savedData);
                                    }else{
                                        gameJson.addProperty("resumeGame", false);
                                    }
                                    
                                    gameJson.addProperty("sign", "X");

                                    xdos.writeUTF(gameJson.toString() + '\n');
                                    xdos.flush();
                                    gameJson.remove("sign");

                                    gameJson.addProperty("sign", "O");
                                    odos.writeUTF(gameJson.toString() + '\n');
                                    odos.flush();
                                    System.err.println("gameJson string: " + gameJson.toString());
                                } catch (IOException ex) {
                                    System.err.println("Cant start game! " + ex);
                                }
                            } else {
                                System.err.println("Couldn't find players");
                            }
                        }

                        if (jsonResponse.get("type").getAsString().equals("getPlayers")) {
                            System.out.println("Get Players Request: ");
                            try {
                                output = socket.getOutputStream();
                                jsonWrite = new DataOutputStream(output);

                                Gson gson = new Gson();
                                String objectString = gson.toJson(PlayerSocket.connectedPlayers);

                                JsonObject objectJson = new JsonObject();
                                objectJson.addProperty("type", "playersVector");
                                objectJson.addProperty("data", objectString);
                                jsonWrite.writeUTF(objectJson.toString());

                                System.out.println("Updated list");
                            } catch (Exception ex) {
                                System.err.println("Cant Update player list" + ex);
                            }
                        }

                        if (jsonResponse.get("type").getAsString().equals("score")) {
                            if (player.isSigned) {
                                int score = jsonResponse.get("points").getAsInt();
                                player.updateScore(score);
                            }
                        }

                    } catch (IOException ex) {
                        System.out.println("Can't Read json: " + ex);
                        isConnected = false;
                    }
                }
            }
        });
        readJson.start();

    }

    public void stopClientHandlerThread() {
        if(readJson!=null){
                readJson.stop();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Server RUN: ");
                Socket socket = ss.accept();
                this.createClientHandlerThread(socket); //Create Thread for each client connects to read json
            } catch (Exception ex) {
                System.err.println("Error: " + ex);
            }

        }

    }

}
