package com.lensrent.ui;

import com.lensrent.service.AlatService;
import com.lensrent.service.AuthService;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Pendaftaran akun khusus penyewa. Akun admin tidak bisa dibuat dari sini. */
public class DaftarAkunFrame extends JFrame {

    private final AuthService authService;
    private final AlatService alatService;

    private final JTextField fieldNama = new JTextField();
    private final JTextField fieldNoHp = new JTextField();
    private final JTextField fieldEmail = new JTextField();
    private final JTextField fieldAlamat = new JTextField();
    private final JTextField fieldUsername = new JTextField();
    private final JPasswordField fieldPassword = new JPasswordField();
    private final JPasswordField fieldUlangiPassword = new JPasswordField();
    private final JLabel labelPesan = Tema.labelPesan();

    public DaftarAkunFrame(AuthService authService, AlatService alatService) {
        super("LensRent - Daftar Akun Penyewa");
        this.authService = authService;
        this.alatService = alatService;

        JPanel kartu = Tema.kartuForm();
        kartu.add(Tema.judul("Daftar Akun"));
        JLabel subjudul = new JLabel("Buat akun penyewa untuk cek alat dari rumah.");
        subjudul.setFont(Tema.font(Font.PLAIN, 14));
        subjudul.setForeground(Tema.TEKS_SEKUNDER);
        kartu.add(subjudul);
        kartu.add(Box.createVerticalStrut(12));

        Tema.tambahField(kartu, "Nama lengkap", fieldNama);
        Tema.tambahField(kartu, "No. HP", fieldNoHp);
        Tema.tambahField(kartu, "Email", fieldEmail);
        Tema.tambahField(kartu, "Alamat", fieldAlamat);
        Tema.tambahField(kartu, "Username", fieldUsername);
        Tema.tambahField(kartu, "Password", fieldPassword);
        Tema.tambahField(kartu, "Ulangi password", fieldUlangiPassword);

        JButton tombolDaftar = Tema.tombolUtama("Daftar");
        kartu.add(tombolDaftar);
        kartu.add(Box.createVerticalStrut(10));
        kartu.add(labelPesan);
        kartu.add(Box.createVerticalStrut(6));

        JButton tombolLogin = Tema.tombolLink("Sudah punya akun? Login");
        kartu.add(tombolLogin);

        JPanel isi = new JPanel(new GridLayout(1, 2));
        isi.add(Tema.panelMerek());
        isi.add(Tema.tengahkan(kartu));
        setContentPane(isi);

        tombolDaftar.addActionListener(e -> daftar());
        tombolLogin.addActionListener(e -> kembaliKeLogin(null));
        getRootPane().setDefaultButton(tombolDaftar);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Tema.LEBAR_JENDELA, Tema.TINGGI_JENDELA);
        setLocationRelativeTo(null);
    }

    private void daftar() {
        try {
            authService.daftar(
                    fieldNama.getText(),
                    fieldNoHp.getText(),
                    fieldEmail.getText(),
                    fieldAlamat.getText(),
                    fieldUsername.getText(),
                    new String(fieldPassword.getPassword()),
                    new String(fieldUlangiPassword.getPassword()));
            kembaliKeLogin("Akun berhasil dibuat, silakan login.");
        } catch (IllegalArgumentException ex) {
            Tema.tampilkanError(labelPesan, ex.getMessage());
        }
    }

    private void kembaliKeLogin(String pesanSukses) {
        dispose();
        LoginFrame loginFrame = new LoginFrame(authService, alatService);
        if (pesanSukses != null) {
            loginFrame.tampilkanSukses(pesanSukses);
        }
        loginFrame.setVisible(true);
    }
}
