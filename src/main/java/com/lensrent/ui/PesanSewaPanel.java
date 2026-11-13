package com.lensrent.ui;

import com.lensrent.model.Alat;
import com.lensrent.service.AlatService;
import com.lensrent.service.PembayaranService;
import com.lensrent.service.TransaksiService;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/** Halaman penyewa: pilih alat dan tanggal, lalu bayar online lewat transfer. */
public class PesanSewaPanel extends JPanel {

    private final PembayaranService pembayaranService;
    private final JSpinner fieldSewa = Tema.pilihTanggal(0);
    private final JSpinner fieldKembali = Tema.pilihTanggal(1);
    private final JComboBox<Alat> pilihAlat = new JComboBox<>();
    private final JSpinner fieldJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JTable tabelItem = Tema.tabel("Alat", "Jumlah");
    private final JLabel labelTotal = new JLabel();
    private final JLabel labelPesan = Tema.labelPesan();
    /** Alat yang akan disewa -> jumlah unit; urutan sama dengan baris tabel. */
    private final Map<Alat, Integer> daftarItem = new LinkedHashMap<>();

    public PesanSewaPanel(AlatService alatService, PembayaranService pembayaranService) {
        super(new java.awt.BorderLayout());
        this.pembayaranService = pembayaranService;
        setBackground(Tema.KREM);

        alatService.semua().stream().filter(Alat::isBisaDisewa).forEach(pilihAlat::addItem);
        Tema.teksDropdown(pilihAlat, a -> a.getNama() + " - " + Tema.rupiah(a.getHargaPerHari()) + "/hari");

        JPanel kartu = Tema.kartuForm();
        kartu.add(Tema.judul("Pesan Sewa"));
        JLabel keterangan = new JLabel("<html>Bayar online lewat transfer, lalu ambil alat di toko.<br>"
                + "Ingin bayar tunai? Datang langsung ke toko.</html>");
        keterangan.setFont(Tema.font(Font.PLAIN, 13));
        keterangan.setForeground(Tema.TEKS_SEKUNDER);
        keterangan.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.add(keterangan);
        kartu.add(Box.createVerticalStrut(16));

        Tema.tambahField(kartu, "Tanggal Sewa (ambil di toko)", fieldSewa);
        Tema.tambahField(kartu, "Tanggal Kembali", fieldKembali);
        Tema.tambahField(kartu, "Alat", pilihAlat);
        Tema.tambahField(kartu, "Jumlah Unit", fieldJumlah);

        JButton tombolTambah = new JButton("Tambah ke Daftar");
        JButton tombolHapus = new JButton("Hapus dari Daftar");
        JPanel tombolItem = new JPanel();
        tombolItem.setOpaque(false);
        tombolItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        tombolItem.add(tombolTambah);
        tombolItem.add(tombolHapus);
        kartu.add(tombolItem);

        JScrollPane gulirItem = new JScrollPane(tabelItem);
        gulirItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        gulirItem.setPreferredSize(new Dimension(380, 110));
        gulirItem.setMaximumSize(new Dimension(380, 110));
        kartu.add(gulirItem);
        kartu.add(Box.createVerticalStrut(12));

        labelTotal.setFont(Tema.font(Font.BOLD, 16));
        labelTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartu.add(labelTotal);
        kartu.add(Box.createVerticalStrut(12));

        JButton tombolBayar = Tema.tombolUtama("Bayar Online (Transfer)");
        kartu.add(tombolBayar);
        kartu.add(Box.createVerticalStrut(12));
        kartu.add(labelPesan);

        tombolTambah.addActionListener(e -> tambahItem());
        tombolHapus.addActionListener(e -> hapusItem());
        tombolBayar.addActionListener(e -> bayar());
        fieldSewa.addChangeListener(e -> perbaruiTotal());
        fieldKembali.addChangeListener(e -> perbaruiTotal());
        perbaruiTotal();

        add(Tema.tengahkan(kartu), java.awt.BorderLayout.CENTER);
    }

    private void tambahItem() {
        Alat alat = (Alat) pilihAlat.getSelectedItem();
        if (alat == null) {
            Tema.tampilkanError(labelPesan, "Tidak ada alat yang bisa disewa.");
            return;
        }
        daftarItem.merge(alat, (Integer) fieldJumlah.getValue(), Integer::sum);
        muatItem();
    }

    private void hapusItem() {
        int baris = tabelItem.getSelectedRow();
        if (baris < 0) {
            Tema.tampilkanError(labelPesan, "Pilih alat di daftar terlebih dahulu.");
            return;
        }
        daftarItem.remove(daftarItem.keySet().stream().skip(baris).findFirst().orElseThrow());
        muatItem();
    }

    private void muatItem() {
        DefaultTableModel model = (DefaultTableModel) tabelItem.getModel();
        model.setRowCount(0);
        daftarItem.forEach((alat, jumlah) -> model.addRow(new Object[] {alat.getNama(), jumlah}));
        labelPesan.setVisible(false);
        perbaruiTotal();
    }

    /** Pratinjau total; -1 jika tanggal belum benar atau total terlalu besar. Total tersimpan dihitung ulang oleh TransaksiService. */
    private long hitungTotal() {
        long hari = ChronoUnit.DAYS.between(Tema.tanggalTerakhir(fieldSewa), Tema.tanggalTerakhir(fieldKembali));
        if (hari <= 0) {
            return -1;
        }
        try {
            long total = 0;
            for (Map.Entry<Alat, Integer> item : daftarItem.entrySet()) {
                total = Math.addExact(total, TransaksiService.subtotal(item.getKey().getHargaPerHari(), item.getValue(), hari));
            }
            return total;
        } catch (ArithmeticException e) {
            return -1;
        }
    }

    private void perbaruiTotal() {
        long total = hitungTotal();
        labelTotal.setText(total < 0 ? "Total: -" : "Total bayar: " + Tema.rupiah(total));
    }

    private void bayar() {
        if (daftarItem.isEmpty()) {
            Tema.tampilkanError(labelPesan, "Tambahkan minimal satu alat.");
            return;
        }
        LocalDate sewa;
        LocalDate kembali;
        try {
            sewa = Tema.nilaiTanggal(fieldSewa);
            kembali = Tema.nilaiTanggal(fieldKembali);
        } catch (IllegalArgumentException ex) {
            Tema.tampilkanError(labelPesan, ex.getMessage());
            return;
        }
        Map<Long, Integer> jumlahPerAlat = new LinkedHashMap<>(); // urutan sesuai yang diisi penyewa
        daftarItem.forEach((alat, jumlah) -> jumlahPerAlat.put(alat.getId(), jumlah));

        BayarOnlineDialog dialog = new BayarOnlineDialog(SwingUtilities.getWindowAncestor(this), pembayaranService,
                sewa, kembali, daftarItem, jumlahPerAlat, hitungTotal());
        dialog.setVisible(true); // modal: lanjut setelah dialog ditutup
        if (dialog.getNomorPesanan() != null) {
            daftarItem.clear();
            muatItem();
            Tema.tampilkanSukses(labelPesan, "Pesanan No " + dialog.getNomorPesanan()
                    + " tercatat dan menunggu konfirmasi admin. Lihat statusnya di menu Status Pembayaran.");
        }
    }
}
