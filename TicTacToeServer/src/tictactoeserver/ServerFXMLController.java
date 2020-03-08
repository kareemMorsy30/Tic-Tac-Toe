/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tictactoeserver;

import controller.NetworkManager;
import controller.Player;
import controller.PlayerSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import models.Config;

/**
 * FXML Controller class
 *
 * @author Mostafa El-kenany
 */
public class ServerFXMLController implements Initializable {

    @FXML
    private Button startServerBtn = new Button();
    @FXML
    private Button endServerBtn = new Button();
    @FXML
    private TableView<Player> playerList;
    @FXML
    private TableColumn<Player, String> name;
    private ObservableList<Player> players;

    NetworkManager myNetwork;
    Thread networkThread;
    @FXML
    private TableColumn<Player, Integer> score;
    @FXML
    private TableColumn<Player, Boolean> status;

    public ServerFXMLController() {
        this.name = new TableColumn<>("name");
        name.setCellValueFactory(new PropertyValueFactory("name"));
        this.score = new TableColumn<>("Score");
        score.setCellValueFactory(new PropertyValueFactory("score"));
        this.status = new TableColumn<>("Status");
        status.setCellValueFactory(new PropertyValueFactory("isSigned"));
        playerList = new TableView<>();
        playerList.getColumns().addAll(name, status, score);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        myNetwork = new NetworkManager();
        MyTask task = new MyTask();
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(task, 0, 3000);
        // TODO
    }

    class MyTask extends TimerTask {

        @Override
        public void run() {
            Platform.runLater(() -> {
                playerList.refresh();
                name.setCellValueFactory(new PropertyValueFactory("name"));
                score.setCellValueFactory(new PropertyValueFactory("score"));
                status.setCellValueFactory(new PropertyValueFactory("isSigned"));
                playerList.setItems(getPlayers());
            });
        }
    }

    @FXML
    private void startServer(ActionEvent event) {

        endServerBtn.setDisable(false);
        startServerBtn.setDisable(true);

        try {
            if (NetworkManager.ss == null) {
                NetworkManager.ss = new ServerSocket(TicTacToeServer.config.serverPort);
            }
        } catch (IOException ex) {
            System.err.println("Listen Error: " + ex);
        }
        networkThread = new Thread(myNetwork);
        networkThread.start();
    }

    @FXML
    private void stopServer(ActionEvent event) {

        endServerBtn.setDisable(true);
        startServerBtn.setDisable(false);

        System.err.println("Server Stopped");
        try {
            if (NetworkManager.ss != null) {
                NetworkManager.ss.close();
                networkThread.stop();
                myNetwork.stopClientHandlerThread();
                NetworkManager.ss = null;
            }

        } catch (IOException ex) {
            Logger.getLogger(ServerFXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public ObservableList<Player> getPlayers() {
        players = FXCollections.observableArrayList();
        players.setAll(PlayerSocket.connectedPlayers);
        return players;
    }

}
