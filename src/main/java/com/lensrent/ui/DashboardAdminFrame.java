package com.lensrent.ui;

import com.lensrent.service.AlatService;
import com.lensrent.service.AuthService;
import com.lensrent.service.PembayaranService;
import com.lensrent.service.TransaksiService;
import com.lensrent.session.Sesi;
import java.awt.BorderLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** Halaman utama admin. Area tengah berganti sesuai menu sidebar. */
public class DashboardAdminFrame extends JFrame {

    public DashboardAdminFrame(AuthService authService, AlatService alatService, TransaksiService transaksiService, PembayaranService pembayaranService) {
        super("LensRent - Dashboard Admin");

        JPanel konten = new JPanel(new BorderLayout());
        konten.setBackground(Tema.KREM);

        Map<String, Runnable> menu = new LinkedHashMap<>();
        menu.put("Dashboard", () -> Tema.gantiIsi(konten, new DashboardPanel(alatService, transaksiService)));
        menu.put("Data Alat", () -> Tema.gantiIsi(konten, new DataAlatPanel(alatService, transaksiService)));
        menu.put("Transaksi Sewa", () -> Tema.gantiIsi(konten, new TransaksiSewaPanel(transaksiService, alatService, pembayaranService)));
        menu.put("Pengembalian & Denda", () -> Tema.gantiIsi(konten, new PengembalianPanel(transaksiService)));
        menu.put("Laporan", () -> Tema.gantiIsi(konten, new LaporanPanel(pembayaranService)));

        JPanel isi = new JPanel(new BorderLayout());
        isi.add(new Sidebar(this, authService, alatService, transaksiService, pembayaranService, Sesi.getPenggunaAktif(), menu), BorderLayout.WEST);
        isi.add(konten, BorderLayout.CENTER);
        setContentPane(isi);

        menu.get("Dashboard").run(); // halaman pertama setelah login

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Tema.LEBAR_JENDELA, Tema.TINGGI_JENDELA);
        setLocationRelativeTo(null);
    }
}
