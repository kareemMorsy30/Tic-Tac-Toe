/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import tictactoeclient.TicTacToeClient;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;

/**
 *
 * @author Moaz_G
 */
public class Gameplay extends Application {
    private Text signLabel = new Text();
    private TextArea chatBox = new TextArea();
    private Button chatButton = new Button("send");
    private Button saveBtn = new Button("Save & Exit");
    private String playerName = "";
    private TextField textField = new TextField();
    public JsonObject gameBoard = new JsonObject();
    public Tiles board[][] = new Tiles[3][3];
    public List<WiningCombo> combos = new ArrayList<>();
    Rectangle tile;
    public Boolean turnX = true;
    public boolean playable = true;
    public boolean winnerFlag = false;
    public boolean playinWithAI = false;
    public Text comment = new Text("Player X turn");
    public Button back = new Button("Exit");
    Socket gameSocket = null;
    DataInputStream jsonRead;
    DataOutputStream jsonWrite = null;
    public String sign;
    private NetworkClient client;
    public boolean isGameRunning = true;
    private Stage mainStage = null;
    public Gameplay(String sign, String pname, boolean isAI, String savedData) {
        this.playerName = pname;
        this.sign = sign;
        System.err.println("MySIGN::::: " + this.sign);
        this.playinWithAI = isAI;
        System.out.println("isAI: " + isAI + "  playinWithAI: " + playinWithAI);
        if (!playinWithAI) {
            try {
                gameSocket = new Socket(TicTacToeClient.config.ipAddress, TicTacToeClient.config.gamePort); //connected to game server listener
                System.err.println("gameSocket: " + gameSocket);
                Thread gameThread = new Thread(new Runnable() { //thread to receive json from GameHandler
                    @Override
                    public void run() {
                        while (isGameRunning) {
                            try {
                                jsonRead = new DataInputStream(gameSocket.getInputStream());
                                JsonParser gameParser = new JsonParser();
                                JsonObject gameResponse = gameParser.parse(jsonRead.readUTF()).getAsJsonObject();
                                System.out.println("game Board: " + gameResponse);
                                if (gameResponse.get("type").getAsString().equals("gameBoard")) {
                                    JsonArray cells = gameResponse.get("cells").getAsJsonArray();
                                    gameBoard.add("cells", cells);
                                    setBoard(cells);
                                }

                                if (gameResponse.get("type").getAsString().equals("chat")) {
                                    chatBox.setText(chatBox.getText() + gameResponse.get("msg").getAsString() + "\n");
                                }
                                if (gameResponse.get("type").getAsString().equals("exit")) {
                                    isGameRunning = false;

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mainStage != null) {
                                                mainStage.close();
                                            }
                                        }

                                    });
                                }

                            } catch (IOException ex) {
                                System.err.println("gameThread: " + ex);
                            }
                        }
                    }
                });
                gameThread.start();
            } catch (IOException ex) {
                System.err.println("gameplay: " + ex);
            }

            if (savedData != null) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        resumeSavedGame(savedData);
                    }
                });

            }

        }
    }

    private void sendChat(String message) {
        String msg = playerName + ": " + message;
        JsonObject jsonMsg = new JsonObject();
        jsonMsg.addProperty("type", "chat");
        jsonMsg.addProperty("msg", msg);
        //send
        System.out.println("GAME SOCKET: " + gameSocket);
        try {
            jsonWrite = new DataOutputStream(gameSocket.getOutputStream());
            jsonWrite.writeUTF(jsonMsg.toString() + '\n');
            jsonWrite.flush();
        } catch (IOException ex) {
            System.err.println("Error in chat: " + ex);
        }
        //++
        chatBox.setText(chatBox.getText() + msg + "\n");
        textField.setText("");
    }

    public class Tiles extends StackPane {

        Text text = new Text();

        public Tiles() {
            tile = new Rectangle(170, 170);
            tile.setFill(null);
            tile.setStroke(Color.RED);
            tile.setStrokeWidth(2);
            this.setAlignment(Pos.CENTER);
            text.setFont(Font.font(60));
            //text.setFill(Color.BROWN);
            text.setFill(Color.BLACK);
            this.getChildren().addAll(tile, text);

            setOnMouseClicked((MouseEvent event) -> {

                if (playable) {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        if (turnX && this.getValue().isEmpty()) {
                            System.err.println("MySIGN::::: MOUSER::::: " + sign + ", " + turnX);
                            if (sign.equals("X")) {
                                drawX();
                                checkState();
                                turnX = false;
                                getBoard(); //to test onlyy
                            }
                        } else if (!turnX && !playinWithAI && this.getValue().isEmpty()) {
                            System.err.println("MySIGN::::: MOUSER::::: turnx " + sign + ", " + turnX);
                            if (sign.equals("O")) {
                                drawO();
                                checkState();
                                turnX = true;
                                getBoard(); //to test only
                            }
                        }
                    }

                }
            });
        }

        public String getValue() {
            return text.getText();
        }

        public void drawX() {
            text.setText("X");
            comment.setText("Player O turn");
//            checkState();
        }

        public void drawO() {
            text.setText("O");
            comment.setText("Player X turn");
//            checkState();
        }
    }

    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        gameBoard.addProperty("type", "gameBoard");
        Pane root = new Pane();
        root.setId("main");
        comment.setId("txt");
        comment.setFill(Color.CRIMSON);
        comment.setVisible(true);
        comment.setTextAlignment(TextAlignment.CENTER);
        chatButton.setId("chatBtn");
        saveBtn.setId("saveBtn");
        chatBox.setId("chatBox");
        textField.setId("chatTextField");
        signLabel.setId("signLabel");
        chatBox.setEditable(false);

        if (playinWithAI) {
            chatButton.setVisible(false);
            chatBox.setVisible(false);
            textField.setVisible(false);
            saveBtn.setVisible(false);
        }

        chatButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sendChat(textField.getText());
            }
        });

        textField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sendChat(textField.getText());
            }
        });

        back.setId("backBtn");
        back.setVisible(true);
        back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sendClose();
                primaryStage.close();
