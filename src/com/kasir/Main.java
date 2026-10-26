package com.kasir;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Class utama untuk menjalankan aplikasi kasir Agam Toko Serbaguna.
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Jika gagal, pakai tampilan default Swing
        }
        SwingUtilities.invokeLater(() -> new FrameKasir().setVisible(true));
    }
}
