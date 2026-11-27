package com.lensrent.ui;

import com.lensrent.model.Pengguna;
import com.lensrent.service.AlatService;
import com.lensrent.service.AuthService;
import com.lensrent.service.PembayaranService;
import com.lensrent.service.TransaksiService;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Sidebar gelap di DashboardAdminFrame. */
public class Sidebar extends JPanel {

    /** daftarMenu: nama menu -> aksi saat diklik; aksi null = menu belum dibuat (nonaktif). */
    public Sidebar(JFrame frame, AuthService authService, AlatService alatService, TransaksiService transaksiService, PembayaranService pembayaranService,
                   Pengguna pengguna, Map<String, Runnable> daftarMenu) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Tema.HITAM);
        setPreferredSize(new Dimension(260, 0));
        setBorder(BorderFactory.createEmptyBorder(28, 24, 24, 24));

        JLabel logo = Tema.logo("logo-putih.png", 212, Tema.PUTIH);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(logo);
        add(Box.createVerticalStrut(28));
        add(label(pengguna.getNama(), Font.BOLD, 15, Tema.PUTIH));
        add(label("Admin", Font.PLAIN, 13, Tema.GARIS)); // hanya admin yang bisa login
        add(Box.createVerticalStrut(28));

        List<JButton> semuaMenu = new ArrayList<>();
        for (Map.Entry<String, Runnable> menu : daftarMenu.entrySet()) {
            JButton tombolMenu = new JButton(menu.getKey());
            if (menu.getValue() == null) {
                tombolMenu.setEnabled(false);
                tombolMenu.setToolTipText("Dibuat di tahap berikutnya");
            } else {
                tombolMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                tombolMenu.addActionListener(e -> {
                    semuaMenu.forEach(t -> tandai(t, t == tombolMenu));
                    menu.getValue().run();
                });
            }
            tombolMenu.setHorizontalAlignment(JButton.LEFT);
            tombolMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
            tombolMenu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            // tombol gaya toolbar: latar hanya muncul saat kursor di atasnya
            tombolMenu.putClientProperty("JButton.buttonType", "toolBarButton");
            tombolMenu.putClientProperty("FlatLaf.style", "hoverBackground: #2C2724; pressedBackground: #3A332E; arc: 10");
            tombolMenu.setFocusPainted(false);
            tandai(tombolMenu, false);
            semuaMenu.add(tombolMenu);
            add(tombolMenu);
        }
        // Halaman pertama setelah login adalah menu aktif pertama
        semuaMenu.stream().filter(JButton::isEnabled).findFirst().ifPresent(t -> tandai(t, true));

        add(Box.createVerticalGlue());

        JButton tombolLogout = Tema.tombolUtama("Logout");
        tombolLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tombolLogout.addActionListener(e -> new KonfirmasiLogoutDialog(frame, authService, alatService, transaksiService, pembayaranService).setVisible(true));
        add(tombolLogout);
    }

    /** Menu yang sedang dibuka: teks oranye tebal dengan garis oranye di kiri. */
    private static void tandai(JButton tombol, boolean aktif) {
        tombol.setFont(Tema.font(aktif ? Font.BOLD : Font.PLAIN, 14));
        if (tombol.isEnabled()) {
            tombol.setForeground(aktif ? Tema.ORANYE_TERANG : Tema.PUTIH);
        }
        tombol.setBorder(aktif
                ? BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, Tema.ORANYE_TERANG),
                        BorderFactory.createEmptyBorder(8, 11, 8, 0))
                : BorderFactory.createEmptyBorder(8, 14, 8, 0));
    }

    private static JLabel label(String teks, int gaya, int ukuran, Color warna) {
        JLabel label = new JLabel(teks);
        label.setFont(Tema.font(gaya, ukuran));
        label.setForeground(warna);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
