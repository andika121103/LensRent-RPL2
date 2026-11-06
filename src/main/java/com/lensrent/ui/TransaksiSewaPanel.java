package com.lensrent.ui;

import com.lensrent.model.Transaksi;
import com.lensrent.service.AlatService;
import com.lensrent.service.TransaksiService;
import java.awt.BorderLayout;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/** Halaman admin: daftar transaksi sewa + catat transaksi baru. */
public class TransaksiSewaPanel extends JPanel {

    private final TransaksiService transaksiService;
    private final JTable tabel = Tema.tabel("Penyewa", "Tgl Sewa", "Tgl Kembali", "Lama", "Alat", "Total");

    public TransaksiSewaPanel(TransaksiService transaksiService, AlatService alatService) {
        super(new BorderLayout(0, 16));
        this.transaksiService = transaksiService;
        setBackground(Tema.KREM);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JButton tombolCatat = Tema.tombolUtama("Catat Transaksi");
        tombolCatat.setPreferredSize(null); // ukuran mengikuti teks, bukan selebar form

        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(Tema.judul("Transaksi Sewa"), BorderLayout.WEST);
        atas.add(tombolCatat, BorderLayout.EAST);

        add(atas, BorderLayout.NORTH);
        tabel.getColumnModel().getColumn(4).setPreferredWidth(420); // kolom Alat memuat beberapa nama alat
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        tombolCatat.addActionListener(e -> {
            // Dialog modal: baris berikutnya baru jalan setelah dialog ditutup
            new FormTransaksiDialog(SwingUtilities.getWindowAncestor(this), transaksiService, alatService).setVisible(true);
            muat();
        });

        muat();
    }

    private void muat() {
        DefaultTableModel model = (DefaultTableModel) tabel.getModel();
        model.setRowCount(0);
        for (Transaksi t : transaksiService.semua()) {
            String alat = t.getDetail().stream()
                    .map(d -> d.getAlat().getNama() + " x" + d.getJumlah())
                    .collect(Collectors.joining(", "));
            model.addRow(new Object[] {t.getPenyewa().getNama(), Tema.teksTanggal(t.getTanggalSewa()),
                Tema.teksTanggal(t.getTanggalKembali()), t.getLamaHari() + " hari", alat, Tema.rupiah(t.getTotal())});
        }
    }
}
