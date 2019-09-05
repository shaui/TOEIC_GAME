package com.example.toeic_game;

public class Member {

    private String name = "defaultName";
    private int money = 0;
    private int numOfWin = 0;
    private int numOfLose = 0;
    private String imgURL = "";

    public Member(){};

    public Member(String name){
        this.name = name;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public int getNumOfWin() {
        return numOfWin;
    }

    public void setNumOfWin(int numOfWin) {
        this.numOfWin = numOfWin;
    }

    public int getNumOfLose() {
        return numOfLose;
    }

    public void setNumOfLose(int numOfLose) {
        this.numOfLose = numOfLose;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    private double getRateOfWin(){
        if(numOfWin + numOfLose == 0){
            return 1;
        }
        return numOfWin/(numOfWin + numOfLose);
    }
}
