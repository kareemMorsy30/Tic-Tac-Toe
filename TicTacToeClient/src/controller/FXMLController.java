/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import com.google.gson.JsonObject;
import controller.AIPlayer;
import controller.Gameplay;
import controller.NetworkClient;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

public class FXMLController implements Initializable {

    @FXML
    private TextField userName;
    @FXML
    private PasswordField password;
    @FXML
    private Label signInError;
    @FXML
    private TextField email;
    @FXML
    private TextField signUpUserName;
    @FXML
    private PasswordField signUpPass;
    @FXML
    private PasswordField confirmPass;
    @FXML
    private Label confirmMsg;
    @FXML
    private TableView<Player> playerList;
    @FXML
    private TableColumn<Player, String> name;
    private ObservableList<Player> players;
    @FXML
    private TableColumn<Player, Integer> score;
    @FXML
    private TableColumn<Player, Boolean> status;
    @FXML
    private TableColumn<Player, String> level;
    @FXML
    private Text userNameText;
    @FXML
    private Text scoreText;
    @FXML
    private Text levelText;
    public static NetworkClient client = new NetworkClient();
    public int selectedIndex;

    public FXMLController() {
        this.name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory("name"));
        this.score = new TableColumn<>("Score");
        score.setCellValueFactory(new PropertyValueFactory("score"));
        this.status = new TableColumn<>("Status");
        status.setCellValueFactory(new PropertyValueFactory("isSigned"));
        this.level = new TableColumn<>("Level");
        level.setCellValueFactory(new PropertyValueFactory("level"));
        playerList = new TableView<>();
        playerList.getColumns().addAll(name, status, score, level);
        userNameText = new Text();
        scoreText = new Text();
        levelText = new Text();

    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("client initialize: " + client);
        if (this.client == null) {
            this.client = new NetworkClient();
        }
        MyTask task = new MyTask();
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(task, 0, 3000);
        System.out.println("client created: " + client);
    }

    class MyTask extends TimerTask {

        @Override
        public void run() {
            Platform.runLater(() -> {
                if (client.currentPlayer != null) {
                    userNameText.setText(client.currentPlayer.getName());
                    for (Player player : NetworkClient.players) {
                        if (player.id == client.currentPlayer.id) {
                            scoreText.setText(String.valueOf(player.getScore()));
                            levelText.setText(player.getLevel());
                        }
                    }
                }

                name.setCellValueFactory(new PropertyValueFactory("name"));
                score.setCellValueFactory(new PropertyValueFactory("score"));
                status.setCellValueFactory(new PropertyValueFactory("isSigned"));
                level.setCellValueFactory(new PropertyValueFactory("level"));
                selectedIndex = playerList.getSelectionModel().getSelectedIndex();
                playerList.setItems(getPlayers());
                playerList.getSelectionModel().clearAndSelect(selectedIndex);
            });
        }
    }

    @FXML
    public void loginBtn(ActionEvent event) throws IOException {

        System.out.println("Login Clicked");
        //if login success
        String user = userName.getText();
        String pass = password.getText();

        boolean isLogged = client.loginPlayer(user, pass);

        if (isLogged) {
            authenticated(event);
        } else {
            userName.setText("");
            password.setText("");
            signInError.setVisible(true);
        }
        System.out.println("my Client:" + this.client);
    }

    @FXML
    public void signUpBtn(ActionEvent event) throws IOException {
        String name = signUpUserName.getText();
        String mail = email.getText();
        String Password = signUpPass.getText();
        String ConfirmPass = confirmPass.getText();

        if (signUpUserName.getText().isEmpty() || email.getText().isEmpty() || signUpPass.getText().isEmpty()) {
            signUpUserName.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            email.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            signUpPass.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
        } else if (!Password.equals(ConfirmPass)) {
            confirmMsg.setVisible(true);
        } else {
            boolean isLogged = client.signupPlayer(name, Password, mail);

            if (isLogged) {
                authenticated(event);
            } else {
                userName.setText("");
                signUpPass.setText("");
                signInError.setVisible(true);
            }
        }
    }

    @FXML
    private void inviteToPlay(ActionEvent event) {
        if(playerList.getSelectionModel().getSelectedItem() != null)
            this.client.startInvitation(playerList.getSelectionModel().getSelectedItem().id);
    }

    public void authenticated(ActionEvent event) throws IOException {
        Parent loadFile = FXMLLoader.load(getClass().getResource("FXML.fxml"));
        Scene playView = new Scene(loadFile);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(playView);
        window.show();
    }

    @FXML
    private void playWithAI(ActionEvent event) {
        AIPlayer ai = new AIPlayer();
        Stage secondaryStage = new Stage();
        ai.game.start(secondaryStage);
        ai.game.playinWithAI = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (ai.game.playable) {
                    try {
                        if (!ai.game.turnX) {
                            Thread.sleep(500);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    ai.makeMove();
                    ai.game.checkState();
                }
                ai.game.back.setVisible(true);
            }
        }).start();

    }

    public ObservableList<Player> getPlayers() {
        players = FXCollections.observableArrayList();
        for (Player player : NetworkClient.players) {
            if (!player.getName().equals(client.currentPlayer.getName())) {
                players.add(player);
            }
        }
        return players;
    }
}
