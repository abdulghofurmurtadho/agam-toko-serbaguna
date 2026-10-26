package com.kasir;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Menyimpan daftar belanja, menghitung total & kembalian, serta membuat teks struk.
 */
public class Transaksi {

    /** Nama toko: cukup diubah di sini, judul jendela dan struk ikut berubah. */
    public static final String NAMA_TOKO = "Agam Toko Serbaguna";

    private static final int LEBAR_STRUK = 40;

    private final List<Barang> daftarBarang = new ArrayList<>();

    // ------------------------------------------------------------ Keranjang

    public void tambahBarang(Barang barang) {
        daftarBarang.add(barang);
    }

    public void hapusBarang(int index) {
        if (index >= 0 && index < daftarBarang.size()) {
            daftarBarang.remove(index);
        }
    }

    public List<Barang> getDaftarBarang() {
        return daftarBarang;
    }

    public boolean isKosong() {
        return daftarBarang.isEmpty();
    }

    public void reset() {
        daftarBarang.clear();
    }

    // ------------------------------------------------------------ Perhitungan

    public long hitungTotal() {
        long total = 0;
        for (Barang b : daftarBarang) {
            total += b.getSubtotal();
        }
        return total;
    }

    public long hitungKembalian(long uangBayar) {
        return uangBayar - hitungTotal();
    }

    // ------------------------------------------------------------ Angka & format

    /**
     * Membersihkan input angka dari tanda titik, koma, spasi, dan teks "Rp",
     * lalu mengubahnya menjadi long. Contoh: "2.000.000" -> 2000000.
     *
     * @throws NumberFormatException jika hasilnya bukan angka valid
     */
    public static long parseAngka(String teks) {
        if (teks == null) {
            throw new NumberFormatException("Input kosong");
        }
        String bersih = teks.replaceAll("(?i)rp", "").replaceAll("[.,\\s]", "");
        if (bersih.isEmpty()) {
            throw new NumberFormatException("Input kosong");
        }
        return Long.parseLong(bersih);
    }

    /** 2000000 -> "2.000.000" */
    public static String formatAngka(long nilai) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"));
        return nf.format(nilai);
    }

    /** 2000000 -> "Rp 2.000.000" */
    public static String formatRupiah(long nilai) {
        return "Rp " + formatAngka(nilai);
    }

    // ------------------------------------------------------------ Struk

    private static String ulang(char karakter, int jumlah) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < jumlah; i++) {
            sb.append(karakter);
        }
        return sb.toString();
    }

    private static String tengah(String teks) {
        int spasi = Math.max(0, (LEBAR_STRUK - teks.length()) / 2);
        return ulang(' ', spasi) + teks;
    }

    private static String kiriKanan(String kiri, String kanan) {
        int spasi = Math.max(1, LEBAR_STRUK - kiri.length() - kanan.length());
        return kiri + ulang(' ', spasi) + kanan;
    }

    public String cetakStruk(long uangBayar) {
        StringBuilder sb = new StringBuilder();
        String garis = ulang('=', LEBAR_STRUK);
        String garisPutus = ulang('-', LEBAR_STRUK);

        sb.append(garis).append("\n");
        sb.append(tengah(NAMA_TOKO.toUpperCase())).append("\n");
        sb.append(tengah("STRUK PEMBELIAN")).append("\n");
        sb.append(garis).append("\n");
        sb.append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())).append("\n");
        sb.append(garisPutus).append("\n");

        for (Barang b : daftarBarang) {
            sb.append(b.getNama()).append("\n");
            String kiri = "  " + b.getJumlah() + " x " + formatRupiah(b.getHarga());
            sb.append(kiriKanan(kiri, formatRupiah(b.getSubtotal()))).append("\n");
        }

        sb.append(garisPutus).append("\n");
        sb.append(kiriKanan("TOTAL", formatRupiah(hitungTotal()))).append("\n");
        sb.append(kiriKanan("BAYAR", formatRupiah(uangBayar))).append("\n");
        sb.append(kiriKanan("KEMBALI", formatRupiah(hitungKembalian(uangBayar)))).append("\n");
        sb.append(garis).append("\n");
        sb.append(tengah("Terima kasih atas kunjungan Anda")).append("\n");
        sb.append(garis).append("\n");
        return sb.toString();
    }
}