//                clearBoard();
//                turnX=true;
//                playable=true;
//                winnerFlag=false;
//                comment.setText("Player X turn");
//                playAgain.setVisible(false);
            }
        });
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                sendClose();
                System.out.println("game closed!");
                isGameRunning = false;
            }
        });

        saveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveGameRequest();
                System.out.println("SAVE GAME REQUESTED");
                sendClose();
                primaryStage.close();
            }
        });
        
        signLabel.setText("Your Sign Is: " + sign);

        root.getChildren().addAll(comment, back, saveBtn, chatBox, chatButton, textField, signLabel);

//        root.setPrefSize(600, 600);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Tiles tile = new Tiles();
                tile.setTranslateX(j * 170);
                tile.setTranslateY(i * 170);
                root.getChildren().add(tile);

                board[j][i] = tile;
            }
        }

        //horizontal combos
        for (int y = 0; y < 3; y++) {
            combos.add(new WiningCombo(board[0][y], board[1][y], board[2][y]));
        }

        //verticalcombos
        for (int x = 0; x < 3; x++) {
            combos.add(new WiningCombo(board[x][0], board[x][1], board[x][2]));
        }

        //diagonal combos
        combos.add(new WiningCombo(board[0][0], board[1][1], board[2][2]));
        combos.add(new WiningCombo(board[2][0], board[1][1], board[0][2]));
