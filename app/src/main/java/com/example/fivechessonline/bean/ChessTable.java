package com.example.fivechessonline.bean;

public class ChessTable {
    private int id;
    private int user_black;
    private int user_white;
    private String last_check;
    private int game_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_black() {
        return user_black;
    }

    public void setUser_black(int user_black) {
        this.user_black = user_black;
    }

    public int getUser_white() {
        return user_white;
    }

    public void setUser_white(int user_white) {
        this.user_white = user_white;
    }

    public String getLast_check() {
        return last_check;
    }

    public void setLast_check(String last_check) {
        this.last_check = last_check;
    }

    public int getGame_id() {
        return game_id;
    }

    public void setGame_id(int game_id) {
        this.game_id = game_id;
    }
}
