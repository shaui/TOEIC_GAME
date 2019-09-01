package com.example.toeic_game;

public class Member {

    private String name = "defaultName";
    private int money = 0;

    public Member(){};

    public Member(String name){
        this.name = name;
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
}