//        BorderPane pane = new BorderPane();
//        pane.setLeft(root);
        Scene scene = new Scene(root, 1000, 670);
        scene.getStylesheets().addAll(this.getClass().getResource("/resources/style.css").toExternalForm());
        //primaryStage.setTitle("TicTacToe");
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public void printWinner(WiningCombo combo) {
        comment.setText("The winner is \nPlayer " + combo.tiles[0].getValue());
//        btn.setDisable(false);

    }

    public boolean isFullBoard() {
        int counter = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (!board[j][i].text.getText().isEmpty()) {
                    counter++;
                }
            }
        }
        if (counter == 9) {
            return true;
        }
        return false;
    }

    public void checkState() {
        for (WiningCombo combo : combos) {
            if (combo.isComplete()) {
                printWinner(combo);
                winnerFlag = true;
                playable = false;
                if (sign.equals("X") && !playinWithAI) {
                    JsonObject score = new JsonObject();
                    score.addProperty("type", "score");
                    score.addProperty("points", 5);

                    client = new NetworkClient();

                    try {
                        jsonWrite = new DataOutputStream(client.clientSocket.getOutputStream());
                        jsonWrite.writeUTF(score.toString());
                        break;
                    } catch (IOException ex) {
                        Logger.getLogger(Gameplay.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if (isFullBoard() && !winnerFlag) {
                comment.setText("Tie Game");
            }
        }

    }

    public void getBoard() {
        JsonArray cells = new JsonArray();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cells.add(board[j][i].text.getText());
            }
        }
        gameBoard.add("cells", cells);
        System.out.println(gameBoard.toString());
        //return gameBoard;
        if (!playinWithAI) {
            sendBoard(gameBoard);
        }
    }

    public void sendBoard(JsonObject myGameBoard) {
        try {
            String jsonRequest = myGameBoard.toString();
            jsonWrite = new DataOutputStream(gameSocket.getOutputStream());
            jsonWrite.writeUTF(jsonRequest + '\n');
            jsonWrite.flush();

        } catch (IOException ex) {
            System.err.println("sendBoard: " + ex);
        }
    }

    public void clearBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[j][i].text.setText("");
            }
        }
    }

    public void saveGameRequest() {
//        gameBoard
        JsonObject saveData = new JsonObject();
        saveData.addProperty("type", "savedGame");
        saveData.addProperty("isTurnx", turnX);
        try {
            saveData.add("cells", gameBoard.get("cells").getAsJsonArray());
        } catch (Exception ex) {
            //game already saved or no one played
            System.out.println("Game can't be saved at this state");
            return;

        }
        try {
            String jsonRequest = saveData.toString();
            jsonWrite = new DataOutputStream(gameSocket.getOutputStream());
            jsonWrite.writeUTF(jsonRequest + '\n');
            jsonWrite.flush();
        } catch (IOException ex) {
            System.err.println("Save Game Request: " + ex);
        }
    }

    public void resumeSavedGame(String data) {

        JsonParser parse = new JsonParser();

        JsonObject savedGame = parse.parse(data).getAsJsonObject();
        turnX = savedGame.get("isTurnx").getAsBoolean();
        if (!turnX) {
            comment.setText("Player O turn");
        }
        //set cells
        JsonArray cells = savedGame.get("cells").getAsJsonArray();
        int count = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[j][i].text.setText(cells.get(count++).getAsString());
                System.out.print(board[j][i].text.getText() + "\t");
            }
        }

    }

    public void sendClose() {
        if (!playinWithAI) {
            JsonObject jsonMsg = new JsonObject();
            jsonMsg.addProperty("type", "exit");
            try {
                jsonWrite = new DataOutputStream(gameSocket.getOutputStream());
                jsonWrite.writeUTF(jsonMsg.toString() + '\n');
                jsonWrite.flush();
            } catch (IOException ex) {
                System.err.println("Error in close: " + ex);
            }
        }

    }

    public void setBoard(JsonArray cells) {
        int count = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[j][i].text.setText(cells.get(count++).getAsString());
                System.out.print(board[j][i].text.getText() + "\t");
            }
        }
        if (turnX) {
            turnX = false;
            comment.setText("Player O turn");
        } else {
            turnX = true;
            comment.setText("Player X turn");
        }
        checkState();
    }
    /**
     * @param args the command line arguments
     */
//    public static void main(String[] args) {
//        launch(args);
//    }
}
