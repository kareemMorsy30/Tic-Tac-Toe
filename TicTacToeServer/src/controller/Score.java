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
import java.util.logging.*;
import models.DBConnection;
import models.Tables;

/**
 *
 * @author kareem
 */
public class Score {

    private int score;
    private Player player;
    private Connection conn;
    
    public Score(){
        score = 0;
    }

    public String getLevel(int score) {
        if (score <= 50) {
            return "Beginner";
        } else if (score <= 100) {
            return "Amateur";
        } else if (score <= 200) {
            return "Semi-Pro";
        } else if (score <= 500) {
            return "Professional";
        } else if (score <= 1000) {
            return "World Class";
        } else if (score <= 1500) {
            return "Legendary";
        }

        return "Ultimate";
    }

    public boolean setScore(int id, int score) {
        if(DBConnection.getConnection() != null){
            try {
                conn = DBConnection.getConnection();
                
                PreparedStatement query = conn.prepareStatement("UPDATE users SET points = ? WHERE id = ?");
                
                query.setInt(1, score);
                query.setInt(2, id);
                
                int rowsUpdated = query.executeUpdate();
                if(rowsUpdated == 0){
                    return false;
                }
            } catch (SQLException ex) {
                Logger.getLogger(Auth.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            return true;
        }else{
            return false;
        }
    }

    public int getScore(int id) {
        if(DBConnection.getConnection() != null){
            try {
                conn = DBConnection.getConnection();
                
                PreparedStatement query = conn.prepareStatement("SELECT * FROM users WHERE id = ? ");
                
                query.setInt(1, id);
                
                ResultSet res = query.executeQuery();
                
                if(res.next()){
                    score = res.getInt(Tables.User.POINTS);
                }
                
                return score;
            } catch (SQLException ex) {
                Logger.getLogger(Auth.class.getName()).log(Level.SEVERE, null, ex);
                return score;
            }
        }else{
            return score;
        }
    }
    
    public boolean updateScore(int id, int score)
    {
        if(DBConnection.getConnection() != null){
            try {
                conn = DBConnection.getConnection();
                // Get user old score and add new one to it
                int oldScore = getScore(id);
                score += oldScore;
                
                PreparedStatement query = conn.prepareStatement("UPDATE users SET points = ? WHERE id = ?");
                
                query.setInt(1, score);
                query.setInt(2, id);
                
                int rowsUpdated = query.executeUpdate();
                if(rowsUpdated == 0){
                    return false;
                }
            } catch (SQLException ex) {
                Logger.getLogger(Auth.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            return true;
        }else{
            return false;
        }
    }
}
