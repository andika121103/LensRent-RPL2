package com.lensrent.ui;

import com.lensrent.service.AlatService;
import com.lensrent.service.AuthService;
import com.lensrent.service.TransaksiService;
import com.lensrent.session.Sesi;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class KonfirmasiLogoutDialog extends JDialog {

    public KonfirmasiLogoutDialog(JFrame frameAktif, AuthService authService, AlatService alatService,
                                  TransaksiService transaksiService) {
        super(frameAktif, "Logout", true); // true = modal

        JLabel judul = new JLabel("Keluar dari LensRent?");
        judul.setFont(Tema.font(Font.BOLD, 20));
        judul.setForeground(Tema.HITAM);

        JLabel teks = new JLabel("Anda akan keluar dari akun ini.");
        teks.setFont(Tema.font(Font.PLAIN, 14));
        teks.setForeground(Tema.TEKS_SEKUNDER);

        JButton tombolBatal = new JButton("Batal");
        JButton tombolLogout = Tema.tombolUtama("Ya, Logout");
        tombolLogout.setPreferredSize(null); // ukuran mengikuti teks, bukan selebar form

        tombolBatal.addActionListener(e -> dispose());
        tombolLogout.addActionListener(e -> {
            Sesi.kosongkan();
            dispose();
            frameAktif.dispose();
            new LoginFrame(authService, alatService, transaksiService).setVisible(true);
        });

        JPanel teksPanel = new JPanel(new BorderLayout(0, 8));
        teksPanel.setOpaque(false);
        teksPanel.add(judul, BorderLayout.NORTH);
        teksPanel.add(teks, BorderLayout.CENTER);

        JPanel tombolPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombolPanel.setOpaque(false);
        tombolPanel.add(tombolBatal);
        tombolPanel.add(tombolLogout);

        JPanel isi = new JPanel(new BorderLayout(0, 24));
        isi.setBackground(Tema.PUTIH);
        isi.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));
        isi.add(teksPanel, BorderLayout.CENTER);
        isi.add(tombolPanel, BorderLayout.SOUTH);
        setContentPane(isi);

        getRootPane().setDefaultButton(tombolBatal); // Enter tidak langsung logout
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(frameAktif);
    }
}
