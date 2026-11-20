package com.lensrent.ui;

import com.lensrent.service.PembayaranService;
import java.awt.BorderLayout;
import java.time.LocalDate;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

/** Halaman admin: laporan transaksi sewa per periode tanggal sewa (JasperReports). */
public class LaporanPanel extends JPanel {

    public LaporanPanel(PembayaranService pembayaranService) {
        super(new BorderLayout());
        setBackground(Tema.KREM);

        JSpinner fieldAwal = Tema.pilihTanggal(1 - LocalDate.now().getDayOfMonth()); // awal bulan ini
        JSpinner fieldAkhir = Tema.pilihTanggal(0);
        JLabel labelPesan = Tema.labelPesan();

        JPanel kartu = Tema.kartuForm();
        kartu.add(Tema.judul("Laporan Transaksi"));
        kartu.add(Box.createVerticalStrut(16));
        Tema.tambahField(kartu, "Tanggal Sewa Dari", fieldAwal);
        Tema.tambahField(kartu, "Sampai", fieldAkhir);
        kartu.add(Box.createVerticalStrut(8));
        JButton tombolTampilkan = Tema.tombolUtama("Tampilkan Laporan");
        kartu.add(tombolTampilkan);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(labelPesan);

        tombolTampilkan.addActionListener(e -> {
            labelPesan.setVisible(false);
            Cetak.tampilkan(labelPesan, () -> pembayaranService.laporanTransaksi(
                    Tema.nilaiTanggal(fieldAwal), Tema.nilaiTanggal(fieldAkhir)));
        });

        add(Tema.tengahkan(kartu), BorderLayout.CENTER);
    }
}
