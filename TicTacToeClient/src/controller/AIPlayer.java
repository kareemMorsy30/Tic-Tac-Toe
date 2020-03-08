/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.util.Random;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 *
 * @author Mostafa El-kenany
 */
public class AIPlayer {

    Random rand = new Random();
    public Gameplay game = new Gameplay("X", "", true, null); //Fix hereeee

    public void makeMove() {
        System.out.print("");
        if (game.playable && !game.turnX) {
            Gameplay.Tiles aiTile = game.board[rand.nextInt(3)][rand.nextInt(3)];
            if (game.playable && aiTile.getValue().isEmpty()) {
                aiTile.drawO();
//                game.checkState();
              
                game.turnX = true;
            }
        }

    }

}