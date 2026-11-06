package com.lensrent.ui;

import com.lensrent.model.Alat;
import com.lensrent.service.AlatService;
import com.lensrent.service.TransaksiService;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Penyewa memilih rentang tanggal, lalu melihat sisa unit tiap alat pada tanggal itu. */
public class CekKetersediaanDialog extends JDialog {

    private final AlatService alatService;
    private final TransaksiService transaksiService;
    private final JSpinner fieldSewa = Tema.pilihTanggal(0);
    private final JSpinner fieldKembali = Tema.pilihTanggal(1);
    private final JTable tabel = Tema.tabel("Nama", "Kategori", "Merek", "Harga/Hari", "Stok Tersedia");
    private final JLabel labelPesan = Tema.labelPesan();

    public CekKetersediaanDialog(Window pemilik, AlatService alatService, TransaksiService transaksiService) {
        super(pemilik, "Cek Ketersediaan", ModalityType.APPLICATION_MODAL);
        this.alatService = alatService;
        this.transaksiService = transaksiService;

        JButton tombolCek = Tema.tombolUtama("Cek");
        tombolCek.setPreferredSize(null); // ukuran mengikuti teks, bukan selebar form

        JPanel saringan = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        saringan.setOpaque(false);
        saringan.add(labelUntuk("Tanggal sewa:", fieldSewa));
        saringan.add(fieldSewa);
        saringan.add(labelUntuk("Tanggal kembali:", fieldKembali));
        saringan.add(fieldKembali);
        saringan.add(tombolCek);

        JScrollPane gulir = new JScrollPane(tabel);
        gulir.setPreferredSize(new Dimension(760, 360));

        JPanel isi = new JPanel(new BorderLayout(0, 12));
        isi.setBackground(Tema.KREM);
        isi.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        isi.add(saringan, BorderLayout.NORTH);
        isi.add(gulir, BorderLayout.CENTER);
        isi.add(labelPesan, BorderLayout.SOUTH);
        setContentPane(isi);

        tombolCek.addActionListener(e -> cek());
        getRootPane().setDefaultButton(tombolCek);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(pemilik);

        cek();
    }

    private JLabel labelUntuk(String teks, JSpinner field) {
        JLabel label = new JLabel(teks);
        label.setLabelFor(field);
        return label;
    }

    private void cek() {
        DefaultTableModel model = (DefaultTableModel) tabel.getModel();
        model.setRowCount(0);
        labelPesan.setVisible(false);
        try {
            LocalDate sewa = Tema.nilaiTanggal(fieldSewa);
            LocalDate kembali = Tema.nilaiTanggal(fieldKembali);
            for (Alat alat : alatService.semua()) {
                model.addRow(new Object[] {alat.getNama(), alat.getKategori(), alat.getMerek(),
                    Tema.rupiah(alat.getHargaPerHari()), transaksiService.sisaUnit(alat, sewa, kembali)});
            }
        } catch (IllegalArgumentException ex) {
            Tema.tampilkanError(labelPesan, ex.getMessage());
        }
    }
}
