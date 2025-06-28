// Transaksi.java
package com.example.celenganku.models;

import java.util.Date;

public class Transaksi {
    private int id;
    private String jenis;
    private int nominal;
    private String deskripsi;
    private Date tanggal;

    public Transaksi(int id, String jenis, int nominal, String deskripsi, Date tanggal) {
        this.id = id;
        this.jenis = jenis;
        this.nominal = nominal;
        this.deskripsi = deskripsi;
        this.tanggal = tanggal;
    }

    // Getters
    public int getId() { return id; }
    public String getJenis() { return jenis; }
    public int getNominal() { return nominal; }
    public String getDeskripsi() { return deskripsi; }
    public Date getTanggal() { return tanggal; }
}