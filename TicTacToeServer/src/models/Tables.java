/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

/**
 *
 * @author kareem
 */
public class Tables {
    public static class User
    {
        public final static int ID = 1;
        public final static int EMAIL = 2;
        public final static int USERNAME = 3;
        public final static int PASSWORD = 4;
        public final static int POINTS = 5;
    }
    
    public static class Game
    {
        public final static int ID = 1;
        public final static int FIRST_OPPONENT = 2;
        public final static int SECOND_OPONNENT = 3;
        public final static int WINNER = 4;
    }
}
