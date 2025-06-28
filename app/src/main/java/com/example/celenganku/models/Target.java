package com.example.celenganku.models;

import java.util.Date;

public class Target {
    private int id;
    private String nama;
    private int targetNominal;
    private int terkumpul;
    private Date targetDate;

    public Target(int id, String nama, int targetNominal, int terkumpul, Date targetDate) {
        this.id = id;
        this.nama = nama;
        this.targetNominal = targetNominal;
        this.terkumpul = terkumpul;
        this.targetDate = targetDate;
    }

    // Getters
    public int getId() { return id; }
    public String getNama() { return nama; }
    public int getTargetNominal() { return targetNominal; }
    public int getTerkumpul() { return terkumpul; }
    public Date getTargetDate() { return targetDate; }

    public int getProgress() {
        if (targetNominal == 0) return 0;
        return (terkumpul * 100) / targetNominal;
    }
}