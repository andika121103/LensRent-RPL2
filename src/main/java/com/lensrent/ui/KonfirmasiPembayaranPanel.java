package com.lensrent.ui;

import com.lensrent.model.Transaksi;
import com.lensrent.service.PembayaranService;
import java.awt.BorderLayout;
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

/** Halaman admin: status bayar semua transaksi, catat pembayaran di toko, cetak nota. */
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

        JButton tombolCatat = Tema.tombolUtama("Catat Bayar di Toko");
        tombolCatat.setPreferredSize(null); // ukuran mengikuti teks, bukan selebar form
        JButton tombolNota = new JButton("Cetak Nota");
        JPanel tombol = new JPanel();
        tombol.setOpaque(false);
        tombol.add(tombolCatat);
        tombol.add(tombolNota);

        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(Tema.judul("Pembayaran"), BorderLayout.WEST);
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
        tombolNota.addActionListener(e -> {
            Transaksi t = terpilih();
            if (t != null) {
                labelPesan.setVisible(false);
                Cetak.tampilkan(labelPesan, () -> pembayaranService.nota(t.getId()));
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
