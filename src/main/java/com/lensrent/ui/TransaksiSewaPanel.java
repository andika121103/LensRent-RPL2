package com.lensrent.ui;

import com.lensrent.model.Transaksi;
import com.lensrent.service.AlatService;
import com.lensrent.service.PembayaranService;
import com.lensrent.service.TransaksiService;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/** Halaman admin: daftar transaksi sewa (semua lunas), catat transaksi baru, cetak nota. */
public class TransaksiSewaPanel extends JPanel {

    private final TransaksiService transaksiService;
    private final PembayaranService pembayaranService;
    private final JTable tabel = Tema.tabel("No", "Penyewa", "No. KTP", "Tgl Sewa", "Tgl Kembali", "Lama", "Alat", "Total");
    private final JLabel labelPesan = Tema.labelPesan();
    /** Transaksi per baris tabel, urutan sama dengan baris. */
    private final List<Transaksi> daftar = new ArrayList<>();

    public TransaksiSewaPanel(TransaksiService transaksiService, AlatService alatService, PembayaranService pembayaranService) {
        super(new BorderLayout(0, 16));
        this.transaksiService = transaksiService;
        this.pembayaranService = pembayaranService;
        setBackground(Tema.KREM);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JButton tombolCatat = Tema.tombolUtama("Catat Transaksi");
        tombolCatat.setPreferredSize(null); // ukuran mengikuti teks, bukan selebar form
        JButton tombolNota = new JButton("Cetak Nota (PDF)");
        tombolNota.addActionListener(e -> cetakNota(this::transaksiTerpilih));
        JPanel tombol = new JPanel();
        tombol.setOpaque(false);
        tombol.add(tombolNota);
        tombol.add(tombolCatat);

        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(Tema.kepala("Transaksi Sewa", "Semua sewa yang tercatat, dibayar lunas saat dicatat"), BorderLayout.WEST);
        atas.add(tombol, BorderLayout.EAST);
        atas.add(labelPesan, BorderLayout.SOUTH);

        add(atas, BorderLayout.NORTH);
        tabel.getColumnModel().getColumn(0).setMaxWidth(60);
        // lebar relatif kolom; Alat paling lebar karena memuat beberapa nama alat
        int[] lebar = {45, 140, 165, 105, 105, 65, 265, 115};
        for (int i = 0; i < lebar.length; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        Tema.rataKanan(tabel, 7);
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        tombolCatat.addActionListener(e -> {
            labelPesan.setVisible(false);
            // Dialog modal: baris berikutnya baru jalan setelah dialog ditutup
            FormTransaksiDialog form = new FormTransaksiDialog(SwingUtilities.getWindowAncestor(this), transaksiService, alatService);
            form.setVisible(true);
            muat();
            if (form.getTersimpan() != null) { // langsung buka nota sewa yang baru dibayar
                cetakNota(form::getTersimpan);
            }
        });

        muat();
    }

    /** Nota langsung dibuka sebagai PDF (Documents\LensRent\Nota-Sewa-No-<no>.pdf). */
    private void cetakNota(Supplier<Transaksi> transaksi) {
        // transaksi diambil di dalam Cetak.pdf agar "belum memilih baris" tampil sebagai pesan, bukan error
        Cetak.pdf(labelPesan, () -> pembayaranService.nota(transaksi.get().getId()), () -> "Nota-Sewa-No-" + transaksi.get().getId());
    }

    /** Transaksi di baris terpilih; IllegalArgumentException (pesan tampil di layar) jika belum memilih. */
    private Transaksi transaksiTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            throw new IllegalArgumentException("Pilih transaksi di tabel terlebih dahulu.");
        }
        return daftar.get(baris);
    }

    private void muat() {
        DefaultTableModel model = (DefaultTableModel) tabel.getModel();
        model.setRowCount(0);
        daftar.clear();
        for (Transaksi t : transaksiService.semua()) {
            daftar.add(t);
            String alat = t.getDetail().stream()
                    .map(d -> d.getAlat().getNama() + " x" + d.getJumlah())
                    .collect(Collectors.joining(", "));
            model.addRow(new Object[] {t.getId(), t.getPenyewa().getNama(), t.getPenyewa().getNoKtp(), Tema.teksTanggal(t.getTanggalSewa()),
                Tema.teksTanggal(t.getTanggalKembali()), t.getLamaHari() + " hari", alat, Tema.rupiah(t.getTotal())});
        }
    }
}
