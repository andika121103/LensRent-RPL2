package com.lensrent.ui;

import com.lensrent.model.Transaksi;
import com.lensrent.service.PembayaranService;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/** Halaman admin: status bayar semua transaksi, konfirmasi/tolak transfer online, catat pembayaran di toko. */
public class KonfirmasiPembayaranPanel extends JPanel {

    private final PembayaranService pembayaranService;
    private final JTable tabel = Tema.tabel("No", "Penyewa", "Tgl Sewa", "Tgl Kembali", "Total", "Dibayar", "Sisa", "Status");
    private final JLabel labelPesan = Tema.labelPesan();
    /** Transaksi per baris tabel, urutan sama dengan baris. */
    private final List<Transaksi> daftar = new ArrayList<>();

    public KonfirmasiPembayaranPanel(PembayaranService pembayaranService) {
        super(new BorderLayout(0, 16));
        this.pembayaranService = pembayaranService;
        setBackground(Tema.KREM);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JButton tombolKonfirmasi = Tema.tombolUtama("Konfirmasi Transfer");
        tombolKonfirmasi.setPreferredSize(null); // ukuran mengikuti teks, bukan selebar form
        JButton tombolTolak = new JButton("Tolak Transfer");
        JButton tombolCatat = new JButton("Catat Bayar di Toko");
        JPanel tombol = new JPanel();
        tombol.setOpaque(false);
        tombol.add(tombolKonfirmasi);
        tombol.add(tombolTolak);
        tombol.add(tombolCatat);

        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(Tema.judul("Konfirmasi Pembayaran"), BorderLayout.WEST);
        atas.add(tombol, BorderLayout.EAST);
        atas.add(labelPesan, BorderLayout.SOUTH);

        add(atas, BorderLayout.NORTH);
        tabel.getColumnModel().getColumn(0).setMaxWidth(60);
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        tombolCatat.addActionListener(e -> {
            Transaksi t = terpilih();
            if (t == null) {
                return;
            }
            labelPesan.setVisible(false);
            // Dialog modal: baris berikutnya baru jalan setelah dialog ditutup
            new FormPembayaranDialog(SwingUtilities.getWindowAncestor(this), pembayaranService, t).setVisible(true);
            muat();
        });
        tombolKonfirmasi.addActionListener(e -> {
            Transaksi t = terpilih();
            if (t == null) {
                return;
            }
            try {
                pembayaranService.konfirmasi(t.getId());
                muat();
                Tema.tampilkanSukses(labelPesan, "Transfer transaksi No " + t.getId() + " dikonfirmasi.");
            } catch (IllegalArgumentException ex) {
                Tema.tampilkanError(labelPesan, ex.getMessage());
            }
        });
        tombolTolak.addActionListener(e -> {
            Transaksi t = terpilih();
            if (t == null) {
                return;
            }
            int jawab = JOptionPane.showConfirmDialog(this,
                    "Tolak transfer transaksi No " + t.getId() + "? Pesanan online tanpa pembayaran lain akan dibatalkan.",
                    "Tolak Transfer", JOptionPane.YES_NO_OPTION);
            if (jawab != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                boolean batal = pembayaranService.tolak(t.getId());
                muat();
                Tema.tampilkanSukses(labelPesan, batal
                        ? "Transfer ditolak, pesanan No " + t.getId() + " dibatalkan dan alat tersedia lagi."
                        : "Transfer ditolak.");
            } catch (IllegalArgumentException ex) {
                Tema.tampilkanError(labelPesan, ex.getMessage());
            }
        });

        muat();
    }

    /** Transaksi di baris terpilih; null (dengan pesan) jika belum ada yang dipilih. */
    private Transaksi terpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            Tema.tampilkanError(labelPesan, "Pilih transaksi di tabel terlebih dahulu.");
            return null;
        }
        return daftar.get(baris);
    }

    private void muat() {
        DefaultTableModel model = (DefaultTableModel) tabel.getModel();
        model.setRowCount(0);
        daftar.clear();
        for (Transaksi t : pembayaranService.semuaTransaksi()) {
            long dibayar = pembayaranService.dibayar(t);
            daftar.add(t);
            model.addRow(new Object[] {t.getId(), t.getPenyewa().getNama(), Tema.teksTanggal(t.getTanggalSewa()),
                Tema.teksTanggal(t.getTanggalKembali()), Tema.rupiah(t.getTotal()), Tema.rupiah(dibayar),
                Tema.rupiah(Math.max(0, t.getTotal() - dibayar)), pembayaranService.keterangan(t)});
        }
    }
}
