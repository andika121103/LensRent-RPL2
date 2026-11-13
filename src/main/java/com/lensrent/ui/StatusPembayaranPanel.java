package com.lensrent.ui;

import com.lensrent.model.Transaksi;
import com.lensrent.service.PembayaranService;
import com.lensrent.session.Sesi;
import java.awt.BorderLayout;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Halaman penyewa: status bayar transaksi milik penyewa yang sedang login (hanya lihat). */
public class StatusPembayaranPanel extends JPanel {

    public StatusPembayaranPanel(PembayaranService pembayaranService) {
        super(new BorderLayout(0, 16));
        setBackground(Tema.KREM);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JTable tabel = Tema.tabel("No", "Tgl Sewa", "Tgl Kembali", "Alat", "Total", "Dibayar", "Sisa", "Status");
        tabel.getColumnModel().getColumn(0).setMaxWidth(60);
        tabel.getColumnModel().getColumn(3).setPreferredWidth(320); // kolom Alat memuat beberapa nama alat

        DefaultTableModel model = (DefaultTableModel) tabel.getModel();
        for (Transaksi t : pembayaranService.transaksiPenyewa(Sesi.getPenggunaAktif().getId())) {
            long dibayar = pembayaranService.dibayar(t);
            String alat = t.getDetail().stream()
                    .map(d -> d.getAlat().getNama() + " x" + d.getJumlah())
                    .collect(Collectors.joining(", "));
            model.addRow(new Object[] {t.getId(), Tema.teksTanggal(t.getTanggalSewa()), Tema.teksTanggal(t.getTanggalKembali()),
                alat, Tema.rupiah(t.getTotal()), Tema.rupiah(dibayar),
                Tema.rupiah(Math.max(0, t.getTotal() - dibayar)), pembayaranService.keterangan(t)});
        }

        add(Tema.judul("Status Pembayaran"), BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);
    }
}
