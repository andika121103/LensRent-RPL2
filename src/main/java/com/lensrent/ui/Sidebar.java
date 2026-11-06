package com.lensrent.ui;

import com.lensrent.model.Pengguna;
import com.lensrent.model.Role;
import com.lensrent.service.AlatService;
import com.lensrent.service.AuthService;
import com.lensrent.service.TransaksiService;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Sidebar gelap yang dipakai bersama oleh DashboardAdminFrame dan MenuPenyewaFrame. */
public class Sidebar extends JPanel {

    /** daftarMenu: nama menu -> aksi saat diklik; aksi null = menu belum dibuat (nonaktif). */
    public Sidebar(JFrame frame, AuthService authService, AlatService alatService, TransaksiService transaksiService,
                   Pengguna pengguna, Map<String, Runnable> daftarMenu) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Tema.HITAM);
        setPreferredSize(new Dimension(260, 0));
        setBorder(BorderFactory.createEmptyBorder(28, 24, 24, 24));

        add(label("LensRent", Font.BOLD, 26, Tema.PUTIH));
        add(Box.createVerticalStrut(28));
        add(label(pengguna.getNama(), Font.BOLD, 15, Tema.PUTIH));
        String peran = pengguna.getRole() == Role.ADMIN ? "Admin" : "Penyewa";
        add(label(peran, Font.PLAIN, 13, Tema.GARIS));
        add(Box.createVerticalStrut(28));

        for (Map.Entry<String, Runnable> menu : daftarMenu.entrySet()) {
            JButton tombolMenu = new JButton(menu.getKey());
            if (menu.getValue() == null) {
                tombolMenu.setEnabled(false);
                tombolMenu.setToolTipText("Dibuat di tahap berikutnya");
            } else {
                tombolMenu.setForeground(Tema.PUTIH);
                tombolMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                tombolMenu.addActionListener(e -> menu.getValue().run());
            }
            tombolMenu.setHorizontalAlignment(JButton.LEFT);
            tombolMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
            tombolMenu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            tombolMenu.setFont(Tema.font(Font.PLAIN, 14));
            tombolMenu.setContentAreaFilled(false);
            tombolMenu.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            add(tombolMenu);
        }

        add(Box.createVerticalGlue());

        JButton tombolLogout = Tema.tombolUtama("Logout");
        tombolLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tombolLogout.addActionListener(e -> new KonfirmasiLogoutDialog(frame, authService, alatService, transaksiService).setVisible(true));
        add(tombolLogout);
    }

    private static JLabel label(String teks, int gaya, int ukuran, Color warna) {
        JLabel label = new JLabel(teks);
        label.setFont(Tema.font(gaya, ukuran));
        label.setForeground(warna);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
