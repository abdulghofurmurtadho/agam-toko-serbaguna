package com.kasir;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Tampilan utama aplikasi kasir (Java Swing).
 * Ada dua tab: "Kasir" untuk transaksi dan "Daftar Produk" untuk mengelola katalog.
 */
public class FrameKasir extends JFrame {

    private static final long serialVersionUID = 1L;

    // Objek logika: keranjang belanja dan daftar produk (disimpan di file produk.csv)
    private final Transaksi transaksi = new Transaksi();
    private final DaftarProduk daftarProduk = new DaftarProduk("produk.csv");

    // ---- komponen tab Kasir
    private JComboBox<String> cmbProduk;
    private boolean sedangIsiCombo = false; // mencegah event saat dropdown diisi ulang
    private JTextField txtNama;
    private JTextField txtHarga;
    private JTextField txtJumlah;
    private JTextField txtBayar;
    private JLabel lblTotal;
    private JLabel lblKembalian;
    private JTable tabel;
    private DefaultTableModel modelTabel;

    // ---- komponen tab Daftar Produk
    private JTextField txtCari;
    private JTextField txtProdukNama;
    private JTextField txtProdukHarga;
    private JTable tabelProduk;
    private DefaultTableModel modelProduk;
    private List<Produk> produkTampil = new ArrayList<>(); // produk yang sedang tampil di tabel

    public FrameKasir() {
        initKomponen();
        isiCombo();
        refreshTabelProduk();
        if (daftarProduk.isGagalMuat()) {
            JOptionPane.showMessageDialog(this,
                    "File daftar produk tidak bisa dibaca:\n" + daftarProduk.getLokasiFile()
                    + "\nDaftar produk akan kosong. Anda tetap bisa memakai kasir secara manual.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void initKomponen() {
        setTitle(Transaksi.NAMA_TOKO + " - Kasir");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 8));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buatHeader(), BorderLayout.NORTH);

        JTabbedPane tab = new JTabbedPane();
        tab.addTab("Kasir", buatTabKasir());
        tab.addTab("Daftar Produk", buatTabProduk());
        add(tab, BorderLayout.CENTER);

