package com.lensrent.ui;

import com.lensrent.service.PembayaranService;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.function.Supplier;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import net.sf.jasperreports.engine.JasperPrint;

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
        Supplier<JasperPrint> laporan = () -> pembayaranService.laporanTransaksi(
                Tema.nilaiTanggal(fieldAwal), Tema.nilaiTanggal(fieldAkhir));
        JButton tombolCetak = Tema.tombolUtama("Cetak Laporan (PDF)");
        kartu.add(tombolCetak);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(labelPesan);

        tombolCetak.addActionListener(e -> Cetak.pdf(labelPesan, laporan,
                () -> "Laporan-Transaksi_" + Tema.nilaiTanggal(fieldAwal) + "_sd_" + Tema.nilaiTanggal(fieldAkhir)));

        add(Tema.tengahkan(kartu), BorderLayout.CENTER);
    }
}
