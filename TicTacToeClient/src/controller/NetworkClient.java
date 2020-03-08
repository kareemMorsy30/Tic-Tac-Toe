package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tictactoeclient.TicTacToeClient;
import tray.animations.AnimationType;
import tray.notification.TrayNotification;

public class NetworkClient extends Application implements Runnable {

    Socket gameSocket = null;
    Socket clientSocket;
    public Player currentPlayer = null;
    InputStream inputStream;
    DataOutputStream jsonWrite;
    //ObjectInputStream objectInputStream;
    DataInputStream jsonRead;
    public static Vector<Player> players = new Vector<>();
    public static Player Auth;
    Thread clientThread = new Thread(this);

//    Thread playersUpdate = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    sleep(2000);
//                    updatePlayers();
//                    System.out.println("update players: " + players);
//                } catch (Exception ex) {
//                    System.err.println("Cant send update request: " + ex);
//                }
//
//            }
//        }
//    });
    public NetworkClient() {
        try {
            //Send Json Request to server for login (Return Player object if success or null if failed)

            System.out.println("NW Const");
            clientSocket = new Socket(TicTacToeClient.config.ipAddress, TicTacToeClient.config.port);
            inputStream = clientSocket.getInputStream();
            System.out.println("INPUT STREAM: " + inputStream);
            System.out.println("currentPlayer const: " + currentPlayer);
            //loginPlayer(user, pass);
            //System.out.println("Logged in: " + currentPlayer);
            //this.getPlayerList();
        } catch (Exception e) {
            System.err.println("Client Cant Connect To Server: " + e);
        }
    }
//
//    public DataInputStream getPlayerList() throws IOException, ClassNotFoundException {
//        JsonObject getPlayers = new JsonObject();
//        getPlayers.addProperty("type", "getPlayers");
//        jsonWrite = new DataOutputStream(clientSocket.getOutputStream());
//        jsonWrite.writeUTF(getPlayers.toString());
//        return new DataInputStream(inputStream);
//        //players = (Vector<Player>) objectInputStream.readObject();
//    }

    
    
    public void showNotification(String name, boolean isOnline){
        TrayNotification tray = new TrayNotification();
        AnimationType type = AnimationType.POPUP;
        Image whatsAppImg = new Image("resources/online.png");
        tray.setTitle("Sign-In");
        tray.setMessage(name + " Has Signed-In");
        tray.setRectangleFill(Paint.valueOf("#2A9A84"));
        if(!isOnline){
            whatsAppImg = new Image("resources/offline.png");
            tray.setTitle("Sign-Out");
            tray.setMessage(name + " Has Signed-Out");
            tray.setRectangleFill(Paint.valueOf("#ff4751"));
        }
        tray.setAnimationType(type);
        tray.setImage(whatsAppImg);
        tray.showAndDismiss(Duration.seconds(2));
    }
    
    public boolean inviteDialog(String pname) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Invitation");
        alert.setHeaderText("Invitation");
        alert.setContentText(pname + " has invited you to play a game");

