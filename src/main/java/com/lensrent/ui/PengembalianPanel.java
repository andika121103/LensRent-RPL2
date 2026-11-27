package com.lensrent.ui;

import com.lensrent.model.Transaksi;
import com.lensrent.service.TransaksiService;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/** Halaman admin: status pengembalian setiap sewa, catat pengembalian beserta dendanya. */
public class PengembalianPanel extends JPanel {

    private final TransaksiService transaksiService;
    private final JTable tabel = Tema.tabel("No", "Penyewa", "Tgl Sewa", "Jadwal Kembali", "Status", "Dikembalikan", "Denda");
    private final JLabel labelPesan = Tema.labelPesan();
    /** Transaksi per baris tabel, urutan sama dengan baris. */
    private final List<Transaksi> daftar = new ArrayList<>();

    public PengembalianPanel(TransaksiService transaksiService) {
        super(new BorderLayout(0, 16));
        this.transaksiService = transaksiService;
        setBackground(Tema.KREM);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JButton tombolCatat = Tema.tombolUtama("Catat Pengembalian");
        tombolCatat.setPreferredSize(null); // ukuran mengikuti teks, bukan selebar form
        JPanel tombol = new JPanel();
        tombol.setOpaque(false);
        tombol.add(tombolCatat);

        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(Tema.kepala("Pengembalian & Denda", "Pilih sewa lalu catat pengembaliannya; denda telat dihitung otomatis"), BorderLayout.WEST);
        atas.add(tombol, BorderLayout.EAST);
        atas.add(labelPesan, BorderLayout.SOUTH);
        add(atas, BorderLayout.NORTH);

        tabel.getColumnModel().getColumn(0).setMaxWidth(60);
        Tema.kolomStatus(tabel, 4);
        Tema.rataKanan(tabel, 6);
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        JLabel aturan = new JLabel("Denda telat = jumlah hari terlambat x harga sewa per hari semua alat. "
                + "Denda kerusakan diisi admin. Denda dibayar saat alat dikembalikan.");
        aturan.setForeground(Tema.TEKS_SEKUNDER);
        add(aturan, BorderLayout.SOUTH);

        tombolCatat.addActionListener(e -> catat());
        muat();
    }

    private void catat() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            Tema.tampilkanError(labelPesan, "Pilih transaksi di tabel terlebih dahulu.");
            return;
        }
        Transaksi transaksi = daftar.get(baris);
        if (transaksi.getTanggalDikembalikan() != null) {
            Tema.tampilkanError(labelPesan, "Alat transaksi No " + transaksi.getId() + " sudah dikembalikan.");
            return;
        }
        if (LocalDate.now().isBefore(transaksi.getTanggalSewa())) {
            Tema.tampilkanError(labelPesan, "Sewa No " + transaksi.getId() + " belum dimulai, alat belum diambil.");
            return;
        }
        labelPesan.setVisible(false);
        // Dialog modal: baris berikutnya baru jalan setelah dialog ditutup
        new PengembalianDialog(SwingUtilities.getWindowAncestor(this), transaksiService, transaksi).setVisible(true);
        muat();
    }

    private void muat() {
        DefaultTableModel model = (DefaultTableModel) tabel.getModel();
        model.setRowCount(0);
        daftar.clear();
        LocalDate hariIni = LocalDate.now();
        for (Transaksi t : transaksiService.semua()) {
            daftar.add(t);
            boolean kembali = t.getTanggalDikembalikan() != null;
            model.addRow(new Object[] {t.getId(), t.getPenyewa().getNama(), Tema.teksTanggal(t.getTanggalSewa()),
                Tema.teksTanggal(t.getTanggalKembali()), TransaksiService.status(t, hariIni),
                kembali ? Tema.teksTanggal(t.getTanggalDikembalikan()) : "-",
                kembali ? Tema.rupiah(t.getTotalDenda()) : "-"});
        }
    }
}
