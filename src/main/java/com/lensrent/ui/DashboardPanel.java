package com.lensrent.ui;

import com.lensrent.model.Transaksi;
import com.lensrent.service.AlatService;
import com.lensrent.service.TransaksiService;
import com.lensrent.session.Sesi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Halaman pertama admin: ringkasan hari ini dan sewa yang harus kembali hari ini atau terlambat. */
public class DashboardPanel extends JPanel {

    public DashboardPanel(AlatService alatService, TransaksiService transaksiService) {
        super(new BorderLayout(0, 16));
        setBackground(Tema.KREM);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        LocalDate hariIni = LocalDate.now();
        List<Transaksi> semua = transaksiService.semua();
        List<Transaksi> belumKembali = semua.stream()
                .filter(t -> t.getTanggalDikembalikan() == null && !hariIni.isBefore(t.getTanggalSewa()))
                .toList();
        List<Transaksi> perhatian = belumKembali.stream()
                .filter(t -> !hariIni.isBefore(t.getTanggalKembali()))
                .toList();
        YearMonth bulanIni = YearMonth.from(hariIni);
        // sama dengan Laporan: pendapatan dihitung menurut tanggal sewa (sewa + denda)
        long pendapatan = semua.stream()
                .filter(t -> YearMonth.from(t.getTanggalSewa()).equals(bulanIni))
                .mapToLong(t -> t.getTotal() + t.getTotalDenda())
                .sum();

        long terlambat = belumKembali.stream().filter(t -> t.getHariTerlambat(hariIni) > 0).count();
        int jenisAlat = alatService.semua().size();

        JPanel kartu = new JPanel(new GridLayout(1, 4, 16, 0));
        kartu.setOpaque(false);
        kartu.add(kartu("JENIS ALAT", String.valueOf(jenisAlat), "terdaftar di Data Alat", Tema.KUNING));
        kartu.add(kartu("SEDANG DISEWA", String.valueOf(belumKembali.size()), "transaksi belum kembali", Tema.ORANYE_TERANG));
        kartu.add(kartu("TERLAMBAT", String.valueOf(terlambat),
                terlambat == 0 ? "semua tepat waktu" : "perlu dihubungi", terlambat == 0 ? Tema.HIJAU_TEKS : Tema.ERROR_TEKS));
        kartu.add(kartu("PENDAPATAN BULAN INI", Tema.rupiah(pendapatan), "sewa + denda", Tema.HITAM));

        String nama = Sesi.getPenggunaAktif() == null ? "Admin" : Sesi.getPenggunaAktif().getNama();
        String tanggal = hariIni.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID")));
        JPanel atas = new JPanel(new BorderLayout(0, 20));
        atas.setOpaque(false);
        atas.add(Tema.kepala("Halo, " + nama, "Ringkasan toko hari ini, " + tanggal), BorderLayout.NORTH);
        atas.add(kartu, BorderLayout.CENTER);
        JLabel subjudul = new JLabel("Harus kembali hari ini atau terlambat");
        subjudul.setFont(Tema.font(Font.BOLD, 16));
        subjudul.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        atas.add(subjudul, BorderLayout.SOUTH);
        add(atas, BorderLayout.NORTH);

        JTable tabel = Tema.tabel("No", "Penyewa", "No. HP", "Jadwal Kembali", "Status");
        tabel.getColumnModel().getColumn(0).setMaxWidth(60);
        Tema.kolomStatus(tabel, 4);
        DefaultTableModel model = (DefaultTableModel) tabel.getModel();
        for (Transaksi t : perhatian) {
            model.addRow(new Object[] {t.getId(), t.getPenyewa().getNama(), t.getPenyewa().getNoHp(),
                Tema.teksTanggal(t.getTanggalKembali()), TransaksiService.status(t, hariIni)});
        }
        add(new JScrollPane(tabel), BorderLayout.CENTER);
    }

    /** Kartu ringkasan putih bersudut bulat: garis warna, judul kecil, angka besar, keterangan. */
    private static JPanel kartu(String judul, String nilai, String keterangan, Color warna) {
        JPanel kartu = new JPanel();
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBackground(Tema.PUTIH);
        kartu.putClientProperty("FlatLaf.style", "arc: 16");
        kartu.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel garis = new JPanel();
        garis.setBackground(warna);
        garis.putClientProperty("FlatLaf.style", "arc: 4");
        Dimension ukuran = new Dimension(32, 4);
        garis.setPreferredSize(ukuran);
        garis.setMaximumSize(ukuran);
        garis.setAlignmentX(Component.LEFT_ALIGNMENT);

        kartu.add(garis);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(teks(judul, Font.BOLD, 12, Tema.TEKS_SEKUNDER));
        kartu.add(Box.createVerticalStrut(6));
        kartu.add(teks(nilai, Font.BOLD, 26, Tema.HITAM));
        kartu.add(Box.createVerticalStrut(2));
        kartu.add(teks(keterangan, Font.PLAIN, 13, Tema.TEKS_SEKUNDER));
        return kartu;
    }

    private static JLabel teks(String isi, int gaya, int ukuran, Color warna) {
        JLabel label = new JLabel(isi);
        label.setFont(Tema.font(gaya, ukuran));
        label.setForeground(warna);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
