package com.lensrent.ui;

import com.lensrent.model.Alat;
import com.lensrent.model.Kategori;
import com.lensrent.service.AlatService;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Halaman penyewa: lihat alat (hanya baca), cari nama/merek, saring kategori. */
public class KatalogPanel extends JPanel {

    private static final String SEMUA_KATEGORI = "Semua Kategori";

    private final AlatService alatService;
    private final JTextField fieldCari = new JTextField(20);
    private final JComboBox<Object> pilihKategori = new JComboBox<>();
    private final JTable tabel = Tema.tabel("Nama", "Kategori", "Merek", "Harga/Hari", "Stok", "Ketersediaan", "Keterangan");

    public KatalogPanel(AlatService alatService) {
        super(new BorderLayout(0, 16));
        this.alatService = alatService;
        setBackground(Tema.KREM);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        pilihKategori.addItem(SEMUA_KATEGORI);
        for (Kategori kategori : Kategori.values()) {
            pilihKategori.addItem(kategori);
        }

        JButton tombolCari = Tema.tombolUtama("Cari");
        tombolCari.setPreferredSize(null); // ukuran mengikuti teks, bukan selebar form

        JLabel labelCari = new JLabel("Cari nama/merek:");
        labelCari.setLabelFor(fieldCari);

        JPanel saringan = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        saringan.setOpaque(false);
        saringan.add(labelCari);
        saringan.add(fieldCari);
        saringan.add(pilihKategori);
        saringan.add(tombolCari);

        JPanel atas = new JPanel(new BorderLayout(0, 12));
        atas.setOpaque(false);
        atas.add(Tema.judul("Katalog Alat"), BorderLayout.NORTH);
        atas.add(saringan, BorderLayout.CENTER);

        add(atas, BorderLayout.NORTH);
        add(new JScrollPane(tabel), BorderLayout.CENTER);

        tombolCari.addActionListener(e -> muat());
        fieldCari.addActionListener(e -> muat()); // tekan Enter di kolom cari
        pilihKategori.addActionListener(e -> muat());

        muat();
    }

    private void muat() {
        Kategori kategori = pilihKategori.getSelectedItem() instanceof Kategori k ? k : null;
        DefaultTableModel model = (DefaultTableModel) tabel.getModel();
        model.setRowCount(0);
        for (Alat alat : alatService.cari(fieldCari.getText(), kategori)) {
            model.addRow(new Object[] {alat.getNama(), alat.getKategori(), alat.getMerek(),
                Tema.rupiah(alat.getHargaPerHari()), alat.getStok(),
                alat.isBisaDisewa() ? "Tersedia" : "Tidak tersedia",
                alat.getKeterangan() == null ? "-" : alat.getKeterangan()});
        }
    }
}
