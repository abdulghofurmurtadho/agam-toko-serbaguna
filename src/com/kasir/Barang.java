package com.kasir;

/**
 * Model data satu barang yang dibeli dalam keranjang belanja.
 */
public class Barang {

    private final String nama;
    private final long harga;
    private final int jumlah;

    public Barang(String nama, long harga, int jumlah) {
        this.nama = nama;
        this.harga = harga;
        this.jumlah = jumlah;
    }

    public String getNama() {
        return nama;
    }

    public long getHarga() {
        return harga;
    }

    public int getJumlah() {
        return jumlah;
    }

    public long getSubtotal() {
        return harga * jumlah;
    }
}
