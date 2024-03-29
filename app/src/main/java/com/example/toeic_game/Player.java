package com.example.toeic_game;

public class Player {
    private String name;
    private int score = 0;
    //不寫會報錯，為了預防data沒有填入數據
    public Player(){
        name = "no_name";
    }

    public Player(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int addScore(int score){
        return this.score + score;
    }
}