        alert.initStyle(StageStyle.UNDECORATED);
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("inviteDialog.css").toExternalForm());
        ///Users/seomac/NetBeansProjects/JavaFXApplication1/src/javafxapplication1/dialog.css
        dialogPane.getStyleClass().add("myDialog");
        ButtonType okButton = new ButtonType("Accept", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Decline", ButtonBar.ButtonData.NO);
        //          ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(okButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.get().getText() == "Accept") {
            return true;

        } else {
            return false;
        }
    }

    public boolean signupPlayer(String name, String password, String mail) {
        try {
            JsonObject signupJson = new JsonObject();
            signupJson.addProperty("type", "signup");
            signupJson.addProperty("username", name);
            signupJson.addProperty("password", password);
            signupJson.addProperty("email", mail);

            jsonWrite = new DataOutputStream(clientSocket.getOutputStream());
            jsonWrite.writeUTF(signupJson.toString());

            jsonRead = new DataInputStream(inputStream);

            JsonParser playerParser = new JsonParser();
            JsonObject loginResponse = playerParser.parse(jsonRead.readUTF()).getAsJsonObject();
            if (loginResponse.get("type").getAsString().equals("loginFailed")) {
                return false;
            } else if (loginResponse.get("type").getAsString().equals("loginSuccess")) {
                System.out.println("client player object signup: " + loginResponse.get("data").getAsString());
                Gson gson = new Gson();
                currentPlayer = gson.fromJson(loginResponse.get("data").getAsString(), Player.class);
                if (currentPlayer.isSigned) {
                    System.out.println("Current Player: " + currentPlayer + ", " + currentPlayer.id);
                    Auth = currentPlayer;
                    clientThread.start();
                    //playersUpdate.start();
                    return true;
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(NetworkClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean loginPlayer(String username, String password) throws IOException {

        JsonObject loginJson = new JsonObject();
        loginJson.addProperty("type", "login");
        loginJson.addProperty("username", username);
        loginJson.addProperty("password", password);
        System.out.println("Login object created");
        jsonWrite = new DataOutputStream(clientSocket.getOutputStream());
        jsonWrite.writeUTF(loginJson.toString() + '\n');
        jsonWrite.flush();
        try {
            jsonRead = new DataInputStream(inputStream);

            JsonParser playerParser = new JsonParser();
            JsonObject loginResponse = playerParser.parse(jsonRead.readUTF()).getAsJsonObject();
            System.out.println("loginResponse: " + loginResponse);
            if (loginResponse.get("type").getAsString().equals("loginFailed")) {
                System.err.println("Login Failed! " + loginResponse);
                return false;
            } else if (loginResponse.get("type").getAsString().equals("loginSuccess")) {
                //currentPlayer = (Player) objectInputStream.readObject();
                System.out.println("client player object: " + loginResponse.get("data").getAsString());
                Gson gson = new Gson();
                currentPlayer = gson.fromJson(loginResponse.get("data").getAsString(), Player.class);
                if (currentPlayer.isSigned) {
                    System.out.println("Current Player: " + currentPlayer + ", " + currentPlayer.id);
                    Auth = currentPlayer;
                    clientThread.start();
                    //playersUpdate.start();
                    return true;
                }
            }

            System.out.println("Login else if skipped: ");

        } catch (Exception ex) {
            System.out.println("Error in lognPlayer: " + ex);
        }
        return false;
    }

//    public void updatePlayers() {
//        if (this.currentPlayer != null && this.currentPlayer.isSigned) {
//
//            System.out.println("Called updatPlayers()");
//            try {
//                JsonObject updateJson = new JsonObject();
//                updateJson.addProperty("type", "getPlayers");
//
//                String jsonRequest = updateJson.toString();
//
//                jsonWrite = new DataOutputStream(clientSocket.getOutputStream());
//
//                jsonWrite.writeUTF(jsonRequest);
//            } catch (Exception ex) {
//                System.out.println("can't send invitation: " + ex);
//
//            }
//
//        }
//
//    }
    public void acceptInvitation(int invitationSender) {

        try {
            JsonObject sendAccept = new JsonObject();
            sendAccept.addProperty("type", "invitationAccepted");
            sendAccept.addProperty("from", this.currentPlayer.id);
            sendAccept.addProperty("to", invitationSender);

            String jsonRequest = sendAccept.toString();
            jsonWrite = new DataOutputStream(clientSocket.getOutputStream());
            jsonWrite.writeUTF(jsonRequest + '\n');
            jsonWrite.flush();
        } catch (IOException ex) {
            System.err.println("accept invitation: " + ex);
        }

    }

    public boolean startInvitation(int id) {
        System.out.println("Called start invitation");
        //send invitation request to server
        //{"type": "invitation", "from": this.currentPlayer.id, "to": id}
        if (true) {
            try {
                //invitation accepted
                // String jsonRequest = "{\"type\": \"invitation\", \"from\": this.currentPlayer.id, \"to\": 2}";
                JsonObject inviteJson = new JsonObject();
                inviteJson.addProperty("type", "invitation");
                inviteJson.addProperty("from", this.currentPlayer.id);
                inviteJson.addProperty("to", id);
                inviteJson.addProperty("fromName", this.currentPlayer.getName());
                String jsonRequest = inviteJson.toString();
                System.out.println("invite json: " + jsonRequest);
                jsonWrite = new DataOutputStream(clientSocket.getOutputStream());
                System.out.println("invite: " + jsonWrite);
                jsonWrite.writeUTF(jsonRequest);

                System.out.println("invite: sent json request");

                return true;
            } catch (Exception ex) {
                System.out.println("can't send invitation: " + ex);
                return false;
            }
        }
        return false;
    }

    @Override
    public void run() {
        while (true) {
            try {
                DataInputStream dis;
                dis = new DataInputStream(clientSocket.getInputStream());
                String responseString = dis.readUTF();
                System.out.println("Client String: " + responseString);
                //handle any kind of json response
                JsonParser parser = new JsonParser();
                JsonObject jsonResponse = parser.parse(responseString).getAsJsonObject();
                System.err.println("RESPONSE STRING:::::::::::::: " + responseString);
                if (jsonResponse.get("type").getAsString().equals("gameStarted")) {
                    
                    System.err.println("Started new game");
//            type: gameStarted
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Gameplay newGame;
                            if(jsonResponse.get("resumeGame").getAsBoolean())
                                newGame = new Gameplay(jsonResponse.get("sign").getAsString(), currentPlayer.getName(), false, jsonResponse.get("resumeData").getAsString());
                            else
                                newGame = new Gameplay(jsonResponse.get("sign").getAsString(), currentPlayer.getName(), false, null);
                            Stage secondaryStage = new Stage();
                            secondaryStage.setTitle(currentPlayer.getName()+", "+currentPlayer.id);
                            
                            newGame.start(secondaryStage);
                        }
                    });
                }
                if (jsonResponse.get("type").getAsString().equals("invitation")) {
                    System.out.println("Invitation Handler");
                    //accept or refuse invitation

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            boolean isAccepted;
                            isAccepted = inviteDialog(jsonResponse.get("fromName").getAsString());
                            if (isAccepted) {
                                acceptInvitation(jsonResponse.get("from").getAsInt());
                            }
                        }
                    });

                }

                if (jsonResponse.get("type").getAsString().equals("playersVector")) {
                    System.out.println("players list handler");

                    TypeToken<Vector<Player>> token = new TypeToken<Vector<Player>>() {
                    };
                    Gson gson = new Gson();
                    players = gson.fromJson(jsonResponse.get("data").getAsString(), token.getType());
                    System.out.println("players: " + players);
                    //players

                }
                
                if (jsonResponse.get("type").getAsString().equals("SignInNotification")) {
                    System.out.println("SignIN notification");
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            showNotification(jsonResponse.get("name").getAsString(), true);
                        }
                    });
                    
                    //players
                }
                
                if (jsonResponse.get("type").getAsString().equals("SignOutNotification")) {
                    System.out.println("SignOut notification");
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            showNotification(jsonResponse.get("name").getAsString(), false);
                        }
                    });
                    
                    //players
                }
                
                
                
                
                

            } catch (IOException ex) {
                System.err.println("Cant receive invitation: " + ex);
            }

        }

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
