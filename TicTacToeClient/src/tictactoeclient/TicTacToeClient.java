
package tictactoeclient;

import controller.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class TicTacToeClient extends Application {
    public static Config config;
    
    @Override
    public void start(Stage stage) throws Exception {
        config = new Config();
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /***get player values from database***/
        int id = 1;
        boolean isSigned = true;
        String name = "ahmed";
        int score = 132;
        /****/
        
//        if(args.length == 2){
//            id = Integer.parseInt(args[0]);
//            name = args[1];
//            System.out.println("Created Client: "+ id + ", " + name);
//        }
        
        
        
        
        System.out.println("Main Func");
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        super.stop(); //To change body of generated methods, choose Tools | Templates.
        System.exit(0);
    }
}
