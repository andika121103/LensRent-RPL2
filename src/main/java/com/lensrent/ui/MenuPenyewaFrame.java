package com.lensrent.ui;

import com.lensrent.service.AlatService;
import com.lensrent.service.AuthService;
import com.lensrent.session.Sesi;
import java.awt.BorderLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** Halaman utama penyewa. Area tengah berganti sesuai menu sidebar. */
public class MenuPenyewaFrame extends JFrame {

    public MenuPenyewaFrame(AuthService authService, AlatService alatService) {
        super("LensRent - Menu Penyewa");

        JPanel konten = new JPanel(new BorderLayout());
        konten.setBackground(Tema.KREM);

        // null = halaman dibuat di tahap berikutnya (tombol menu nonaktif)
        Map<String, Runnable> menu = new LinkedHashMap<>();
        menu.put("Katalog & Cek Ketersediaan", () -> Tema.gantiIsi(konten, new KatalogPanel(alatService)));
        menu.put("Riwayat Sewa & Denda", null);

        JPanel isi = new JPanel(new BorderLayout());
        isi.add(new Sidebar(this, authService, alatService, Sesi.getPenggunaAktif(), menu), BorderLayout.WEST);
        isi.add(konten, BorderLayout.CENTER);
        setContentPane(isi);

        menu.get("Katalog & Cek Ketersediaan").run(); // halaman pertama setelah login

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Tema.LEBAR_JENDELA, Tema.TINGGI_JENDELA);
        setLocationRelativeTo(null);
    }
}
