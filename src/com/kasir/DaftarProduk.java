package com.kasir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mengelola daftar produk (katalog) dan menyimpannya ke file teks.
 *
 * Format file (satu produk per baris):  nama;harga
 * Contoh:  Gula Pasir 1 kg;17000
 *
 * File dibuat otomatis saat pertama kali program dijalankan.
 * Setiap ada produk ditambah / diubah / dihapus, file langsung diperbarui.
 */
public class DaftarProduk {

    private final List<Produk> daftar = new ArrayList<>();
    private final File file;
    private boolean gagalMuat = false;

    public DaftarProduk(String namaFile) {
        this.file = new File(namaFile);
        muat();
    }

    // ------------------------------------------------------------ Baca / tulis file

    private void muat() {
        if (!file.exists()) {
            isiContoh();
            simpan();
            return;
        }
        try {
            List<String> baris = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            for (String b : baris) {
                Produk p = parseBaris(b);
                if (p != null) {
                    daftar.add(p);
                }
            }
            urutkan();
        } catch (IOException e) {
            gagalMuat = true;
        }
    }

    private Produk parseBaris(String baris) {
        String b = baris.replace("﻿", "").trim();
        int pisah = b.lastIndexOf(';');
        if (pisah <= 0) {
            return null;
        }
        String nama = b.substring(0, pisah).trim();
        String teksHarga = b.substring(pisah + 1).trim();
        if (nama.isEmpty()) {
            return null;
        }
        try {
            long harga = Transaksi.parseAngka(teksHarga);
            if (harga <= 0) {
                return null;
            }
            return new Produk(nama, harga);
        } catch (NumberFormatException e) {
            return null; // baris rusak dilewati
        }
    }

    /** Menulis seluruh daftar ke file. Mengembalikan true jika berhasil. */
    public boolean simpan() {
        List<String> baris = new ArrayList<>();
        for (Produk p : daftar) {
            baris.add(p.getNama() + ";" + p.getHarga());
        }
        try {
            Files.write(file.toPath(), baris, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void isiContoh() {
        daftar.add(new Produk("Beras 5 kg", 68000));
        daftar.add(new Produk("Minyak Goreng 1 L", 19000));
        daftar.add(new Produk("Gula Pasir 1 kg", 17000));
        daftar.add(new Produk("Telur Ayam 1 kg", 29000));
        daftar.add(new Produk("Mi Instan Goreng", 3500));
        daftar.add(new Produk("Air Mineral 600 ml", 4000));
        daftar.add(new Produk("Teh Botol", 5000));
        daftar.add(new Produk("Kopi Bubuk 165 g", 15000));
        daftar.add(new Produk("Susu Kental Manis", 12000));
        daftar.add(new Produk("Sabun Mandi", 4500));
        daftar.add(new Produk("Sampo Sachet", 1500));
        daftar.add(new Produk("Deterjen 800 g", 22000));
        daftar.add(new Produk("Buku Tulis", 5000));
        daftar.add(new Produk("Pulpen", 3000));
        urutkan();
    }

    // ------------------------------------------------------------ Operasi daftar

    private static String normal(String nama) {
        // titik koma dipakai sebagai pemisah di file, jadi diganti spasi;
        // spasi ganda diringkas menjadi satu
        return nama.replace(";", " ").replaceAll("\\s+", " ").trim();
    }

    private void urutkan() {
        Collections.sort(daftar, (a, b) -> a.getNama().compareToIgnoreCase(b.getNama()));
    }

    /** Mencari produk berdasarkan nama (huruf besar/kecil tidak dibedakan). Null jika tidak ada. */
    public Produk cari(String nama) {
        String n = normal(nama);
        for (Produk p : daftar) {
            if (p.getNama().equalsIgnoreCase(n)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Menambah produk baru, atau memperbarui harganya jika namanya sudah ada.
     * Langsung disimpan ke file. Mengembalikan true jika file berhasil ditulis.
     */
    public boolean simpanProduk(String nama, long harga) {
        String n = normal(nama);
        Produk lama = cari(n);
        String namaDipakai = n;
        if (lama != null) {
            namaDipakai = lama.getNama(); // pertahankan penulisan nama yang sudah ada
            daftar.remove(lama);
        }
        daftar.add(new Produk(namaDipakai, harga));
        urutkan();
        return simpan();
    }

    /** Menghapus produk dari daftar dan file. Mengembalikan true jika file berhasil ditulis. */
    public boolean hapusProduk(String nama) {
        Produk p = cari(nama);
        if (p != null) {
            daftar.remove(p);
        }
        return simpan();
    }

    /** Salinan seluruh daftar (sudah terurut A-Z). */
    public List<Produk> getDaftar() {
        return new ArrayList<>(daftar);
    }

    /** Daftar produk yang namanya mengandung kata tertentu. Kata kosong = semua produk. */
    public List<Produk> filter(String kata) {
        String k = kata.trim().toLowerCase();
        List<Produk> hasil = new ArrayList<>();
        for (Produk p : daftar) {
            if (k.isEmpty() || p.getNama().toLowerCase().contains(k)) {
                hasil.add(p);
            }
        }
        return hasil;
    }

    public boolean isGagalMuat() {
        return gagalMuat;
    }

    public String getLokasiFile() {
        return file.getAbsolutePath();
    }
}
