package com.kasir;

/**
 * Model data satu produk di daftar produk (katalog toko).
 * Berbeda dengan Barang: Produk = isi katalog (nama + harga),
 * sedangkan Barang = yang dibeli pelanggan (ada jumlahnya).
 */
public class Produk {

    private final String nama;
    private final long harga;

    public Produk(String nama, long harga) {
        this.nama = nama;
        this.harga = harga;
    }

    public String getNama() {
        return nama;
    }

    public long getHarga() {
        return harga;
    }

    /** Teks yang tampil di dropdown, contoh: "Indomie - Rp 3.500". */
    @Override
    public String toString() {
        return nama + " - " + Transaksi.formatRupiah(harga);
    }
}