        setMinimumSize(new Dimension(860, 680));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buatHeader() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        JLabel judul = new JLabel(Transaksi.NAMA_TOKO.toUpperCase(), SwingConstants.CENTER);
        judul.setFont(new Font("SansSerif", Font.BOLD, 24));
        JLabel sub = new JLabel("Aplikasi Kasir (Point of Sale)", SwingConstants.CENTER);
        sub.setForeground(Color.GRAY);
        panel.add(judul);
        panel.add(sub);
        return panel;
    }

    // ================================================================ TAB KASIR

    private JPanel buatTabKasir() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(buatPanelInput(), BorderLayout.NORTH);
        panel.add(buatPanelTabel(), BorderLayout.CENTER);
        panel.add(buatPanelPembayaran(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buatPanelInput() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Input Barang"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        cmbProduk = new JComboBox<>();
        cmbProduk.setMaximumRowCount(12);
        cmbProduk.addActionListener(e -> pilihDariCombo());

        txtNama = new JTextField(18);
        txtHarga = new JTextField(10);
        txtJumlah = new JTextField(5);
        formatSaatKeluar(txtHarga);

        JButton btnTambah = new JButton("Tambah ke Keranjang");
        btnTambah.addActionListener(e -> tambahBarang());
        txtJumlah.addActionListener(e -> tambahBarang()); // Enter di kolom jumlah

        JButton btnHapus = new JButton("Hapus Item Terpilih");
        btnHapus.addActionListener(e -> hapusItemTerpilih());

        JLabel lblPetunjuk = new JLabel("Pilih dari daftar atau ketik manual. Barang baru bisa disimpan ke daftar produk.");
        lblPetunjuk.setForeground(Color.GRAY);

        // baris 0: dropdown produk
        c.gridx = 0; c.gridy = 0; c.gridwidth = 1; c.weightx = 0;
        panel.add(new JLabel("Pilih Produk"), c);
        c.gridx = 1; c.gridwidth = 5; c.weightx = 1;
        panel.add(cmbProduk, c);

        // baris 1: nama, harga, jumlah
        c.gridy = 1; c.gridwidth = 1;
        c.gridx = 0; c.weightx = 0;
        panel.add(new JLabel("Nama Barang"), c);
        c.gridx = 1; c.weightx = 1;
        panel.add(txtNama, c);
        c.gridx = 2; c.weightx = 0;
        panel.add(new JLabel("Harga (Rp)"), c);
        c.gridx = 3; c.weightx = 0.5;
        panel.add(txtHarga, c);
        c.gridx = 4; c.weightx = 0;
        panel.add(new JLabel("Jumlah"), c);
        c.gridx = 5; c.weightx = 0.2;
        panel.add(txtJumlah, c);

        // baris 2: tombol
        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelTombol.add(btnTambah);
        panelTombol.add(btnHapus);
        c.gridx = 1; c.gridy = 2; c.gridwidth = 5; c.weightx = 1;
        panel.add(panelTombol, c);

        // baris 3: petunjuk
        c.gridy = 3;
        panel.add(lblPetunjuk, c);

        return panel;
    }

    private JScrollPane buatPanelTabel() {
        modelTabel = new DefaultTableModel(
                new Object[]{"No", "Nama Barang", "Harga", "Jumlah", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabel = new JTable(modelTabel);
        tabel.setRowHeight(24);
        tabel.getTableHeader().setReorderingAllowed(false);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer kanan = new DefaultTableCellRenderer();
        kanan.setHorizontalAlignment(SwingConstants.RIGHT);
        tabel.getColumnModel().getColumn(2).setCellRenderer(kanan);
        tabel.getColumnModel().getColumn(3).setCellRenderer(kanan);
        tabel.getColumnModel().getColumn(4).setCellRenderer(kanan);
        tabel.getColumnModel().getColumn(0).setMaxWidth(50);

        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setBorder(BorderFactory.createTitledBorder("Daftar Belanja"));
        scroll.setPreferredSize(new Dimension(700, 200));
        return scroll;
    }

    private JPanel buatPanelPembayaran() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel info = new JPanel(new GridLayout(3, 2, 8, 8));
        info.setBorder(BorderFactory.createTitledBorder("Pembayaran"));

        lblTotal = new JLabel(Transaksi.formatRupiah(0), SwingConstants.RIGHT);
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTotal.setForeground(new Color(0, 100, 0));

        txtBayar = new JTextField();
        txtBayar.setHorizontalAlignment(SwingConstants.RIGHT);
        txtBayar.setFont(new Font("SansSerif", Font.PLAIN, 18));
        saatBerubah(txtBayar, () -> updateKembalian());

        lblKembalian = new JLabel(Transaksi.formatRupiah(0), SwingConstants.RIGHT);
        lblKembalian.setFont(new Font("SansSerif", Font.BOLD, 22));

        info.add(new JLabel("Total Bayar"));
        info.add(lblTotal);
        info.add(new JLabel("Uang Bayar (Rp)"));
        info.add(txtBayar);
        info.add(new JLabel("Kembalian"));
        info.add(lblKembalian);

        JButton btnBayar = new JButton("Bayar & Cetak Struk");
        btnBayar.setFont(btnBayar.getFont().deriveFont(Font.BOLD, 14f));
        btnBayar.addActionListener(e -> bayarDanCetak());

        JButton btnBaru = new JButton("Transaksi Baru");
        btnBaru.setFont(btnBaru.getFont().deriveFont(Font.BOLD, 14f));
        btnBaru.addActionListener(e -> transaksiBaru());

        JPanel tombol = new JPanel(new GridLayout(2, 1, 8, 8));
        tombol.add(btnBayar);
        tombol.add(btnBaru);

        panel.add(info, BorderLayout.CENTER);
        panel.add(tombol, BorderLayout.EAST);
        return panel;
    }

    // ================================================================ TAB DAFTAR PRODUK

    private JPanel buatTabProduk() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // atas: kotak pencarian
        JPanel atas = new JPanel(new BorderLayout(8, 0));
        atas.add(new JLabel("Cari produk:"), BorderLayout.WEST);
        txtCari = new JTextField();
        saatBerubah(txtCari, () -> refreshTabelProduk());
        atas.add(txtCari, BorderLayout.CENTER);

        // tengah: tabel produk
        modelProduk = new DefaultTableModel(new Object[]{"No", "Nama Produk", "Harga"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelProduk = new JTable(modelProduk);
        tabelProduk.setRowHeight(24);
        tabelProduk.getTableHeader().setReorderingAllowed(false);
        tabelProduk.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer kanan = new DefaultTableCellRenderer();
        kanan.setHorizontalAlignment(SwingConstants.RIGHT);
        tabelProduk.getColumnModel().getColumn(2).setCellRenderer(kanan);
        tabelProduk.getColumnModel().getColumn(0).setMaxWidth(50);
        // klik satu produk di tabel -> datanya muncul di form bawah (untuk diubah harganya)
        tabelProduk.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                isiFormDariTabelProduk();
            }
        });
        JScrollPane scroll = new JScrollPane(tabelProduk);
        scroll.setBorder(BorderFactory.createTitledBorder("Daftar Produk"));

        // bawah: form tambah / ubah produk
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Tambah / Ubah Produk"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        txtProdukNama = new JTextField(20);
        txtProdukHarga = new JTextField(10);
        formatSaatKeluar(txtProdukHarga);

        JButton btnSimpan = new JButton("Simpan Produk");
        btnSimpan.addActionListener(e -> simpanProdukDariTab());
        txtProdukHarga.addActionListener(e -> simpanProdukDariTab());

        JButton btnHapus = new JButton("Hapus Produk Terpilih");
        btnHapus.addActionListener(e -> hapusProdukTerpilih());

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        form.add(new JLabel("Nama Produk"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(txtProdukNama, c);
        c.gridx = 2; c.weightx = 0;
        form.add(new JLabel("Harga (Rp)"), c);
        c.gridx = 3; c.weightx = 0.5;
        form.add(txtProdukHarga, c);

        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelTombol.add(btnSimpan);
        panelTombol.add(btnHapus);
        c.gridx = 1; c.gridy = 1; c.gridwidth = 3; c.weightx = 1;
        form.add(panelTombol, c);

        JLabel lblFile = new JLabel("Tersimpan otomatis di: " + daftarProduk.getLokasiFile());
        lblFile.setForeground(Color.GRAY);
        c.gridy = 2;
        form.add(lblFile, c);

        panel.add(atas, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(form, BorderLayout.SOUTH);
        return panel;
    }

    // ================================================================ AKSI: KASIR

    /** Dipanggil saat user memilih produk di dropdown: nama & harga terisi otomatis. */
    private void pilihDariCombo() {
        if (sedangIsiCombo) {
            return;
        }
        int idx = cmbProduk.getSelectedIndex();
        if (idx <= 0) {
            return; // 0 = tulisan "-- Pilih produk --"
        }
        List<Produk> semua = daftarProduk.getDaftar();
        if (idx - 1 >= semua.size()) {
            return;
        }
        Produk p = semua.get(idx - 1);
        txtNama.setText(p.getNama());
        txtHarga.setText(Transaksi.formatAngka(p.getHarga()));
        if (txtJumlah.getText().trim().isEmpty()) {
            txtJumlah.setText("1");
        }
        txtJumlah.requestFocus();
        txtJumlah.selectAll();
    }

    private void tambahBarang() {
        String nama = txtNama.getText().trim();
        if (nama.isEmpty()) {
            tampilkanError("Nama barang tidak boleh kosong.", txtNama);
            return;
        }

        Produk diDaftar = daftarProduk.cari(nama);

        long harga;
        if (txtHarga.getText().trim().isEmpty() && diDaftar != null) {
            harga = diDaftar.getHarga(); // harga dikosongkan -> pakai harga dari daftar
        } else {
            try {
                harga = Transaksi.parseAngka(txtHarga.getText());
            } catch (NumberFormatException ex) {
                tampilkanError("Harga harus berupa angka. Contoh: 2000000 atau 2.000.000", txtHarga);
                return;
            }
        }
        if (harga <= 0) {
            tampilkanError("Harga harus lebih dari 0.", txtHarga);
            return;
        }

        int jumlah;
        try {
            long j = Transaksi.parseAngka(txtJumlah.getText());
            if (j <= 0 || j > Integer.MAX_VALUE) {
                throw new NumberFormatException();
            }
            jumlah = (int) j;
        } catch (NumberFormatException ex) {
            tampilkanError("Jumlah beli harus berupa angka bulat lebih dari 0.", txtJumlah);
            return;
        }

        String namaFinal = (diDaftar != null) ? diDaftar.getNama() : nama;
        transaksi.tambahBarang(new Barang(namaFinal, harga, jumlah));
        refreshTabel();

        tawarkanSimpanKeDaftar(diDaftar, nama, harga);
        bersihkanInput();
    }

    /** Jika produk belum ada di daftar (atau harganya beda), tanyakan apakah mau disimpan. */
    private void tawarkanSimpanKeDaftar(Produk diDaftar, String nama, long harga) {
        if (diDaftar == null) {
            int pilih = JOptionPane.showConfirmDialog(this,
                    "\"" + nama + "\" belum ada di daftar produk.\n"
                    + "Simpan ke daftar produk dengan harga " + Transaksi.formatRupiah(harga) + "?",
                    "Produk Baru", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (pilih == JOptionPane.YES_OPTION) {
                simpanKeDaftar(nama, harga);
            }
        } else if (diDaftar.getHarga() != harga) {
            int pilih = JOptionPane.showConfirmDialog(this,
                    "Harga \"" + diDaftar.getNama() + "\" di daftar: "
                    + Transaksi.formatRupiah(diDaftar.getHarga()) + "\n"
                    + "Harga yang Anda masukkan: " + Transaksi.formatRupiah(harga) + "\n"
                    + "Perbarui harga di daftar produk?",
                    "Harga Berbeda", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (pilih == JOptionPane.YES_OPTION) {
                simpanKeDaftar(diDaftar.getNama(), harga);
            }
        }
    }

    private void simpanKeDaftar(String nama, long harga) {
        boolean berhasil = daftarProduk.simpanProduk(nama, harga);
        isiCombo();
        refreshTabelProduk();
        if (!berhasil) {
            tampilkanGagalSimpan();
        }
    }

    private void hapusItemTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this, "Pilih dulu barang di tabel yang akan dihapus.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        transaksi.hapusBarang(baris);
        refreshTabel();
    }

    private void refreshTabel() {
        modelTabel.setRowCount(0);
        int no = 1;
        for (Barang b : transaksi.getDaftarBarang()) {
            modelTabel.addRow(new Object[]{
                no++,
                b.getNama(),
                Transaksi.formatRupiah(b.getHarga()),
                b.getJumlah(),
                Transaksi.formatRupiah(b.getSubtotal())
            });
        }
        lblTotal.setText(Transaksi.formatRupiah(transaksi.hitungTotal()));
        updateKembalian();
    }

    private void updateKembalian() {
        String teks = txtBayar.getText().trim();
        if (teks.isEmpty()) {
            lblKembalian.setText(Transaksi.formatRupiah(0));
            lblKembalian.setForeground(Color.BLACK);
            return;
        }
        try {
            long bayar = Transaksi.parseAngka(teks);
            long kembali = transaksi.hitungKembalian(bayar);
            if (kembali < 0) {
                lblKembalian.setText("Kurang " + Transaksi.formatRupiah(-kembali));
                lblKembalian.setForeground(Color.RED);
            } else {
                lblKembalian.setText(Transaksi.formatRupiah(kembali));
                lblKembalian.setForeground(Color.BLACK);
            }
        } catch (NumberFormatException ex) {
            lblKembalian.setText("-");
            lblKembalian.setForeground(Color.RED);
        }
    }

    private void bayarDanCetak() {
        if (transaksi.isKosong()) {
            tampilkanError("Keranjang masih kosong. Tambahkan barang terlebih dahulu.", txtNama);
            return;
        }

        long bayar;
        try {
            bayar = Transaksi.parseAngka(txtBayar.getText());
        } catch (NumberFormatException ex) {
            tampilkanError("Uang bayar harus berupa angka. Contoh: 2500000 atau 2.500.000", txtBayar);
            return;
        }

        if (bayar < transaksi.hitungTotal()) {
            tampilkanError("Uang bayar kurang dari total belanja.", txtBayar);
            return;
        }

        JTextArea area = new JTextArea(transaksi.cetakStruk(bayar));
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(440, 400));

        JOptionPane.showMessageDialog(this, scroll, "Struk Pembelian", JOptionPane.PLAIN_MESSAGE);
    }

    private void transaksiBaru() {
        transaksi.reset();
        modelTabel.setRowCount(0);
        txtBayar.setText("");
        lblTotal.setText(Transaksi.formatRupiah(0));
        lblKembalian.setText(Transaksi.formatRupiah(0));
        lblKembalian.setForeground(Color.BLACK);
        bersihkanInput();
    }

    private void bersihkanInput() {
        txtNama.setText("");
        txtHarga.setText("");
        txtJumlah.setText("");
        cmbProduk.setSelectedIndex(0);
        txtNama.requestFocus();
    }

    /** Mengisi ulang dropdown dari daftar produk terbaru. */
    private void isiCombo() {
        sedangIsiCombo = true;
        cmbProduk.removeAllItems();
        cmbProduk.addItem("-- Pilih produk dari daftar --");
        for (Produk p : daftarProduk.getDaftar()) {
            cmbProduk.addItem(p.toString());
        }
        cmbProduk.setSelectedIndex(0);
        sedangIsiCombo = false;
    }

    // ================================================================ AKSI: DAFTAR PRODUK

    private void refreshTabelProduk() {
        produkTampil = daftarProduk.filter(txtCari.getText());
        modelProduk.setRowCount(0);
        int no = 1;
        for (Produk p : produkTampil) {
            modelProduk.addRow(new Object[]{no++, p.getNama(), Transaksi.formatRupiah(p.getHarga())});
        }
    }

    private void isiFormDariTabelProduk() {
        int baris = tabelProduk.getSelectedRow();
        if (baris >= 0 && baris < produkTampil.size()) {
            Produk p = produkTampil.get(baris);
            txtProdukNama.setText(p.getNama());
            txtProdukHarga.setText(Transaksi.formatAngka(p.getHarga()));
        }
    }

    private void simpanProdukDariTab() {
        String nama = txtProdukNama.getText().trim();
        if (nama.isEmpty()) {
            tampilkanError("Nama produk tidak boleh kosong.", txtProdukNama);
            return;
        }

        long harga;
        try {
            harga = Transaksi.parseAngka(txtProdukHarga.getText());
        } catch (NumberFormatException ex) {
            tampilkanError("Harga harus berupa angka. Contoh: 3500 atau 3.500", txtProdukHarga);
            return;
        }
        if (harga <= 0) {
            tampilkanError("Harga harus lebih dari 0.", txtProdukHarga);
            return;
        }

        boolean sudahAda = daftarProduk.cari(nama) != null;
        boolean berhasil = daftarProduk.simpanProduk(nama, harga);
        isiCombo();
        refreshTabelProduk();

        if (!berhasil) {
            tampilkanGagalSimpan();
        } else {
            JOptionPane.showMessageDialog(this,
                    sudahAda ? "Harga produk berhasil diperbarui." : "Produk baru berhasil disimpan.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        }
        txtProdukNama.setText("");
        txtProdukHarga.setText("");
        txtProdukNama.requestFocus();
    }

    private void hapusProdukTerpilih() {
        int baris = tabelProduk.getSelectedRow();
        if (baris < 0 || baris >= produkTampil.size()) {
            JOptionPane.showMessageDialog(this, "Pilih dulu produk di tabel yang akan dihapus.",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Produk p = produkTampil.get(baris);
        int pilih = JOptionPane.showConfirmDialog(this,
                "Hapus \"" + p.getNama() + "\" dari daftar produk?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (pilih != JOptionPane.YES_OPTION) {
            return;
        }
        boolean berhasil = daftarProduk.hapusProduk(p.getNama());
        isiCombo();
        refreshTabelProduk();
        txtProdukNama.setText("");
        txtProdukHarga.setText("");
        if (!berhasil) {
            tampilkanGagalSimpan();
        }
    }

    // ================================================================ BANTUAN

    private void tampilkanError(String pesan, JTextField fokus) {
        JOptionPane.showMessageDialog(this, pesan, "Input Tidak Valid", JOptionPane.WARNING_MESSAGE);
        if (fokus != null) {
            fokus.requestFocus();
            fokus.selectAll();
        }
    }

    private void tampilkanGagalSimpan() {
        JOptionPane.showMessageDialog(this,
                "Perubahan tidak bisa disimpan ke file:\n" + daftarProduk.getLokasiFile()
                + "\nPastikan folder tidak terkunci (read-only).",
                "Gagal Menyimpan", JOptionPane.ERROR_MESSAGE);
    }

    /** Saat user meninggalkan kolom harga, angka dirapikan: 2000000 -> 2.000.000. */
    private void formatSaatKeluar(JTextField field) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                try {
                    long angka = Transaksi.parseAngka(field.getText());
                    field.setText(Transaksi.formatAngka(angka));
                } catch (NumberFormatException ex) {
                    // dibiarkan; divalidasi saat tombol simpan/tambah ditekan
                }
            }
        });
    }

    /** Menjalankan 'aksi' setiap kali isi kolom teks berubah (diketik / dihapus). */
    private static void saatBerubah(JTextField field, Runnable aksi) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                aksi.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                aksi.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                aksi.run();
            }
        });
    }
}
