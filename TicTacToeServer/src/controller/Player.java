package controller;


import java.io.Serializable;

public class Player implements Serializable {
    public Player(int id){
        this.id = id;
    }
    public boolean isSigned = false;
    public String name;
    public int score;
    public int id;
    public String password;
    public String email;
    private Score handleScore;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public int getDB_id() {
        return id;
    }

    public void setDB_id(int DB_id) {
        this.id = DB_id;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public void setScore() {
        handleScore = new Score();
        score = handleScore.getScore(this.id);
    }
    
    public int getScore(){
        return score;
    }

    public boolean updateScore(int score) {
        this.score = this.score + score;
        handleScore = new Score();
        return handleScore.updateScore(this.id, score);
    }

    /**
     * @return the isSigned
     */
    public String isIsSigned() {
        if (isSigned) {
          return "Online";
        }
        return "Offline";
    }
}