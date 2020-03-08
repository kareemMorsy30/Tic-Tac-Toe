package controller;

import java.io.Serializable;

public class Player implements Serializable {

    public Player(int id) {
        this.id = id;
    }
    public int DB_id;
    public boolean isSigned = false;
    private String name;
    private int score;
    public int id = 1;
    public String password;
    public String email;
    private String level;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the score
     */
    public int getScore() {
        return score;
    }

    public String isIsSigned() {
        if (isSigned) {
            return "Online";
        }
        return "Offline";
    }

    /**
     * @return the level
     */
    public String getLevel() {
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
}
