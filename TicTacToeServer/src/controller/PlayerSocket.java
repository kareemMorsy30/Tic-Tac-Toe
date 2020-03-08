/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;
import java.net.Socket;
import java.util.Vector;


public class PlayerSocket{
    public static Vector<Player> connectedPlayers = new Vector<Player>();
    public Socket socket;
    public final Player myPlayer;

    PlayerSocket(int i) {
        myPlayer = new Player(i);
        connectedPlayers.add(myPlayer);
    }
    
    PlayerSocket(Player p, Socket s) {
        socket = s;
        myPlayer = p;
        connectedPlayers.add(p);
    }
    
    public void remove(){
        boolean isRemoved = connectedPlayers.remove(this.myPlayer);
        System.err.println("Removed Player: "+ isRemoved);
    }
    public void addPlayerData(Player player){
        if(player != null){
            myPlayer.name = player.name;
            myPlayer.email = player.email;
            myPlayer.password = player.password;
            myPlayer.setDB_id(player.getDB_id());
            myPlayer.isSigned = player.isSigned;
            myPlayer.score = player.score;
        }
    }
}
