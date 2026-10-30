package com.lensrent.ui;

import com.lensrent.service.AlatService;
import com.lensrent.service.AuthService;
import com.lensrent.session.Sesi;
import java.awt.BorderLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** Halaman utama admin. Area tengah berganti sesuai menu sidebar. */
public class DashboardAdminFrame extends JFrame {

    public DashboardAdminFrame(AuthService authService, AlatService alatService) {
        super("LensRent - Dashboard Admin");

        JPanel konten = new JPanel(new BorderLayout());
        konten.setBackground(Tema.KREM);

        // null = halaman dibuat di tahap berikutnya (tombol menu nonaktif)
        Map<String, Runnable> menu = new LinkedHashMap<>();
        menu.put("Data Alat", () -> Tema.gantiIsi(konten, new DataAlatPanel(alatService)));
        menu.put("Konfirmasi Pembayaran", null);
        menu.put("Transaksi Sewa", null);
        menu.put("Pengembalian & Denda", null);

        JPanel isi = new JPanel(new BorderLayout());
        isi.add(new Sidebar(this, authService, alatService, Sesi.getPenggunaAktif(), menu), BorderLayout.WEST);
        isi.add(konten, BorderLayout.CENTER);
        setContentPane(isi);

        menu.get("Data Alat").run(); // halaman pertama setelah login

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Tema.LEBAR_JENDELA, Tema.TINGGI_JENDELA);
        setLocationRelativeTo(null);
    }
}
