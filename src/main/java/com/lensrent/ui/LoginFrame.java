package com.lensrent.ui;

import com.lensrent.model.Pengguna;
import com.lensrent.service.AlatService;
import com.lensrent.service.AuthService;
import com.lensrent.service.PembayaranService;
import com.lensrent.service.TransaksiService;
import com.lensrent.session.Sesi;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {

    private final AuthService authService;
    private final AlatService alatService;
    private final TransaksiService transaksiService;
    private final PembayaranService pembayaranService;

    private final JTextField fieldUsername = new JTextField();
    private final JPasswordField fieldPassword = new JPasswordField();
    private final JLabel labelPesan = Tema.labelPesan();

    public LoginFrame(AuthService authService, AlatService alatService, TransaksiService transaksiService, PembayaranService pembayaranService) {
        super("LensRent - Masuk");
        this.authService = authService;
        this.alatService = alatService;
        this.transaksiService = transaksiService;
        this.pembayaranService = pembayaranService;

        JPanel kartu = Tema.kartuForm();
        kartu.add(Tema.judul("Masuk"));
        JLabel subjudul = new JLabel("Khusus admin toko LensRent.");
        subjudul.setFont(Tema.font(Font.PLAIN, 14));
        subjudul.setForeground(Tema.TEKS_SEKUNDER);
        kartu.add(subjudul);
        kartu.add(Box.createVerticalStrut(24));

        Tema.tambahField(kartu, "Username", fieldUsername);
        Tema.tambahField(kartu, "Password", fieldPassword);

        JButton tombolLogin = Tema.tombolUtama("Login");
        kartu.add(Box.createVerticalStrut(4));
        kartu.add(tombolLogin);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(labelPesan);

        // Kiri: panel merek gelap, kanan: form login
        JPanel isi = new JPanel(new GridLayout(1, 2));
        isi.add(Tema.panelMerek());
        isi.add(Tema.tengahkan(kartu));
        setContentPane(isi);

        tombolLogin.addActionListener(e -> login());
        getRootPane().setDefaultButton(tombolLogin); // tekan Enter = Login

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Tema.LEBAR_JENDELA, Tema.TINGGI_JENDELA);
        setLocationRelativeTo(null);
    }

    private void login() {
        try {
            Pengguna pengguna = authService.login(
                    fieldUsername.getText(), new String(fieldPassword.getPassword()));
            Sesi.simpan(pengguna);
            dispose();
            new DashboardAdminFrame(authService, alatService, transaksiService, pembayaranService).setVisible(true);
        } catch (IllegalArgumentException ex) {
            Tema.tampilkanError(labelPesan, ex.getMessage());
            fieldPassword.setText("");
        }
    }
}
