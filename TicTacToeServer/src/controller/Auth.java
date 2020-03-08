/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.DBConnection;
import models.Tables;

/**
 *
 * @author kareem
 */
public class Auth {

    private Player player;

    public Auth() {
        player = new Player(0);
        player.isSigned = false;
    }

    public Player register(String email, String password, String username) {
        if (DBConnection.getConnection() != null) {
            try {
                Connection conn = DBConnection.getConnection();

                if (isEmailExist(email, conn)) {
                    return player;
                }

                PreparedStatement query = conn.prepareStatement("INSERT INTO `users`(email,password,username) VALUES (?,?,?)");

                query.setString(1, email);
                query.setString(2, password);
                query.setString(3, username);

                int rowsUpdated = query.executeUpdate();
                if (rowsUpdated == 0) {
                    return player;
                }

                player = SignIn(email, password);
            } catch (SQLException ex) {
                Logger.getLogger(Auth.class.getName()).log(Level.SEVERE, null, ex);
                return player;
            }
            return player;
        } else {
            return player;
        }
    }

    public boolean isEmailExist(String email, Connection conn) {

        try {
            String selectSQL = "SELECT email FROM users WHERE email = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
            preparedStatement.setString(1, email);
            ResultSet rs = preparedStatement.executeQuery();

            return rs.next();
        } catch (SQLException ex) {
            Logger.getLogger(Auth.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public Player SignIn(String emailOrUser, String password) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement query = conn.prepareStatement("SELECT * FROM users WHERE (username= ? OR email= ?) AND password = ?");

            query.setString(1, emailOrUser);
            query.setString(2, emailOrUser);
            query.setString(3, password);

            ResultSet res = query.executeQuery();
            player = new Player(0);
            if (res.next()) {
                player.setDB_id(res.getInt(Tables.User.ID));
                player.email = res.getString(Tables.User.EMAIL);
                player.name = res.getString(Tables.User.USERNAME);
                player.isSigned = true;
                player.score = res.getInt(Tables.User.POINTS);
            }else{
                player.isSigned = false;
            }

            return player;
        } catch (SQLException ex) {
            Logger.getLogger(Auth.class.getName()).log(Level.SEVERE, null, ex);
            return player;
        }

    }

    //get saved game data if exists
    public static String getSavedGame(int xId, int oId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement query = conn.prepareStatement("SELECT data, id FROM games WHERE (xplayer = ? AND oplayer = ?) OR (xplayer = ? AND oplayer = ?)");

            query.setInt(1, xId);
            query.setInt(2, oId);

            query.setInt(3, oId);
            query.setInt(4, xId);
            ResultSet res = query.executeQuery();
            if (res.next()) {
                String myData = res.getString("data");
                PreparedStatement delete = conn.prepareStatement("DELETE FROM games WHERE id = ?");
                delete.setInt(1, res.getInt("id"));
                delete.executeUpdate();
                return myData;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            System.err.println("Err in getPlayers auth: " + ex);
            return null;
        }
    }

    
    public static boolean saveGame(String data, int xId, int oId) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement query = conn.prepareStatement("INSERT INTO games (xplayer, oplayer, data) VALUES (?, ?, ?)");

            query.setInt(1, xId);
            query.setInt(2, oId);
            query.setString(3, data);
            
            int res = query.executeUpdate();
            if (res == 0) {
                System.err.println("Insertion Failed: " + res);
                return false;
            }
            return true;
        } catch (SQLException ex) {
            System.err.println("Can't Save Game in db: " + ex);
            return false;
        }
    }
    
    
    
    public Vector<Player> getPlayers() {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement query = conn.prepareStatement("SELECT * FROM users");

            ResultSet res = query.executeQuery();

            Vector<Player> playerVector = new Vector<Player>();

            while (res.next()) {
                Player temp = new Player(0);
                temp.setDB_id(res.getInt(Tables.User.ID));
                temp.email = res.getString(Tables.User.EMAIL);
                temp.name = res.getString(Tables.User.USERNAME);
                temp.isSigned = false;
                temp.score = res.getInt(Tables.User.POINTS);
                playerVector.add(temp);
            }

            return playerVector;
        } catch (SQLException ex) {
            System.err.println("Err in getPlayers auth: " + ex);
            return null;
        }

    }
}
